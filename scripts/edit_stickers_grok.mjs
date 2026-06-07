#!/usr/bin/env node
/**
 * Transform Panini scans (wc26/) into Pixar-style stickers via Grok Imagine edit on fal.ai.
 * Uploads source + result to Firebase Storage and updates seed JSON. Does NOT seed Firestore.
 *
 * Prerequisites:
 *   - FAL_KEY in .env
 *   - wc26/ folder with country subfolders and player PNGs
 *   - cd functions && npm install
 *   - gcloud auth application-default login (or GOOGLE_APPLICATION_CREDENTIALS)
 *
 * Usage:
 *   node scripts/edit_stickers_grok.mjs --dry-run
 *   node scripts/edit_stickers_grok.mjs --dry-run --limit 10
 *   node scripts/edit_stickers_grok.mjs --limit 5
 *   node scripts/edit_stickers_grok.mjs
 *   node scripts/edit_stickers_grok.mjs --player-id argentina_lionel_messi --force
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import {
  fetchImageBuffer,
  generateWithGrokEdit,
  GROK_EDIT_COST_PER_IMAGE,
  GROK_EDIT_MODEL,
  grokEditPrompt,
  initFirebaseAdmin,
  loadEnvFile,
  parseArgs,
  readJson,
  runPool,
  uploadLocalFileToStorage,
  uploadToStorage,
  writeJson,
  writeSeedPair,
} from "./sticker_images_lib.mjs";
import {
  playersWithoutWc26File,
  scanWc26SourceDir,
} from "./wc26_sticker_match_lib.mjs";

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
const progressPath = path.join(root, "data", "grok_sticker_edit_progress.json");
const localOutDir = path.join(root, "data", "generated_stickers_grok");

function defaultSourceDir() {
  return process.env.STICKER_SOURCE_DIR?.trim() || path.join(root, "wc26");
}

function loadProgress() {
  if (!fs.existsSync(progressPath)) return { players: {} };
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

function printScanReport(scan, players) {
  const noFile = playersWithoutWc26File(players, scan.matched);
  console.log(`wc26 scan: ${scan.matched.length} matched, ${scan.unmatchedFiles.length} unmatched files`);
  console.log(`  seed players without wc26 file: ${noFile.length}`);
  if (scan.unknownFolders.length) {
    console.log(`  unknown folders (no teamId): ${scan.unknownFolders.join(", ")}`);
  }
  if (scan.conflicts.length) {
    console.log(`  name key conflicts: ${scan.conflicts.length} (first wins)`);
  }
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const sourceDir = path.isAbsolute(defaultSourceDir())
    ? defaultSourceDir()
    : path.join(root, defaultSourceDir());

  if (!process.env.FAL_KEY && !args.dryRun) {
    console.error("Missing FAL_KEY. Copy .env.example to .env and set your fal.ai API key.");
    process.exit(1);
  }

  const baseSeed = projectConfig.seed.functions;
  let players = readJson(playersPath(baseSeed));
  let stickers = readJson(stickersPath(baseSeed));
  const stickerByPlayer = indexStickersByPlayerId(stickers);
  const progress = loadProgress();

  const scan = scanWc26SourceDir(sourceDir, readJson(path.join(baseSeed, "teams_seed.json")), players);
  printScanReport(scan, players);

  let queue = scan.matched.filter((row) => needsImage(row.player.imageUrl, args.force));
  if (args.playerId) {
    queue = queue.filter((row) => row.player.playerId === args.playerId);
    if (!queue.length) {
      const direct = scan.matched.find((r) => r.player.playerId === args.playerId);
      if (!direct) {
        console.error(`No wc26 file matched player-id ${args.playerId}`);
        process.exit(1);
      }
      if (!args.force && !needsImage(direct.player.imageUrl, false)) {
        console.error(`Player ${args.playerId} already has imageUrl (use --force)`);
        process.exit(1);
      }
      queue = [direct];
    }
  }
  if (args.limit != null) queue = queue.slice(0, args.limit);

  const est = (queue.length * GROK_EDIT_COST_PER_IMAGE).toFixed(2);
  console.log(`\nGrok edit queue: ${queue.length} (${GROK_EDIT_MODEL}, ~$${est} @ $${GROK_EDIT_COST_PER_IMAGE}/image)\n`);

  if (args.dryRun) {
    for (const row of queue.slice(0, 8)) {
      const p = row.player;
      console.log(`  [dry-run] ${p.playerId}`);
      console.log(`    source: ${row.filePath}`);
      console.log(`    prompt: ${grokEditPrompt(p.playerName).slice(0, 100)}…`);
    }
    if (queue.length > 8) console.log(`  … and ${queue.length - 8} more`);
    if (scan.unmatchedFiles.length) {
      console.log(`\nUnmatched files (sample):`);
      for (const u of scan.unmatchedFiles.slice(0, 5)) {
        console.log(`  ${u.filePath}`);
      }
      if (scan.unmatchedFiles.length > 5) {
        console.log(`  … and ${scan.unmatchedFiles.length - 5} more`);
      }
    }
    console.log("\nDry run complete — no API calls or file writes.");
    return;
  }

  const admin = initFirebaseAdmin(root, projectConfig);
  const bucket = admin.storage().bucket();
  fs.mkdirSync(localOutDir, { recursive: true });

  let done = 0;
  let failed = 0;

  const persistSeed = () => {
    writeSeedPair(seedPaths, "players_seed.json", players);
    writeSeedPair(seedPaths, "stickers_seed.json", stickers);
  };

  const playerById = new Map(players.map((p) => [p.playerId, p]));

  await runPool(queue, args.concurrency, async (row) => {
    const id = row.player.playerId;
    const player = playerById.get(id);
    if (!player) {
      failed++;
      console.error(`  fail ${id}: player not in seed`);
      return;
    }

    try {
      const prompt = grokEditPrompt(player.playerName);
      const sourceStoragePath = `stickers/sources/wc26/${id}${row.ext}`;
      const sourceUrl = await uploadLocalFileToStorage(bucket, sourceStoragePath, row.filePath);

      const { imageUrl: falUrl } = await generateWithGrokEdit({
        prompt,
        imageUrl: sourceUrl,
      });

      const buffer = await fetchImageBuffer(falUrl);
      const storagePath = `stickers/players/${id}.jpg`;
      const publicUrl = await uploadToStorage(bucket, storagePath, buffer, "image/jpeg");

      player.imageUrl = publicUrl;
      const sticker = stickerByPlayer.get(id);
      if (sticker) sticker.imageUrl = publicUrl;

      progress.players[id] = {
        imageUrl: publicUrl,
        sourceUrl,
        sourceFile: row.filePath,
        at: new Date().toISOString(),
      };
      done++;
      if (done % 5 === 0) {
        persistSeed();
        saveProgress(progress);
      }
      console.log(`  ok ${id}`);
    } catch (e) {
      failed++;
      console.error(`  fail ${id}: ${e.message || e}`);
    }
  });

  persistSeed();
  saveProgress(progress);
  console.log(`\nDone. Edited/updated: ${done}, failed: ${failed}`);
  console.log("Seed JSON updated in android + functions assets.");
  console.log("Next: npm run go-live:publish");
}

main().catch((e) => {
  console.error(e.message || e);
  if (String(e).includes("Could not load the default credentials")) {
    console.error(
      "\nFor Firebase Storage upload, set GOOGLE_APPLICATION_CREDENTIALS or run:\n" +
        "  gcloud auth application-default login\n"
    );
  }
  process.exit(1);
});
