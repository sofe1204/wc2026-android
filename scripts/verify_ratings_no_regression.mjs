#!/usr/bin/env node
/**
 * Snapshot or verify seed ratings are not regressed after pipeline runs.
 *
 *   node scripts/verify_ratings_no_regression.mjs --snapshot
 *   node scripts/verify_ratings_no_regression.mjs
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const config = JSON.parse(fs.readFileSync(path.join(ROOT, "project.config.json"), "utf8"));
const PLAYERS_PATH = path.join(ROOT, config.seed.android, "players_seed.json");
const SNAPSHOT_PATH = path.join(ROOT, "data", ".ratings_snapshot.json");

function loadSnapshot() {
  const players = JSON.parse(fs.readFileSync(PLAYERS_PATH, "utf8"));
  const snap = {};
  for (const p of players) {
    if (!p.ratingsComplete) continue;
    snap[p.playerId] = {
      overall: p.ratings?.overall ?? 0,
      clubName: p.clubName ?? "",
      clubLeague: p.clubLeague ?? "",
    };
  }
  return snap;
}

function main() {
  const snapshot = process.argv.includes("--snapshot");

  if (snapshot) {
    const data = loadSnapshot();
    fs.writeFileSync(SNAPSHOT_PATH, JSON.stringify(data, null, 2) + "\n", "utf8");
    console.log(`Snapshot: ${Object.keys(data).length} complete players → ${path.relative(ROOT, SNAPSHOT_PATH)}`);
    return;
  }

  if (!fs.existsSync(SNAPSHOT_PATH)) {
    console.error(`No snapshot at ${SNAPSHOT_PATH}. Run with --snapshot first.`);
    process.exit(1);
  }

  const before = JSON.parse(fs.readFileSync(SNAPSHOT_PATH, "utf8"));
  const after = loadSnapshot();
  let errors = 0;

  for (const [playerId, prev] of Object.entries(before)) {
    const curr = after[playerId];
    if (!curr) {
      console.error(`REGRESSION: ${playerId} lost ratingsComplete`);
      errors++;
      continue;
    }
    if (curr.overall !== prev.overall) {
      console.error(
        `REGRESSION: ${playerId} overall ${prev.overall} → ${curr.overall}`,
      );
      errors++;
    }
  }

  const beforeCount = Object.keys(before).length;
  const afterCount = Object.keys(after).length;
  console.log(`Complete: ${beforeCount} (snapshot) → ${afterCount} (now)`);

  if (errors) {
    console.error(`FAILED: ${errors} regression(s)`);
    process.exit(1);
  }
  console.log("OK: no regressions detected");
}

main();
