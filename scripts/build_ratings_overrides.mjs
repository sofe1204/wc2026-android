#!/usr/bin/env node
/**
 * Look up priority players in SoFIFA and merge into data/ratings_overrides.json.
 * Only adds entries for players still incomplete in player_ratings.csv.
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import {
  SOFIFA_URL,
  parseCsv,
  val,
  rowToRating,
  buildSofifaIndex,
  findBestMatch,
  loadRatingsCsv,
  ratingRowComplete,
  significantTokens,
  allTokensMatch,
  positionCompatible,
  countryMatches,
  nameScore,
  versionScore,
  lastNameOf,
  MIN_OVR_HINT,
} from "./player_ratings_lib.mjs";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const config = JSON.parse(fs.readFileSync(path.join(ROOT, "project.config.json"), "utf8"));
const PLAYERS_PATH = path.join(ROOT, config.seed.android, "players_seed.json");
const RATINGS_CSV = path.join(ROOT, "data", "player_ratings.csv");
const OUT_PATH = path.join(ROOT, "data", "ratings_overrides.json");
const UNMATCHED_PATH = path.join(ROOT, "data", "unmatched_players.csv");

const DEFAULT_PRIORITY = [
  "mexico_andres_guardado",
  "mexico_rodolfo_cota",
  "mexico_hector_moreno",
  "mexico_alexis_vega",
  "canada_milan_borjan",
  "canada_lucas_cavallini",
];

function ovr(row) {
  return val(row, "overall_rating", "overall");
}

function findWithAliases(player, index) {
  let hit = findBestMatch(player, index);
  if (hit && ovr(hit) > 0) return hit;

  const attempts = [
    player.playerName,
    significantTokens(player.playerName).join(" "),
    lastNameOf(player.playerName),
  ];
  for (const label of attempts) {
    if (!label || label.length < 3) continue;
    const probe = { ...player, playerName: label };
    hit = findBestMatch(probe, index);
    if (hit && ovr(hit) > 0) return hit;
  }

  const last = lastNameOf(player.playerName);
  const pool = index.byLastName.get(last) ?? [];
  let best = null;
  let bestScore = 0;
  for (const row of pool) {
    if (!positionCompatible(player, row)) continue;
    if (countryMatches(player.countryName, row.country_name) === false) continue;
    if (!allTokensMatch(player.playerName, row) && nameScore(player.playerName, row) < 80) continue;
    for (const token of significantTokens(player.playerName)) {
      const min = MIN_OVR_HINT[token];
      if (min && ovr(row) > 0 && ovr(row) < min) continue;
    }
    const s = nameScore(player.playerName, row) + versionScore(row) * 8 + ovr(row);
    if (s > bestScore) {
      bestScore = s;
      best = row;
    }
  }
  return best && ovr(best) > 0 ? best : null;
}

function loadPriorityIds(players) {
  const ids = new Set(DEFAULT_PRIORITY);
  if (fs.existsSync(UNMATCHED_PATH)) {
    const text = fs.readFileSync(UNMATCHED_PATH, "utf8");
    const lines = text.trim().split("\n").slice(1);
    for (const line of lines) {
      const [playerId, , country] = line.split(",");
      if (country?.includes("brazil") || country?.includes("Brazil")) ids.add(playerId.replace(/^"|"$/g, ""));
    }
  }
  return [...ids].filter((id) => players.some((p) => p.playerId === id));
}

async function main() {
  console.log("Downloading SoFIFA dataset …");
  const res = await fetch(SOFIFA_URL);
  if (!res.ok) throw new Error(`Download failed: ${res.status}`);
  const sofifaRows = parseCsv(await res.text());
  const index = buildSofifaIndex(sofifaRows);

  const players = JSON.parse(fs.readFileSync(PLAYERS_PATH, "utf8"));
  const csvById = loadRatingsCsv(RATINGS_CSV);
  const existing =
    fs.existsSync(OUT_PATH) ? JSON.parse(fs.readFileSync(OUT_PATH, "utf8")) : {};

  const priorityIds = loadPriorityIds(players);
  let added = 0;
  let skipped = 0;

  for (const playerId of priorityIds) {
    const player = players.find((p) => p.playerId === playerId);
    if (!player) continue;

    const csvRow = csvById.get(playerId);
    if (csvRow && ratingRowComplete(csvRow, player.position)) {
      skipped++;
      continue;
    }

    const hit = findWithAliases(player, index);
    if (!hit || ovr(hit) <= 0) {
      console.log(`No SoFIFA row: ${playerId} (${player.playerName})`);
      continue;
    }

    const rating = rowToRating(player, hit);
    const { player_id, ...stats } = rating;
    existing[playerId] = stats;
    added++;
    console.log(`Override ${playerId}: ${stats.club_name} OVR ${stats.overall}`);
  }

  fs.mkdirSync(path.dirname(OUT_PATH), { recursive: true });
  fs.writeFileSync(OUT_PATH, JSON.stringify(existing, null, 2) + "\n", "utf8");
  console.log(`Wrote ${path.relative(ROOT, OUT_PATH)} (+${added} new, ${skipped} already complete)`);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
