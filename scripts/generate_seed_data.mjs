#!/usr/bin/env node
/**
 * Node port of generate_seed_data.py — rebuilds teams/players/stickers seed JSON.
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { CORE } from "./squads_core.mjs";
import { EXTENDED } from "./squads_extended.mjs";
import { emptyRatings, playerIdFor, squadTuple, tuplesToSquad, writeSquadsCsv } from "./seed_lib.mjs";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const config = JSON.parse(fs.readFileSync(path.join(ROOT, "project.config.json"), "utf8"));
const OUTPUT = path.join(ROOT, config.seed.android);
const FUNCTIONS_SEED = path.join(ROOT, config.seed.functions);
const SQUADS_CSV = path.join(ROOT, "data", "squads.csv");

const TEAMS = [
  ["mexico", "Mexico", "Group A", "MEX", "🇲🇽", "#006847", "#FFFFFF"],
  ["south_africa", "South Africa", "Group A", "RSA", "🇿🇦", "#007A4D", "#FFB612"],
  ["south_korea", "South Korea", "Group A", "KOR", "🇰🇷", "#CD2E3A", "#0047A0"],
  ["czechia", "Czechia", "Group A", "CZE", "🇨🇿", "#11457E", "#D7141A"],
  ["canada", "Canada", "Group B", "CAN", "🇨🇦", "#FF0000", "#FFFFFF"],
  ["switzerland", "Switzerland", "Group B", "SUI", "🇨🇭", "#FF0000", "#FFFFFF"],
  ["qatar", "Qatar", "Group B", "QAT", "🇶🇦", "#8D1B3D", "#FFFFFF"],
  ["bosnia_herzegovina", "Bosnia and Herzegovina", "Group B", "BIH", "🇧🇦", "#002395", "#FECB00"],
  ["brazil", "Brazil", "Group C", "BRA", "🇧🇷", "#009C3B", "#FFDF00"],
  ["morocco", "Morocco", "Group C", "MAR", "🇲🇦", "#C1272D", "#006233"],
  ["scotland", "Scotland", "Group C", "SCO", "🏴󠁧󠁢󠁳󠁣󠁴󠁿", "#005EB8", "#FFFFFF"],
  ["haiti", "Haiti", "Group C", "HAI", "🇭🇹", "#00209F", "#D21034"],
  ["united_states", "United States", "Group D", "USA", "🇺🇸", "#3C3B6E", "#B22234"],
  ["paraguay", "Paraguay", "Group D", "PAR", "🇵🇾", "#D52B1E", "#0038A8"],
  ["australia", "Australia", "Group D", "AUS", "🇦🇺", "#00008B", "#FFCD00"],
  ["turkiye", "Türkiye", "Group D", "TUR", "🇹🇷", "#E30A17", "#FFFFFF"],
  ["germany", "Germany", "Group E", "GER", "🇩🇪", "#000000", "#DD0000"],
  ["ecuador", "Ecuador", "Group E", "ECU", "🇪🇨", "#FFD100", "#0033A0"],
  ["ivory_coast", "Ivory Coast", "Group E", "CIV", "🇨🇮", "#F77F00", "#009E60"],
  ["curacao", "Curaçao", "Group E", "CUW", "🇨🇼", "#002B7F", "#F9E814"],
  ["netherlands", "Netherlands", "Group F", "NED", "🇳🇱", "#FF6600", "#21468B"],
  ["japan", "Japan", "Group F", "JPN", "🇯🇵", "#BC002D", "#FFFFFF"],
  ["tunisia", "Tunisia", "Group F", "TUN", "🇹🇳", "#E70013", "#FFFFFF"],
  ["sweden", "Sweden", "Group F", "SWE", "🇸🇪", "#006AA7", "#FECC00"],
  ["belgium", "Belgium", "Group G", "BEL", "🇧🇪", "#000000", "#FAE042"],
  ["iran", "Iran", "Group G", "IRN", "🇮🇷", "#239F40", "#FFFFFF"],
  ["egypt", "Egypt", "Group G", "EGY", "🇪🇬", "#CE1126", "#FFFFFF"],
  ["new_zealand", "New Zealand", "Group G", "NZL", "🇳🇿", "#00247D", "#FFFFFF"],
  ["spain", "Spain", "Group H", "ESP", "🇪🇸", "#AA151B", "#F1BF00"],
  ["uruguay", "Uruguay", "Group H", "URU", "🇺🇾", "#0038A8", "#FFFFFF"],
  ["saudi_arabia", "Saudi Arabia", "Group H", "KSA", "🇸🇦", "#006C35", "#FFFFFF"],
  ["cape_verde", "Cape Verde", "Group H", "CPV", "🇨🇻", "#003893", "#FFFFFF"],
  ["france", "France", "Group I", "FRA", "🇫🇷", "#0055A4", "#EF4135"],
  ["senegal", "Senegal", "Group I", "SEN", "🇸🇳", "#00853F", "#FDEF42"],
  ["norway", "Norway", "Group I", "NOR", "🇳🇴", "#BA0C2F", "#00205B"],
  ["iraq", "Iraq", "Group I", "IRQ", "🇮🇶", "#CE1126", "#FFFFFF"],
  ["argentina", "Argentina", "Group J", "ARG", "🇦🇷", "#74ACDF", "#FFFFFF"],
  ["austria", "Austria", "Group J", "AUT", "🇦🇹", "#ED2939", "#FFFFFF"],
  ["algeria", "Algeria", "Group J", "ALG", "🇩🇿", "#006233", "#FFFFFF"],
  ["jordan", "Jordan", "Group J", "JOR", "🇯🇴", "#007A3D", "#FFFFFF"],
  ["portugal", "Portugal", "Group K", "POR", "🇵🇹", "#006600", "#FF0000"],
  ["colombia", "Colombia", "Group K", "COL", "🇨🇴", "#FCD116", "#003893"],
  ["uzbekistan", "Uzbekistan", "Group K", "UZB", "🇺🇿", "#1EB53A", "#FFFFFF"],
  ["dr_congo", "DR Congo", "Group K", "COD", "🇨🇩", "#007FFF", "#F7D618"],
  ["england", "England", "Group L", "ENG", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "#FFFFFF", "#CE1141"],
  ["croatia", "Croatia", "Group L", "CRO", "🇭🇷", "#FF0000", "#FFFFFF"],
  ["panama", "Panama", "Group L", "PAN", "🇵🇦", "#005293", "#DC143C"],
  ["ghana", "Ghana", "Group L", "GHA", "🇬🇭", "#006B3F", "#FCD116"],
];

function buildAllSquads() {
  const merged = { ...CORE, ...EXTENDED };
  const out = {};
  for (const [teamId, tuples] of Object.entries(merged)) {
    out[teamId] = tuplesToSquad(tuples);
  }
  return out;
}

function animePrompt(playerName, country, shirt, position) {
  return (
    `Anime-style collectible football sticker portrait of ${playerName} as an inspired stylized character, ` +
    `not copied from a photo, wearing a generic ${country}-inspired football shirt number ${shirt}, ` +
    `${position} identity, confident expression, stadium lights, vibrant card border, clean line art.`
  );
}

function main() {
  const allSquads = buildAllSquads();
  writeSquadsCsv(SQUADS_CSV, allSquads);

  const teamsOut = [];
  const playersOut = [];
  const stickersOut = [];

  for (const [teamId, country, group, code, flag, primary, secondary] of TEAMS) {
    if (!allSquads[teamId]) {
      throw new Error(`Missing squad for team_id=${teamId}`);
    }
    teamsOut.push({
      teamId,
      countryName: country,
      group,
      teamCode: code,
      flagEmoji: flag,
      customEmblemUrl: "",
      primaryColor: primary,
      secondaryColor: secondary,
      isActive: true,
    });
    stickersOut.push({
      stickerId: `${code}-000`,
      stickerNumber: 0,
      playerId: "",
      teamId,
      countryName: country,
      group,
      rarity: "epic",
      imageUrl: "",
      isActive: true,
    });

    const namesSeen = new Set();
    for (const row of allSquads[teamId]) {
      const { shirt_number: shirt, player_name: pname, position: pos, rarity } = row;
      const playerId = playerIdFor(teamId, pname);
      if (namesSeen.has(playerId)) {
        throw new Error(`Duplicate player name in ${teamId}: ${pname} -> ${playerId}`);
      }
      namesSeen.add(playerId);
      const ratings = emptyRatings();
      playersOut.push({
        playerId,
        teamId,
        countryName: country,
        group,
        shirtNumber: shirt,
        playerName: pname,
        position: pos,
        rarity,
        animeStickerPrompt: animePrompt(pname, country, shirt, pos),
        imageUrl: "",
        clubName: "",
        clubLeague: "",
        ratings,
        ratingsComplete: false,
        isActive: true,
      });
      stickersOut.push({
        stickerId: `${code}-${String(shirt).padStart(3, "0")}`,
        stickerNumber: shirt,
        playerId,
        teamId,
        countryName: country,
        group,
        rarity,
        imageUrl: "",
        isActive: true,
      });
    }
  }

  if (teamsOut.length !== 48 || playersOut.length !== 720 || stickersOut.length !== 768) {
    throw new Error(`Counts: teams=${teamsOut.length} players=${playersOut.length} stickers=${stickersOut.length}`);
  }

  for (const dir of [OUTPUT, FUNCTIONS_SEED]) {
    fs.mkdirSync(dir, { recursive: true });
    fs.writeFileSync(path.join(dir, "teams_seed.json"), JSON.stringify(teamsOut, null, 2) + "\n", "utf8");
    fs.writeFileSync(path.join(dir, "players_seed.json"), JSON.stringify(playersOut, null, 2) + "\n", "utf8");
    fs.writeFileSync(path.join(dir, "stickers_seed.json"), JSON.stringify(stickersOut, null, 2) + "\n", "utf8");
  }

  console.log(`Generated 48 teams, 720 players, 768 stickers`);
  console.log(`Wrote ${path.relative(ROOT, SQUADS_CSV)}`);
}

main();
