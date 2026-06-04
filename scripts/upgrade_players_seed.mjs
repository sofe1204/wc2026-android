#!/usr/bin/env node
/**
 * Add club + ratings fields to players_seed.json and export data/*.csv templates.
 * Node fallback when Python is unavailable.
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const config = JSON.parse(fs.readFileSync(path.join(ROOT, "project.config.json"), "utf8"));
const PLAYERS_ANDROID = path.join(ROOT, config.seed.android, "players_seed.json");
const PLAYERS_FUNCTIONS = path.join(ROOT, config.seed.functions, "players_seed.json");
const SQUADS_CSV = path.join(ROOT, "data", "squads.csv");
const RATINGS_CSV = path.join(ROOT, "data", "player_ratings.csv");

const EMPTY_RATINGS = {
  overall: 0,
  pace: 0,
  shooting: 0,
  passing: 0,
  dribbling: 0,
  defending: 0,
  physical: 0,
  diving: 0,
  handling: 0,
  kicking: 0,
  reflexes: 0,
  speed: 0,
  positioning: 0,
};

function escapeCsv(value) {
  const s = String(value ?? "");
  if (/[",\n]/.test(s)) return `"${s.replace(/"/g, '""')}"`;
  return s;
}

function writeCsv(filePath, columns, rows) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
  const lines = [columns.join(",")];
  for (const row of rows) {
    lines.push(columns.map((c) => escapeCsv(row[c])).join(","));
  }
  fs.writeFileSync(filePath, lines.join("\n") + "\n", "utf8");
}

function upgradePlayer(p) {
  return {
    ...p,
    clubName: p.clubName ?? "",
    clubLeague: p.clubLeague ?? "",
    clubLogoUrl: p.clubLogoUrl ?? "",
    ratings: { ...EMPTY_RATINGS, ...(p.ratings ?? {}) },
    ratingsComplete: p.ratingsComplete ?? false,
  };
}

function main() {
  const raw = fs.readFileSync(PLAYERS_ANDROID, "utf8");
  const players = JSON.parse(raw).map(upgradePlayer);
  const json = JSON.stringify(players, null, 2) + "\n";
  fs.writeFileSync(PLAYERS_ANDROID, json, "utf8");
  fs.writeFileSync(PLAYERS_FUNCTIONS, json, "utf8");

  const squadRows = players
    .slice()
    .sort((a, b) => a.teamId.localeCompare(b.teamId) || a.shirtNumber - b.shirtNumber)
    .map((p) => ({
      team_id: p.teamId,
      shirt_number: p.shirtNumber,
      player_name: p.playerName,
      position: p.position,
      rarity: p.rarity,
    }));

  const ratingRows = players.map((p) => ({
    player_id: p.playerId,
    club_name: p.clubName ?? "",
    club_league: p.clubLeague ?? "",
    club_logo_url: p.clubLogoUrl ?? "",
    overall: p.ratings?.overall || "",
    pace: p.ratings?.pace || "",
    shooting: p.ratings?.shooting || "",
    passing: p.ratings?.passing || "",
    dribbling: p.ratings?.dribbling || "",
    defending: p.ratings?.defending || "",
    physical: p.ratings?.physical || "",
    diving: p.ratings?.diving || "",
    handling: p.ratings?.handling || "",
    kicking: p.ratings?.kicking || "",
    reflexes: p.ratings?.reflexes || "",
    speed: p.ratings?.speed || "",
    positioning: p.ratings?.positioning || "",
  }));

  writeCsv(SQUADS_CSV, ["team_id", "shirt_number", "player_name", "position", "rarity"], squadRows);
  writeCsv(
    RATINGS_CSV,
    [
      "player_id",
      "club_name",
      "club_league",
      "club_logo_url",
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
    ],
    ratingRows,
  );

  console.log(`Upgraded ${players.length} players in seed JSON`);
  console.log(`Wrote ${path.relative(ROOT, SQUADS_CSV)}`);
  console.log(`Wrote ${path.relative(ROOT, RATINGS_CSV)}`);
}

main();
