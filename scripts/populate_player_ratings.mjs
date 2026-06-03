#!/usr/bin/env node
/**
 * Auto-fill data/player_ratings.csv from SoFIFA open-data CSV, then optional overrides.
 * Default: --fill-only preserves existing complete rows in player_ratings.csv.
 *
 *   node scripts/populate_player_ratings.mjs
 *   node scripts/populate_player_ratings.mjs --enrich
 *   node scripts/populate_player_ratings.mjs --force   # destructive full rebuild
 */
import fs from "fs";
import path from "path";
import { spawnSync } from "child_process";
import { fileURLToPath } from "url";
import {
  SOFIFA_URL,
  RATINGS_CSV_COLUMNS,
  parseCsv,
  val,
  rowToRating,
  emptyRatingRow,
  buildSofifaIndex,
  findBestMatch,
  isPlaceholderPlayer,
  loadRatingsCsv,
  ratingRowComplete,
  writeRatingsCsv,
  mergeOverrideEmptyOnly,
} from "./player_ratings_lib.mjs";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const config = JSON.parse(fs.readFileSync(path.join(ROOT, "project.config.json"), "utf8"));
const PLAYERS_PATH = path.join(ROOT, config.seed.android, "players_seed.json");
const RATINGS_CSV = path.join(ROOT, "data", "player_ratings.csv");
const OVERRIDES_PATH = path.join(ROOT, "data", "ratings_overrides.json");

const runEnrich = process.argv.includes("--enrich");
const forceRebuild = process.argv.includes("--force");
const fillOnly = !forceRebuild;
const backup = process.argv.includes("--backup");

function loadOverrides() {
  if (!fs.existsSync(OVERRIDES_PATH)) return new Map();
  const raw = JSON.parse(fs.readFileSync(OVERRIDES_PATH, "utf8"));
  const map = new Map();
  for (const [playerId, row] of Object.entries(raw)) {
    map.set(playerId, { player_id: playerId, ...row });
  }
  return map;
}

async function main() {
  if (backup && fs.existsSync(RATINGS_CSV)) {
    const bak = `${RATINGS_CSV}.${Date.now()}.bak`;
    fs.copyFileSync(RATINGS_CSV, bak);
    console.log(`Backed up ${path.relative(ROOT, RATINGS_CSV)} → ${path.relative(ROOT, bak)}`);
  }

  const existingById = fillOnly ? loadRatingsCsv(RATINGS_CSV) : new Map();

  console.log("Downloading SoFIFA dataset …");
  const res = await fetch(SOFIFA_URL);
  if (!res.ok) throw new Error(`Download failed: ${res.status}`);
  const sofifaRows = parseCsv(await res.text());
  console.log(`Loaded ${sofifaRows.length} SoFIFA rows`);

  const players = JSON.parse(fs.readFileSync(PLAYERS_PATH, "utf8"));
  const index = buildSofifaIndex(sofifaRows);
  const overrides = loadOverrides();

  let matched = 0;
  let preserved = 0;
  let overridden = 0;
  let skippedPlaceholder = 0;
  const outRows = [];
  const unmatched = [];

  for (const player of players) {
    if (isPlaceholderPlayer(player)) {
      skippedPlaceholder++;
      outRows.push(emptyRatingRow(player.playerId));
      continue;
    }

    const existing = existingById.get(player.playerId);
    if (fillOnly && existing && ratingRowComplete(existing, player.position)) {
      outRows.push(existing);
      preserved++;
      continue;
    }

    const hit = findBestMatch(player, index);
    let row =
      hit && val(hit, "overall_rating", "overall") > 0
        ? rowToRating(player, hit)
        : existing
          ? { ...existing, player_id: player.playerId }
          : emptyRatingRow(player.playerId);
    if (hit && val(hit, "overall_rating", "overall") > 0) matched++;

    if (overrides.has(player.playerId)) {
      const beforeComplete = ratingRowComplete(row, player.position);
      row = mergeOverrideEmptyOnly(row, overrides.get(player.playerId));
      if (!beforeComplete && ratingRowComplete(row, player.position)) overridden++;
    }

    outRows.push(row);
    if (!ratingRowComplete(row, player.position)) unmatched.push(player.playerId);
  }

  writeRatingsCsv(RATINGS_CSV, outRows);

  const named = players.length - skippedPlaceholder;
  console.log(
    `Matched ${matched} new, preserved ${preserved} existing (${fillOnly ? "fill-only" : "force rebuild"})`,
  );
  if (overridden) console.log(`Overrides filled ${overridden} previously incomplete row(s)`);
  console.log(`Wrote ${path.relative(ROOT, RATINGS_CSV)}`);
  if (unmatched.length) {
    console.log(`Still empty (${unmatched.length}): ${unmatched.slice(0, 12).join(", ")}…`);
  }

  if (runEnrich) {
    const enrich = path.join(ROOT, "scripts", "enrich_player_ratings.mjs");
    console.log("\nRunning enrich …");
    const result = spawnSync(process.execPath, [enrich], { stdio: "inherit", cwd: ROOT });
    process.exit(result.status ?? 1);
  }
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
