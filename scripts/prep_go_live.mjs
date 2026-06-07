#!/usr/bin/env node
/**
 * Pre-flight checks before generating sticker images and seeding Firestore.
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { createRequire } from "module";
import {
  getFalModel,
  GROK_EDIT_COST_PER_IMAGE,
  IMAGEN4_COST_PER_IMAGE,
  loadEnvFile,
  readJson,
} from "./sticker_images_lib.mjs";
import { scanWc26SourceDir } from "./wc26_sticker_match_lib.mjs";

const root = path.join(path.dirname(fileURLToPath(import.meta.url)), "..");
loadEnvFile(root);

const config = readJson(path.join(root, "project.config.json"));
const seedDir = path.join(root, config.seed.functions);
const require = createRequire(import.meta.url);

let ok = true;

function pass(msg) {
  console.log(`  ✓ ${msg}`);
}
function fail(msg) {
  console.log(`  ✗ ${msg}`);
  ok = false;
}
function warn(msg) {
  console.log(`  ! ${msg}`);
}

console.log("Go-live preflight\n");

if (process.env.FAL_KEY?.trim()) {
  pass(`FAL_KEY set (model: ${getFalModel()})`);
} else {
  fail("FAL_KEY missing — copy .env.example to .env and add your fal.ai key");
}

const adminPath = path.join(root, "functions/node_modules/firebase-admin");
if (fs.existsSync(adminPath)) {
  pass("firebase-admin installed (functions/)");
} else {
  fail("Run: cd functions && npm install");
}

try {
  const admin = require(adminPath);
  if (!admin.apps.length) {
    admin.initializeApp({
      projectId: config.firebase.projectId,
      storageBucket: config.firebase.storageBucket,
    });
  }
  pass(`Firebase project: ${config.firebase.projectId}`);
} catch (e) {
  warn(`Firebase init: ${e.message || e} (set ADC or GOOGLE_APPLICATION_CREDENTIALS before upload)`);
}

const players = readJson(path.join(seedDir, "players_seed.json"));
const stickers = readJson(path.join(seedDir, "stickers_seed.json"));
const teams = readJson(path.join(seedDir, "teams_seed.json"));

const playersMissing = players.filter((p) => !p.imageUrl?.trim()).length;
const emblemsMissing = stickers.filter(
  (s) => s.stickerNumber === 0 && !s.playerId && !s.imageUrl?.trim()
).length;
const pixarPrompts = players.filter((p) =>
  String(p.animeStickerPrompt || "").includes("Pixar-style")
).length;

pass(`${players.length} players, ${pixarPrompts} with Pixar prompts`);
if (playersMissing === 0) {
  pass("All players already have imageUrl (use --force to regenerate)");
} else {
  console.log(`  → ${playersMissing} player images still to generate`);
}
if (emblemsMissing > 0) {
  console.log(`  → ${emblemsMissing} team emblems still to generate`);
}

const wc26Dir = process.env.STICKER_SOURCE_DIR?.trim()
  ? path.isAbsolute(process.env.STICKER_SOURCE_DIR)
    ? process.env.STICKER_SOURCE_DIR
    : path.join(root, process.env.STICKER_SOURCE_DIR)
  : path.join(root, "wc26");

if (fs.existsSync(wc26Dir)) {
  try {
    const scan = scanWc26SourceDir(wc26Dir, teams, players);
    const grokQueue = scan.matched.filter((r) => !r.player.imageUrl?.trim()).length;
    const grokEst = (grokQueue * GROK_EDIT_COST_PER_IMAGE).toFixed(2);
    console.log(
      `\nwc26/ found: ${scan.matched.length} matched scans → Grok edit ~$${grokEst} (${grokQueue} without imageUrl)`
    );
    console.log("  npm run edit:stickers-grok -- --dry-run");
    console.log("  npm run go-live:edit-grok:pilot");
    console.log("  npm run go-live:edit-grok");
  } catch (e) {
    warn(`wc26 scan: ${e.message || e}`);
  }
}

const model = getFalModel();
const costPer = model.includes("imagen") ? IMAGEN4_COST_PER_IMAGE : 0.003;
const total = playersMissing + emblemsMissing;
const est = (total * costPer).toFixed(2);
console.log(`\nImagen/Kontext path (approx): $${est} (${total} images × $${costPer})`);
console.log("Budget ~10% extra for retries.\n");

if (ok) {
  console.log("Ready. Image paths — see docs/GO_LIVE.md:");
  console.log("  Grok (wc26 scans): go-live:edit-grok");
  console.log("  Imagen/Kontext:    go-live:images → go-live:images:emblems");
  console.log("  Then: npm run go-live:publish\n");
} else {
  console.log("Fix the items above, then re-run: npm run go-live:check\n");
  process.exit(1);
}
