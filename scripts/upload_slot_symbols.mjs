#!/usr/bin/env node
/**
 * Upload slot machine reel images from slots/ to Firebase Storage and update seed JSON.
 *
 * Usage:
 *   node scripts/upload_slot_symbols.mjs --dry-run
 *   node scripts/upload_slot_symbols.mjs
 *   node scripts/upload_slot_symbols.mjs --symbol-id trophy --force
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
import { scanSlotSourceDir } from "./slot_symbol_match_lib.mjs";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, "..");

loadEnvFile(root);

const projectConfig = readJson(path.join(root, "project.config.json"));
const seedPaths = [
  path.join(root, projectConfig.seed.android),
  path.join(root, projectConfig.seed.functions),
];
const progressPath = path.join(root, "data", "slot_symbol_upload_progress.json");

function defaultSourceDir() {
  const fromEnv = process.env.SLOT_SOURCE_DIR?.trim();
  if (fromEnv) {
    return path.isAbsolute(fromEnv) ? fromEnv : path.join(root, fromEnv);
  }
  return path.join(root, "slots");
}

function loadProgress() {
  if (!fs.existsSync(progressPath)) return { symbols: {} };
  return readJson(progressPath);
}

function saveProgress(progress) {
  writeJson(progressPath, progress);
}

function needsImage(url, force) {
  return force || !url || String(url).trim() === "";
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const sourceDir = defaultSourceDir();
  const baseSeed = projectConfig.seed.functions;

  let symbols = readJson(path.join(baseSeed, "slot_symbols_seed.json"));
  const players = readJson(path.join(baseSeed, "players_seed.json"));
  const progress = loadProgress();

  const scan = scanSlotSourceDir(sourceDir, symbols, players);
  console.log(
    `slot scan: ${scan.matched.length} matched, ${scan.unmatchedFiles.length} unmatched files`
  );
  if (scan.symbolsWithoutFile.length) {
    console.log(`  symbols without file: ${scan.symbolsWithoutFile.length}`);
    for (const s of scan.symbolsWithoutFile) {
      console.log(`    ${s.symbolId} (${s.label})`);
    }
  }
  if (scan.unmatchedFiles.length) {
    for (const u of scan.unmatchedFiles) {
      console.log(`  unmatched: ${u.filePath}`);
    }
  }

  let queue = scan.matched.filter((row) => needsImage(row.symbol.imageUrl, args.force));
  if (args.symbolId) {
    queue = queue.filter((row) => row.symbol.symbolId === args.symbolId);
  }
  if (args.limit != null) queue = queue.slice(0, args.limit);

  console.log(`\nSlot symbol upload queue: ${queue.length}\n`);

  if (args.dryRun) {
    for (const row of queue) {
      console.log(`  [dry-run] ${row.symbol.symbolId} ← ${row.filePath}`);
      console.log(`    → slots/${row.symbol.symbolId}${row.ext}`);
    }
    console.log("\nDry run complete — no uploads or file writes.");
    return;
  }

  let bucket = null;
  if (!args.skipUpload) {
    const admin = initFirebaseAdmin(root, projectConfig);
    bucket = admin.storage().bucket();
  }

  const symbolById = new Map(symbols.map((s) => [s.symbolId, s]));
  let done = 0;
  let failed = 0;

  const persistSeed = () => {
    writeSeedPair(seedPaths, "slot_symbols_seed.json", symbols);
  };

  for (const row of queue) {
    const id = row.symbol.symbolId;
    const symbol = symbolById.get(id);
    if (!symbol) {
      failed++;
      console.error(`  fail ${id}: symbol not in seed`);
      continue;
    }

    try {
      const storagePath = `slots/${id}${row.ext}`;
      let publicUrl;
      if (args.skipUpload) {
        publicUrl = row.filePath;
      } else {
        publicUrl = await uploadLocalFileToStorage(bucket, storagePath, row.filePath);
      }

      symbol.imageUrl = publicUrl;
      progress.symbols[id] = {
        imageUrl: publicUrl,
        sourceFile: row.filePath,
        at: new Date().toISOString(),
      };
      done++;
      if (done % 3 === 0) {
        persistSeed();
        saveProgress(progress);
      }
      console.log(`  ok ${id}`);
    } catch (e) {
      failed++;
      console.error(`  fail ${id}: ${e.message || e}`);
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
  process.exit(1);
});
