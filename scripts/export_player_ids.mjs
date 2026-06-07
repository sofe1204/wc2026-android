#!/usr/bin/env node
/** Export player_id lookup tables grouped by national team. */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const config = JSON.parse(fs.readFileSync(path.join(ROOT, "project.config.json"), "utf8"));
const PLAYERS_PATH = path.join(ROOT, config.seed.android, "players_seed.json");
const TEAMS_PATH = path.join(ROOT, config.seed.android, "teams_seed.json");
const OUT_CSV = path.join(ROOT, "data", "player_ids_by_team.csv");
const OUT_DIR = path.join(ROOT, "data", "player_ids_by_team");

const COLS = [
  "country",
  "team_id",
  "team_code",
  "group",
  "shirt_number",
  "player_id",
  "player_name",
  "position",
  "reference_filename",
];

function escapeCsv(v) {
  const s = String(v ?? "");
  return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
}

function rowToLine(row) {
  return COLS.map((c) => escapeCsv(row[c])).join(",");
}

function main() {
  const players = JSON.parse(fs.readFileSync(PLAYERS_PATH, "utf8"));
  const teams = JSON.parse(fs.readFileSync(TEAMS_PATH, "utf8"));

  const teamMeta = new Map(
    teams.map((t) => [
      t.teamId,
      { countryName: t.countryName, teamCode: t.teamCode, group: t.group },
    ]),
  );

  const byTeam = new Map();
  for (const team of teams) {
    byTeam.set(team.teamId, []);
  }

  for (const p of players) {
    const meta = teamMeta.get(p.teamId);
    if (!meta) continue;

    const row = {
      country: meta.countryName,
      team_id: p.teamId,
      team_code: meta.teamCode,
      group: meta.group,
      shirt_number: p.shirtNumber,
      player_id: p.playerId,
      player_name: p.playerName,
      position: p.position,
      reference_filename: `${p.playerId}.jpg`,
    };

    if (!byTeam.has(p.teamId)) byTeam.set(p.teamId, []);
    byTeam.get(p.teamId).push(row);
  }

  fs.mkdirSync(OUT_DIR, { recursive: true });

  const masterLines = [COLS.join(",")];
  let total = 0;

  for (const team of teams) {
    const rows = byTeam.get(team.teamId) ?? [];
    rows.sort((a, b) => a.shirt_number - b.shirt_number);

    const teamLines = [COLS.join(",")];
    for (const row of rows) {
      teamLines.push(rowToLine(row));
      masterLines.push(rowToLine(row));
      total++;
    }

    const teamFile = path.join(OUT_DIR, `${team.teamId}.csv`);
    fs.writeFileSync(teamFile, teamLines.join("\n") + "\n", "utf8");
  }

  fs.writeFileSync(OUT_CSV, masterLines.join("\n") + "\n", "utf8");

  console.log(`Wrote ${total} players → ${path.relative(ROOT, OUT_CSV)}`);
  console.log(`Wrote ${teams.length} team files → ${path.relative(ROOT, OUT_DIR)}/`);
}

main();
