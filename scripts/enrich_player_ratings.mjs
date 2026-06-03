#!/usr/bin/env node
/** Merge data/player_ratings.csv into players_seed.json (Node fallback). */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const config = JSON.parse(fs.readFileSync(path.join(ROOT, "project.config.json"), "utf8"));
const PLAYERS_PATHS = [
  path.join(ROOT, config.seed.android, "players_seed.json"),
  path.join(ROOT, config.seed.functions, "players_seed.json"),
];
const RATINGS_CSV = path.join(ROOT, "data", "player_ratings.csv");

const strict = process.argv.includes("--strict");

function parseCsv(text) {
  const lines = text.trim().split(/\r?\n/);
  const headers = lines[0].split(",").map((h) => h.trim());
  const rows = [];
  for (let i = 1; i < lines.length; i++) {
    const line = lines[i];
    if (!line.trim()) continue;
    const values = [];
    let cur = "";
    let inQuotes = false;
    for (let j = 0; j < line.length; j++) {
      const ch = line[j];
      if (inQuotes) {
        if (ch === '"' && line[j + 1] === '"') {
          cur += '"';
          j++;
        } else if (ch === '"') inQuotes = false;
        else cur += ch;
      } else if (ch === '"') inQuotes = true;
      else if (ch === ",") {
        values.push(cur);
        cur = "";
      } else cur += ch;
    }
    values.push(cur);
    const row = {};
    headers.forEach((h, idx) => {
      row[h] = values[idx] ?? "";
    });
    rows.push(row);
  }
  return rows;
}

function ratingsFromRow(row) {
  const keys = [
    "overall",
    "pace",
    "shooting",
    "passing",
    "dribbling",
    "defending",
    "physical",
    "diving",
    "handling",
    "kicking",
    "reflexes",
    "speed",
    "positioning",
  ];
  const out = {};
  for (const k of keys) out[k] = parseInt(row[k], 10) || 0;
  return out;
}

function ratingsComplete(position, ratings) {
  if (ratings.overall <= 0) return false;
  if (position === "Goalkeeper") {
    return ["diving", "handling", "kicking", "reflexes", "speed", "positioning"].every((k) => ratings[k] > 0);
  }
  return ["pace", "shooting", "passing", "dribbling", "defending", "physical"].every((k) => ratings[k] > 0);
}

function main() {
  if (!fs.existsSync(RATINGS_CSV)) {
    console.error(`Missing ${RATINGS_CSV}`);
    process.exit(1);
  }
  const byId = new Map();
  for (const row of parseCsv(fs.readFileSync(RATINGS_CSV, "utf8"))) {
    const id = row.player_id?.trim();
    if (!id) continue;
    if (byId.has(id)) throw new Error(`Duplicate player_id: ${id}`);
    byId.set(id, row);
  }

  let exitCode = 0;
  for (const playersPath of PLAYERS_PATHS) {
    const players = JSON.parse(fs.readFileSync(playersPath, "utf8"));
    const enriched = players.map((p) => {
      const row = byId.get(p.playerId);
      if (!row) {
        if (strict) {
          console.error(`Missing ratings row for ${p.playerId}`);
          exitCode = 1;
        }
        return p;
      }
      const ratings = ratingsFromRow(row);
      const complete = ratingsComplete(p.position, ratings);
      if (strict && !complete) {
        console.error(`Incomplete ratings for ${p.playerId}`);
        exitCode = 1;
      }
      return {
        ...p,
        clubName: row.club_name?.trim() ?? "",
        clubLeague: row.club_league?.trim() ?? "",
        ratings,
        ratingsComplete: complete,
      };
    });
    const completeCount = enriched.filter((p) => p.ratingsComplete).length;
    fs.writeFileSync(playersPath, JSON.stringify(enriched, null, 2) + "\n", "utf8");
    console.log(`${path.relative(ROOT, playersPath)}: ${completeCount}/${enriched.length} complete`);
  }
  process.exit(exitCode);
}

main();
