#!/usr/bin/env node
/**
 * Backfill empty rows in player_ratings.csv via strict last-name matching.
 * Complete rows are copied verbatim.
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
  loadRatingsCsv,
  ratingRowComplete,
  writeRatingsCsv,
  lastNameOf,
  positionCompatible,
  countryMatches,
  nameScore,
  allTokensMatch,
  versionScore,
  significantTokens,
  MIN_OVR_HINT,
} from "./player_ratings_lib.mjs";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const config = JSON.parse(fs.readFileSync(path.join(ROOT, "project.config.json"), "utf8"));
const PLAYERS_PATH = path.join(ROOT, config.seed.android, "players_seed.json");
const RATINGS_CSV = path.join(ROOT, "data", "player_ratings.csv");

function ovr(row) {
  return val(row, "overall_rating", "overall");
}

function passesMinOvr(player, row) {
  for (const token of significantTokens(player.playerName)) {
    const min = MIN_OVR_HINT[token];
    if (min && ovr(row) > 0 && ovr(row) < min) return false;
  }
  return true;
}

function filterCandidates(player, pool) {
  return pool.filter((row) => {
    if (!positionCompatible(player, row)) return false;
    if (countryMatches(player.countryName, row.country_name) === false) return false;
    if (!passesMinOvr(player, row)) return false;
    if (!allTokensMatch(player.playerName, row) && nameScore(player.playerName, row) < 55) {
      return false;
    }
    return true;
  });
}

function pickPassA(player, pool) {
  const cands = filterCandidates(player, pool).filter(
    (row) => nameScore(player.playerName, row) >= 80,
  );
  if (cands.length !== 1) return null;
  const row = cands[0];
  if (countryMatches(player.countryName, row.country_name) !== true) return null;
  return row;
}

function pickPassB(player, pool) {
  const cands = filterCandidates(player, pool).filter(
    (row) => allTokensMatch(player.playerName, row) || nameScore(player.playerName, row) >= 70,
  );
  if (!cands.length) return null;
  let best = null;
  let bestKey = -1;
  for (const row of cands) {
    const key = versionScore(row) * 1000 + ovr(row);
    if (key > bestKey) {
      bestKey = key;
      best = row;
    }
  }
  return best;
}

async function main() {
  console.log("Downloading SoFIFA dataset …");
  const res = await fetch(SOFIFA_URL);
  if (!res.ok) throw new Error(`Download failed: ${res.status}`);
  const sofifaRows = parseCsv(await res.text());
  const index = buildSofifaIndex(sofifaRows);

  const players = JSON.parse(fs.readFileSync(PLAYERS_PATH, "utf8"));
  const byId = loadRatingsCsv(RATINGS_CSV);
  const playerById = new Map(players.map((p) => [p.playerId, p]));

  const outRows = [];
  let filledA = 0;
  let filledB = 0;
  let preserved = 0;
  let stillEmpty = 0;

  for (const player of players) {
    const existing = byId.get(player.playerId);
    if (existing && ratingRowComplete(existing, player.position)) {
      outRows.push(existing);
      preserved++;
      continue;
    }

    const last = lastNameOf(player.playerName);
    const pool =
      last.length >= 3 && index.byLastName.has(last) ? index.byLastName.get(last) : [];

    let hit = pickPassA(player, pool);
    if (hit) filledA++;
    else {
      hit = pickPassB(player, pool);
      if (hit) filledB++;
    }

    if (hit && ovr(hit) > 0) {
      outRows.push(rowToRating(player, hit));
    } else if (existing) {
      outRows.push(existing);
      stillEmpty++;
    } else {
      outRows.push(
        Object.fromEntries(
          ["player_id", "club_name", "club_league", "overall", "pace", "shooting", "passing", "dribbling", "defending", "physical", "diving", "handling", "kicking", "reflexes", "speed", "positioning"].map(
            (c) => [c, c === "player_id" ? player.playerId : ""],
          ),
        ),
      );
      stillEmpty++;
    }
  }

  writeRatingsCsv(RATINGS_CSV, outRows);
  console.log(
    `Preserved ${preserved}, filled Pass A: ${filledA}, Pass B: ${filledB}, still empty: ${stillEmpty}`,
  );
  console.log(`Wrote ${path.relative(ROOT, RATINGS_CSV)}`);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
