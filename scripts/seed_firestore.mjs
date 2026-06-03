#!/usr/bin/env node
/**
 * Upload teams / players / stickers from functions/seed into Firestore.
 * Required for signed-in users (packs, slots, album sync use cloud collections).
 *
 * Auth: set GOOGLE_APPLICATION_CREDENTIALS to a service account JSON with
 * Firestore + Firebase Auth admin (or run: gcloud auth application-default login).
 *
 * Usage:
 *   node scripts/seed_firestore.mjs
 *   node scripts/seed_firestore.mjs --only teams,players
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { createRequire } from "module";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, "..");
const require = createRequire(import.meta.url);

const projectConfig = JSON.parse(
  fs.readFileSync(path.join(root, "project.config.json"), "utf8")
);
const projectId = projectConfig.firebase.projectId;
const seedDir = path.join(root, projectConfig.seed.functions);

const BATCH_SIZE = 400;

function loadJson(name) {
  const p = path.join(seedDir, name);
  return JSON.parse(fs.readFileSync(p, "utf8"));
}

function parseOnly(argv) {
  const flag = argv.find((a) => a.startsWith("--only"));
  if (!flag) return null;
  const value = flag.includes("=") ? flag.split("=")[1] : argv[argv.indexOf(flag) + 1];
  if (!value) return null;
  return new Set(value.split(",").map((s) => s.trim()));
}

async function batchSeed(db, collection, items, idField) {
  const admin = require(path.join(root, "functions/node_modules/firebase-admin"));
  const now = admin.firestore.FieldValue.serverTimestamp();
  let batch = db.batch();
  let ops = 0;
  const batches = [];

  for (const item of items) {
    const id = String(item[idField]);
    const ref = db.collection(collection).doc(id);
    batch.set(ref, { ...item, createdAt: now, updatedAt: now }, { merge: true });
    ops++;
    if (ops % BATCH_SIZE === 0) {
      batches.push(batch);
      batch = db.batch();
    }
  }
  batches.push(batch);

  for (const b of batches) {
    await b.commit();
  }
  return items.length;
}

async function main() {
  const only = parseOnly(process.argv.slice(2));
  const run = (key) => !only || only.has(key);

  let admin;
  try {
    admin = require(path.join(root, "functions/node_modules/firebase-admin"));
  } catch {
    console.error("Run: cd functions && npm install");
    process.exit(1);
  }

  if (!admin.apps.length) {
    admin.initializeApp({ projectId });
  }
  const db = admin.firestore();

  console.log(`Seeding Firestore project: ${projectId}`);
  console.log(`Source: ${seedDir}\n`);

  if (run("teams")) {
    const teams = loadJson("teams_seed.json");
    const n = await batchSeed(db, "teams", teams, "teamId");
    console.log(`  teams: ${n}`);
  }
  if (run("players")) {
    const players = loadJson("players_seed.json");
    const n = await batchSeed(db, "players", players, "playerId");
    console.log(`  players: ${n}`);
  }
  if (run("stickers")) {
    const stickers = loadJson("stickers_seed.json");
    const n = await batchSeed(db, "stickers", stickers, "stickerId");
    console.log(`  stickers: ${n}`);
  }

  console.log("\nDone. Signed-in users can open packs and browse the official catalog.");
}

main().catch((e) => {
  console.error(e.message || e);
  if (String(e).includes("Could not load the default credentials")) {
    console.error(
      "\nSet GOOGLE_APPLICATION_CREDENTIALS to a Firebase service account key, or run:\n" +
        "  gcloud auth application-default login\n"
    );
  }
  process.exit(1);
});
