#!/usr/bin/env node
/**
 * Upload local team logo JPGs/PNGs to Firebase Storage and set emblem URLs in seed JSON.
 * Updates teams_seed.json (customEmblemUrl) and stickers_seed.json (emblem slot imageUrl).
 *
 * Prerequisites:
 *   - GOOGLE_APPLICATION_CREDENTIALS in .env (or exported)
 *   - Logos in logos done/ (default) — flat folder, one file per country
 *   - cd functions && npm install
 *
 * Usage:
 *   node scripts/upload_emblems.mjs --dry-run
 *   node scripts/upload_emblems.mjs
 *   node scripts/upload_emblems.mjs --team-id argentina --force
 *   node scripts/upload_emblems.mjs --skip-upload   # copy paths only (local file:// paths)
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import {
  initFirebaseAdmin,
  loadEnvFile,
  parseArgs,
  readJson,
  uploadLocalFileToStorage,
  writeJson,
  writeSeedPair,
} from "./sticker_images_lib.mjs";
import {
  indexEmblemStickersByTeamId,
  scanLogoSourceDir,
} from "./emblem_upload_lib.mjs";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, "..");

loadEnvFile(root);

const projectConfig = readJson(path.join(root, "project.config.json"));
const seedPaths = [
  path.join(root, projectConfig.seed.android),
  path.join(root, projectConfig.seed.functions),
];
const progressPath = path.join(root, "data", "emblem_upload_progress.json");

function defaultSourceDir() {
  const fromEnv = process.env.LOGO_SOURCE_DIR?.trim();
  if (fromEnv) {
    return path.isAbsolute(fromEnv) ? fromEnv : path.join(root, fromEnv);
  }
  return path.join(root, "logos done");
}

function loadProgress() {
  if (!fs.existsSync(progressPath)) return { teams: {} };
  return readJson(progressPath);
}

function saveProgress(progress) {
  writeJson(progressPath, progress);
}

function needsEmblem(url, force) {
  return force || !url || String(url).trim() === "";
}

function printScanReport(scan) {
  console.log(
    `logo scan: ${scan.matched.length} matched, ${scan.unmatchedFiles.length} unmatched files`
  );
  console.log(`  teams without logo file: ${scan.teamsWithoutLogo.length}`);
  if (scan.unmatchedFiles.length) {
    console.log("\nUnmatched files:");
    for (const u of scan.unmatchedFiles) {
      console.log(`  ${u.filePath}`);
    }
  }
  if (scan.teamsWithoutLogo.length) {
    console.log("\nTeams missing a logo file:");
    for (const t of scan.teamsWithoutLogo) {
      console.log(`  ${t.teamId} (${t.countryName})`);
    }
  }
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const sourceDir = defaultSourceDir();

  const baseSeed = projectConfig.seed.functions;
  let teams = readJson(path.join(baseSeed, "teams_seed.json"));
  let stickers = readJson(path.join(baseSeed, "stickers_seed.json"));
  const emblemByTeam = indexEmblemStickersByTeamId(stickers);
  const progress = loadProgress();

  const scan = scanLogoSourceDir(sourceDir, teams);
  printScanReport(scan);

  let queue = scan.matched.filter((row) => {
    const team = row.team;
    const sticker = emblemByTeam.get(row.teamId);
    const teamNeeds = needsEmblem(team.customEmblemUrl, args.force);
    const stickerNeeds = sticker ? needsEmblem(sticker.imageUrl, args.force) : true;
    return teamNeeds || stickerNeeds;
  });

  if (args.teamId) {
    queue = queue.filter((row) => row.teamId === args.teamId);
    if (!queue.length) {
      const direct = scan.matched.find((r) => r.teamId === args.teamId);
      if (!direct) {
        console.error(`No logo file matched team-id ${args.teamId}`);
        process.exit(1);
      }
      queue = [direct];
    }
  }
  if (args.limit != null) queue = queue.slice(0, args.limit);

  console.log(`\nEmblem upload queue: ${queue.length}\n`);

  if (args.dryRun) {
    for (const row of queue) {
      console.log(`  [dry-run] ${row.teamId} ← ${row.filePath}`);
      console.log(`    → emblems/${row.teamId}.jpg`);
    }
    console.log("\nDry run complete — no uploads or file writes.");
    return;
  }

  let bucket = null;
  if (!args.skipUpload) {
    const admin = initFirebaseAdmin(root, projectConfig);
    bucket = admin.storage().bucket();
  }

  let done = 0;
  let failed = 0;

  const teamById = new Map(teams.map((t) => [t.teamId, t]));

  const persistSeed = () => {
    writeSeedPair(seedPaths, "teams_seed.json", teams);
    writeSeedPair(seedPaths, "stickers_seed.json", stickers);
  };

  for (const row of queue) {
    const { teamId } = row;
    const team = teamById.get(teamId);
    const sticker = emblemByTeam.get(teamId);

    if (!team) {
      failed++;
      console.error(`  fail ${teamId}: team not in seed`);
      continue;
    }
    if (!sticker) {
      failed++;
      console.error(`  fail ${teamId}: no emblem sticker row (stickerNumber 0)`);
      continue;
    }

    try {
      const storagePath = `emblems/${teamId}.jpg`;
      let publicUrl;
      if (args.skipUpload) {
        publicUrl = row.filePath;
      } else {
        publicUrl = await uploadLocalFileToStorage(bucket, storagePath, row.filePath);
      }

      team.customEmblemUrl = publicUrl;
      sticker.imageUrl = publicUrl;

      progress.teams[teamId] = {
        imageUrl: publicUrl,
        sourceFile: row.filePath,
        at: new Date().toISOString(),
      };
      done++;
      if (done % 10 === 0) {
        persistSeed();
        saveProgress(progress);
      }
      console.log(`  ok ${teamId}`);
    } catch (e) {
      failed++;
      console.error(`  fail ${teamId}: ${e.message || e}`);
    }
  }

  persistSeed();
  saveProgress(progress);
  console.log(`\nDone. Uploaded/updated: ${done}, failed: ${failed}`);
  console.log("Seed JSON updated in android + functions assets.");
  console.log("Next: npm run go-live:publish");
}

main().catch((e) => {
  console.error(e.message || e);
  if (String(e).includes("Could not load the default credentials")) {
    console.error(
      "\nSet GOOGLE_APPLICATION_CREDENTIALS in .env or export it before running.\n"
    );
  }
  process.exit(1);
});
