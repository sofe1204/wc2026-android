#!/usr/bin/env node
/** Export players missing complete ratings to data/unmatched_players.csv */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { loadRatingsCsv, ratingRowComplete } from "./player_ratings_lib.mjs";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const config = JSON.parse(fs.readFileSync(path.join(ROOT, "project.config.json"), "utf8"));
const PLAYERS_PATH = path.join(ROOT, config.seed.android, "players_seed.json");
const RATINGS_CSV = path.join(ROOT, "data", "player_ratings.csv");
const OUT_PATH = path.join(ROOT, "data", "unmatched_players.csv");

function escapeCsv(v) {
  const s = String(v ?? "");
  return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
}

function main() {
  const players = JSON.parse(fs.readFileSync(PLAYERS_PATH, "utf8"));
  const csvById = loadRatingsCsv(RATINGS_CSV);

  const cols = [
    "player_id",
    "player_name",
    "country",
    "position",
    "team_id",
    "shirt_number",
    "reason",
  ];
  const lines = [cols.join(",")];

  let count = 0;
  for (const p of players) {
    let reason = "";
    if (p.ratingsComplete) continue;

    const row = csvById.get(p.playerId);
    if (!row) reason = "no_csv_row";
    else if (!ratingRowComplete(row, p.position)) reason = "incomplete_csv";
    else reason = "incomplete_seed";

    lines.push(
      cols.map((c) => escapeCsv({
        player_id: p.playerId,
        player_name: p.playerName,
        country: p.countryName,
        position: p.position,
        team_id: p.teamId,
        shirt_number: p.shirtNumber,
        reason,
      }[c])).join(","),
    );
    count++;
  }

  fs.mkdirSync(path.dirname(OUT_PATH), { recursive: true });
  fs.writeFileSync(OUT_PATH, lines.join("\n") + "\n", "utf8");
  console.log(`Wrote ${count} unmatched → ${path.relative(ROOT, OUT_PATH)}`);
}

main();
