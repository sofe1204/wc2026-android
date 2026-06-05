#!/usr/bin/env node
/**
 * Refresh club_name from Wikipedia FIFA 2026 squads (official_squads_2026.json).
 * League + logo from SoFIFA club index. Preserves ratings in player_ratings.csv.
 *
 *   npm run import:official-squads   # re-fetch Wikipedia squads + clubs
 *   npm run sync:clubs
 */
import fs from "fs";
import path from "path";
import { spawnSync } from "child_process";
import { fileURLToPath } from "url";
import {
  SOFIFA_URL,
  applyOfficialClub,
  buildSofifaClubIndex,
  loadOfficialClubIndexes,
  officialClubFor,
  loadRatingsCsv,
  parseCsv,
  writeRatingsCsv,
} from "./player_ratings_lib.mjs";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const config = JSON.parse(fs.readFileSync(path.join(ROOT, "project.config.json"), "utf8"));
const PLAYERS_PATH = path.join(ROOT, config.seed.android, "players_seed.json");
const RATINGS_CSV = path.join(ROOT, "data", "player_ratings.csv");
const runEnrich = !process.argv.includes("--no-enrich");

async function main() {
  const officialIndexes = loadOfficialClubIndexes(ROOT);
  if (officialIndexes.byTeamShirt.size === 0) {
    console.error("No clubs in data/official_squads_2026.json — run: npm run import:official-squads");
    process.exit(1);
  }

  const players = JSON.parse(fs.readFileSync(PLAYERS_PATH, "utf8"));
  const existingById = loadRatingsCsv(RATINGS_CSV);

  console.log("Downloading SoFIFA dataset (club league/logo lookup) …");
  const res = await fetch(SOFIFA_URL);
  if (!res.ok) throw new Error(`Download failed: ${res.status}`);
  const clubIndex = buildSofifaClubIndex(parseCsv(await res.text()));

  let updated = 0;
  let missingOfficial = 0;
  const outRows = [];

  for (const player of players) {
    let row = existingById.get(player.playerId);
    if (!row) {
      row = { player_id: player.playerId };
    }
    const before = row.club_name?.trim() ?? "";
    row = applyOfficialClub(row, player, officialIndexes, clubIndex);
    if (row.club_name?.trim() && row.club_name !== before) updated++;
    if (!officialClubFor(player, officialIndexes)) missingOfficial++;
    outRows.push(row);
  }

  writeRatingsCsv(RATINGS_CSV, outRows);
  console.log(`Wikipedia clubs in source: ${officialIndexes.byTeamShirt.size} by shirt`);
  console.log(`Updated club_name on ${updated} row(s) in ${path.relative(ROOT, RATINGS_CSV)}`);
  if (missingOfficial) {
    console.log(`No Wikipedia club for ${missingOfficial} seed player(s) (name/shirt mismatch?)`);
  }

  if (runEnrich) {
    console.log("\nRunning enrich_player_ratings.mjs …");
    const enrich = path.join(ROOT, "scripts", "enrich_player_ratings.mjs");
    const result = spawnSync(process.execPath, [enrich], { stdio: "inherit", cwd: ROOT });
    process.exit(result.status ?? 1);
  }
}

main().catch((e) => {
  console.error(e.message || e);
  process.exit(1);
});
