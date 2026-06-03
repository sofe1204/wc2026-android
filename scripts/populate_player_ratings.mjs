#!/usr/bin/env node
/**
 * Auto-fill data/player_ratings.csv from SoFIFA open-data CSV, then optional overrides.
 *
 *   node scripts/populate_player_ratings.mjs
 *   node scripts/populate_player_ratings.mjs --enrich   # also merge into players_seed.json
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
  escapeCsv,
  buildSofifaIndex,
  findBestMatch,
  isPlaceholderPlayer,
} from "./player_ratings_lib.mjs";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const config = JSON.parse(fs.readFileSync(path.join(ROOT, "project.config.json"), "utf8"));
const PLAYERS_PATH = path.join(ROOT, config.seed.android, "players_seed.json");
const RATINGS_CSV = path.join(ROOT, "data", "player_ratings.csv");
const OVERRIDES_PATH = path.join(ROOT, "data", "ratings_overrides.json");

const runEnrich = process.argv.includes("--enrich");

function loadOverrides() {
  if (!fs.existsSync(OVERRIDES_PATH)) return new Map();
  const raw = JSON.parse(fs.readFileSync(OVERRIDES_PATH, "utf8"));
  const map = new Map();
  for (const [playerId, row] of Object.entries(raw)) {
    map.set(playerId, { player_id: playerId, ...row });
  }
  return map;
}

function applyOverride(base, override) {
  const out = { ...base };
  for (const col of RATINGS_CSV_COLUMNS) {
    if (col === "player_id") continue;
    const v = override[col];
    if (v !== undefined && v !== null && String(v).trim() !== "") out[col] = v;
  }
  return out;
}

async function main() {
  console.log("Downloading SoFIFA dataset …");
  const res = await fetch(SOFIFA_URL);
  if (!res.ok) throw new Error(`Download failed: ${res.status}`);
  const sofifaRows = parseCsv(await res.text());
  console.log(`Loaded ${sofifaRows.length} SoFIFA rows`);

  const players = JSON.parse(fs.readFileSync(PLAYERS_PATH, "utf8"));
  const index = buildSofifaIndex(sofifaRows);
  const overrides = loadOverrides();

  let matched = 0;
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

    const hit = findBestMatch(player, index);
    let row =
      hit && val(hit, "overall_rating", "overall") > 0
        ? rowToRating(player, hit)
        : emptyRatingRow(player.playerId);
    if (hit && val(hit, "overall_rating", "overall") > 0) matched++;

    if (overrides.has(player.playerId)) {
      row = applyOverride(row, overrides.get(player.playerId));
      overridden++;
    }

    outRows.push(row);
    if (!row.overall || parseInt(row.overall, 10) <= 0) unmatched.push(player.playerId);
  }

  const lines = [RATINGS_CSV_COLUMNS.join(",")];
  for (const r of outRows) {
    lines.push(RATINGS_CSV_COLUMNS.map((c) => escapeCsv(r[c])).join(","));
  }
  fs.mkdirSync(path.dirname(RATINGS_CSV), { recursive: true });
  fs.writeFileSync(RATINGS_CSV, lines.join("\n") + "\n", "utf8");

  const named = players.length - skippedPlaceholder;
  console.log(`Matched ${matched}/${named} named players (${skippedPlaceholder} placeholders skipped)`);
  if (overridden) console.log(`Applied ${overridden} override(s) from data/ratings_overrides.json`);
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
