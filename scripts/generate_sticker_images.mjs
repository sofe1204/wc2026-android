#!/usr/bin/env node
/**
 * Generate Pixar-style sticker images via fal.ai (Imagen 4 by default), upload to Firebase Storage,
 * and write imageUrl into seed JSON (android + functions). Does NOT seed Firestore — run
 * npm run go-live:publish when ready.
 *
 * Prerequisites:
 *   - FAL_KEY in .env or environment (https://fal.ai)
 *   - cd functions && npm install
 *   - For upload: GOOGLE_APPLICATION_CREDENTIALS or gcloud auth application-default login
 *
 * Usage:
 *   node scripts/generate_sticker_images.mjs --dry-run --limit 3
 *   node scripts/generate_sticker_images.mjs --limit 10
 *   node scripts/generate_sticker_images.mjs
 *   node scripts/generate_sticker_images.mjs --skip-upload --limit 5   # local JPEG only
 *   node scripts/generate_sticker_images.mjs --player-id mexico_raul_rangel
 *   node scripts/generate_sticker_images.mjs --emblems-only
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import {
  buildEmblemPrompt,
  buildPlayerPrompt,
  fetchImageBuffer,
  generateWithFal,
  getFalModel,
  initFirebaseAdmin,
  loadEnvFile,
  parseArgs,
  readJson,
  runPool,
  uploadToStorage,
  writeJson,
  writeSeedPair,
} from "./sticker_images_lib.mjs";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, "..");

loadEnvFile(root);

const projectConfig = readJson(path.join(root, "project.config.json"));
const seedPaths = [
  path.join(root, projectConfig.seed.android),
  path.join(root, projectConfig.seed.functions),
];
const playersPath = (dir) => path.join(dir, "players_seed.json");
const stickersPath = (dir) => path.join(dir, "stickers_seed.json");
const teamsPath = path.join(root, projectConfig.seed.functions, "teams_seed.json");
const progressPath = path.join(root, "data", "sticker_images_progress.json");
const localOutDir = path.join(root, "data", "generated_stickers");

function loadProgress() {
  if (!fs.existsSync(progressPath)) return { players: {}, emblems: {} };
  return readJson(progressPath);
}

function saveProgress(progress) {
  writeJson(progressPath, progress);
}

function needsImage(url, force) {
  return force || !url || String(url).trim() === "";
}

function indexStickersByPlayerId(stickers) {
  const map = new Map();
  for (const s of stickers) {
    if (s.playerId) map.set(s.playerId, s);
  }
  return map;
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const runPlayers = !args.emblemsOnly;
  const runEmblems = !args.playersOnly;

  if (!process.env.FAL_KEY && !args.dryRun) {
    console.error("Missing FAL_KEY. Copy .env.example to .env and set your fal.ai API key.");
    process.exit(1);
  }

  if (!args.dryRun) {
    console.log(`fal model: ${getFalModel()} (aspect 3:4)\n`);
  }

  const baseSeed = projectConfig.seed.functions;
  let players = readJson(playersPath(baseSeed));
  let stickers = readJson(stickersPath(baseSeed));
  const teams = readJson(teamsPath);
  const teamById = new Map(teams.map((t) => [t.teamId, t]));
  const stickerByPlayer = indexStickersByPlayerId(stickers);

  const progress = loadProgress();
  const globalSeed = process.env.STICKER_IMAGE_SEED
    ? Number(process.env.STICKER_IMAGE_SEED)
    : undefined;

  let bucket = null;
  if (!args.skipUpload && !args.dryRun) {
    const admin = initFirebaseAdmin(root, projectConfig);
    bucket = admin.storage().bucket();
  }

  fs.mkdirSync(localOutDir, { recursive: true });

  let done = 0;
  let failed = 0;

  const persistSeed = () => {
    writeSeedPair(seedPaths, "players_seed.json", players);
    writeSeedPair(seedPaths, "stickers_seed.json", stickers);
  };

  if (runPlayers) {
    let queue = players.filter(
      (p) =>
        p.isActive !== false &&
        p.animeStickerPrompt &&
        needsImage(p.imageUrl, args.force)
    );
    if (args.playerId) queue = queue.filter((p) => p.playerId === args.playerId);
    if (args.limit != null) queue = queue.slice(0, args.limit);

    console.log(`Players to generate: ${queue.length}`);
    if (args.dryRun) {
      for (const p of queue.slice(0, 5)) {
        console.log(`  [dry-run] ${p.playerId}: ${buildPlayerPrompt(p.animeStickerPrompt).slice(0, 120)}…`);
      }
      if (queue.length > 5) console.log(`  … and ${queue.length - 5} more`);
    } else {
      await runPool(queue, args.concurrency, async (player) => {
        const id = player.playerId;
        try {
          const prompt = buildPlayerPrompt(player.animeStickerPrompt);
          const { imageUrl: falUrl, seed } = await generateWithFal(prompt, {
            seed: globalSeed,
          });
          let publicUrl = falUrl;
          if (!args.skipUpload) {
            const buffer = await fetchImageBuffer(falUrl);
            const storagePath = `stickers/players/${id}.jpg`;
            publicUrl = await uploadToStorage(bucket, storagePath, buffer, "image/jpeg");
          } else {
            const buffer = await fetchImageBuffer(falUrl);
            const localFile = path.join(localOutDir, `${id}.jpg`);
            fs.writeFileSync(localFile, buffer);
            publicUrl = localFile;
          }

          player.imageUrl = publicUrl;
          const sticker = stickerByPlayer.get(id);
          if (sticker) sticker.imageUrl = publicUrl;

          progress.players[id] = { imageUrl: publicUrl, seed, at: new Date().toISOString() };
          done++;
          if (done % 5 === 0) {
            persistSeed();
            saveProgress(progress);
          }
          console.log(`  ok ${id}`);
          return { ok: true };
        } catch (e) {
          failed++;
          console.error(`  fail ${id}: ${e.message || e}`);
          return { ok: false };
        }
      });
    }
  }

  if (runEmblems && !args.dryRun) {
    if (runPlayers && args.limit != null) {
      console.log("Skipping emblems when --limit is set (re-run with --emblems-only).");
    } else {
      let emblemStickers = stickers.filter(
        (s) => s.stickerNumber === 0 && !s.playerId && needsImage(s.imageUrl, args.force)
      );
      if (args.teamId) emblemStickers = emblemStickers.filter((s) => s.teamId === args.teamId);
      if (args.limit != null) emblemStickers = emblemStickers.slice(0, args.limit);
      console.log(`Team emblems to generate: ${emblemStickers.length}`);
      await runPool(emblemStickers, Math.min(args.concurrency, 2), async (sticker) => {
        const team = teamById.get(sticker.teamId);
        if (!team) {
          failed++;
          console.error(`  fail ${sticker.stickerId}: unknown teamId ${sticker.teamId}`);
          return;
        }
        try {
          const prompt = buildEmblemPrompt(team);
          const { imageUrl: falUrl, seed } = await generateWithFal(prompt, { seed: globalSeed });
          let publicUrl = falUrl;
          if (!args.skipUpload) {
            const buffer = await fetchImageBuffer(falUrl);
            const storagePath = `emblems/${sticker.teamId}.jpg`;
            publicUrl = await uploadToStorage(bucket, storagePath, buffer, "image/jpeg");
          } else {
            const buffer = await fetchImageBuffer(falUrl);
            const localFile = path.join(localOutDir, `emblem_${sticker.teamId}.jpg`);
            fs.writeFileSync(localFile, buffer);
            publicUrl = localFile;
          }

          sticker.imageUrl = publicUrl;
          team.customEmblemUrl = publicUrl;

          progress.emblems[sticker.teamId] = { imageUrl: publicUrl, seed, at: new Date().toISOString() };
          done++;
          if (done % 3 === 0) {
            persistSeed();
            writeSeedPair(seedPaths, "teams_seed.json", teams);
            saveProgress(progress);
          }
          console.log(`  ok emblem ${sticker.teamId}`);
        } catch (e) {
          failed++;
          console.error(`  fail emblem ${sticker.teamId}: ${e.message || e}`);
        }
      });
    }
  } else if (runEmblems && args.dryRun) {
    const emblemStickers = stickers.filter((s) => s.stickerNumber === 0 && !s.playerId);
    const sample = emblemStickers[0];
    if (sample) {
      const team = teamById.get(sample.teamId);
      console.log(`  [dry-run] emblem ${sample.teamId}: ${buildEmblemPrompt(team).slice(0, 120)}…`);
    }
    console.log(`  [dry-run] ${emblemStickers.length} emblems total`);
  }

  if (!args.dryRun) {
    persistSeed();
    writeSeedPair(seedPaths, "teams_seed.json", teams);
    saveProgress(progress);
    console.log(`\nDone. Generated/updated: ${done}, failed: ${failed}`);
    console.log("Seed JSON updated in android + functions assets.");
    console.log("Next: npm run go-live:publish");
  } else {
    console.log("\nDry run complete — no API calls or file writes.");
  }
}

main().catch((e) => {
  console.error(e.message || e);
  if (String(e).includes("Could not load the default credentials")) {
    console.error(
      "\nFor Firebase Storage upload, set GOOGLE_APPLICATION_CREDENTIALS or run:\n" +
        "  gcloud auth application-default login\n" +
        "Or use --skip-upload to save JPEGs under data/generated_stickers/ only.\n"
    );
  }
  process.exit(1);
});
