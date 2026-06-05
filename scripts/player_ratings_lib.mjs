/**
 * Shared helpers for SoFIFA CSV parsing and face-stat computation.
 */
import fs from "fs";
import path from "path";
import { playerIdFor } from "./seed_lib.mjs";

export const SOFIFA_URL =
  "https://github.com/SolideSpoke/sofifa-web-scraper/raw/main/output/player-data-full.csv";

export const RATINGS_CSV_COLUMNS = [
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
];

export const COUNTRY_ALIASES = {
  "united states": ["united states", "usa"],
  czechia: ["czechia", "czech republic"],
  turkiye: ["turkiye", "turkey"],
  "dr congo": ["dr congo", "congo dr", "democratic republic of the congo"],
  "ivory coast": ["ivory coast", "cote divoire", "cote d ivoire"],
  "south korea": ["south korea", "korea republic"],
  "bosnia and herzegovina": ["bosnia and herzegovina", "bosnia herzegovina"],
  "cape verde": ["cape verde", "cape verde islands"],
};

/** Reject wrong homonym matches when a star's SoFIFA OVR is far below expected. */
export const TOKEN_ALIASES = {
  vinicius: ["vini"],
  vini: ["vinicius"],
  neymar: ["neymar jr"],
  mbappe: ["kylian"],
  kylian: ["mbappe"],
  messi: ["lionel"],
  lionel: ["messi"],
  cristiano: ["cristiano ronaldo"],
  ronaldo: ["cristiano"],
  hector: ["moreno"],
  moreno: ["hector"],
  edson: ["alvarez"],
  alvarez: ["edson"],
};

export const MIN_OVR_HINT = {
  vinicius: 84,
  neymar: 85,
  messi: 86,
  ronaldo: 85,
  mbappe: 88,
  haaland: 88,
  bellingham: 84,
  rodrygo: 80,
  marquinhos: 84,
  casemiro: 82,
  raphinha: 80,
  guardado: 70,
  dzeko: 78,
  jimenez: 75,
};

const TOP_LEAGUE_HINTS = [
  "premier league",
  "la liga",
  "serie a",
  "bundesliga",
  "ligue 1",
  "eredivisie",
  "primeira liga",
];

const RATING_STAT_KEYS = [
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

export function ratingsFromCsvRow(row) {
  const out = {};
  for (const k of RATING_STAT_KEYS) out[k] = parseInt(row[k], 10) || 0;
  return out;
}

export function ratingsCompleteForPosition(position, ratings) {
  if (ratings.overall <= 0) return false;
  if (position === "Goalkeeper") {
    return ["diving", "handling", "kicking", "reflexes", "speed", "positioning"].every(
      (k) => ratings[k] > 0,
    );
  }
  return ["pace", "shooting", "passing", "dribbling", "defending", "physical"].every(
    (k) => ratings[k] > 0,
  );
}

export function ratingRowComplete(row, position) {
  return ratingsCompleteForPosition(position, ratingsFromCsvRow(row));
}

export function loadRatingsCsv(filePath) {
  if (!fs.existsSync(filePath)) return new Map();
  const text = fs.readFileSync(filePath, "utf8");
  const byId = new Map();
  for (const row of parseCsv(text)) {
    const id = row.player_id?.trim();
    if (id) byId.set(id, row);
  }
  return byId;
}

export function normalizeRatingRow(row) {
  return Object.fromEntries(RATINGS_CSV_COLUMNS.map((c) => [c, String(row?.[c] ?? "").trim()]));
}

export function writeRatingsCsv(filePath, rows) {
  const lines = [RATINGS_CSV_COLUMNS.join(",")];
  for (const r of rows) {
    const normalized = normalizeRatingRow(r);
    lines.push(RATINGS_CSV_COLUMNS.map((c) => escapeCsv(normalized[c])).join(","));
  }
  const dir = path.dirname(filePath);
  if (dir) fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(filePath, lines.join("\n") + "\n", "utf8");
}

/** Merge override into base without overwriting non-empty fields. */
export function mergeOverrideEmptyOnly(base, override) {
  const out = { ...base };
  for (const col of RATINGS_CSV_COLUMNS) {
    if (col === "player_id") continue;
    const existing = String(out[col] ?? "").trim();
    const incoming = override[col];
    if (existing !== "" && existing !== "0") continue;
    if (incoming !== undefined && incoming !== null && String(incoming).trim() !== "") {
      out[col] = incoming;
    }
  }
  return out;
}

function leagueBonus(row) {
  const league = normalize(row.club_league_name || "");
  return TOP_LEAGUE_HINTS.some((h) => league.includes(h)) ? 4 : 0;
}

export const WEAK_SOFIFA_COUNTRIES = new Set([
  "",
  "friendly international",
  "free agents",
  "creation zone",
  "classic xi",
  "soccer aid",
]);

export function normalize(s) {
  return String(s ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, " ")
    .trim();
}

export function parseCsv(text) {
  const rows = [];
  let row = [];
  let field = "";
  let inQuotes = false;
  for (let i = 0; i < text.length; i++) {
    const ch = text[i];
    if (inQuotes) {
      if (ch === '"' && text[i + 1] === '"') {
        field += '"';
        i++;
      } else if (ch === '"') inQuotes = false;
      else field += ch;
    } else if (ch === '"') inQuotes = true;
    else if (ch === ",") {
      row.push(field);
      field = "";
    } else if (ch === "\n" || ch === "\r") {
      if (ch === "\r" && text[i + 1] === "\n") i++;
      row.push(field);
      if (row.some((c) => c.length > 0)) rows.push(row);
      row = [];
      field = "";
    } else field += ch;
  }
  if (field.length || row.length) {
    row.push(field);
    rows.push(row);
  }
  const headers = rows[0];
  return rows.slice(1).map((cells) => {
    const obj = {};
    headers.forEach((h, idx) => {
      obj[h] = cells[idx] ?? "";
    });
    return obj;
  });
}

export function val(row, ...keys) {
  for (const key of keys) {
    const raw = row[key];
    if (raw !== undefined && raw !== "") {
      const n = parseInt(String(raw), 10);
      if (Number.isFinite(n)) return n;
    }
  }
  return 0;
}

export function versionScore(row) {
  const blob = `${row.description || ""} ${row.version || ""}`;
  if (blob.includes("FC 26") || blob.includes("EA FC 26")) return 4;
  if (blob.includes("FC 25") || blob.includes("EA FC 25")) return 3;
  if (blob.includes("FC 24") || blob.includes("EA FC 24")) return 1;
  return 0;
}

export function outfieldFace(r) {
  const pace = Math.round(
    (val(r, "acceleration", "movement_acceleration") + val(r, "sprint_speed", "movement_sprint_speed")) / 2,
  );
  const shooting = Math.round(
    (val(r, "finishing", "attacking_finishing") +
      val(r, "shot_power", "power_shot_power") +
      val(r, "long_shots", "power_long_shots") +
      val(r, "positioning", "mentality_att_positioning") +
      val(r, "volleys", "attacking_volleys")) /
      5,
  );
  const passing = Math.round(
    (val(r, "vision", "mentality_vision") +
      val(r, "crossing", "attacking_crossing") +
      val(r, "short_passing", "attacking_short_passing") +
      val(r, "long_passing", "skill_long_passing") +
      val(r, "curve", "skill_curve")) /
      5,
  );
  const dribbling = Math.round(
    (val(r, "dribbling", "skill_dribbling") +
      val(r, "ball_control", "skill_ball_control") +
      val(r, "agility", "movement_agility") +
      val(r, "balance", "movement_balance")) /
      4,
  );
  const defending = Math.round(
    (val(r, "defensive_awareness", "defending_defensive_awareness") +
      val(r, "standing_tackle", "defending_standing_tackle") +
      val(r, "sliding_tackle", "defending_sliding_tackle") +
      val(r, "heading_accuracy", "attacking_heading_accuracy")) /
      4,
  );
  const physical = Math.round(
    (val(r, "jumping", "power_jumping") +
      val(r, "stamina", "power_stamina") +
      val(r, "strength", "power_strength") +
      val(r, "aggression", "mentality_aggression")) /
      4,
  );
  return { pace, shooting, passing, dribbling, defending, physical };
}

export function goalkeeperFace(r) {
  const speed = Math.round(
    (val(r, "acceleration", "movement_acceleration") + val(r, "sprint_speed", "movement_sprint_speed")) / 2,
  );
  return {
    diving: val(r, "gk_diving", "goalkeeping_gk_diving"),
    handling: val(r, "gk_handling", "goalkeeping_gk_handling"),
    kicking: val(r, "gk_kicking", "goalkeeping_gk_kicking"),
    reflexes: val(r, "gk_reflexes", "goalkeeping_gk_reflexes"),
    positioning: val(r, "gk_positioning", "goalkeeping_gk_positioning"),
    speed,
  };
}

export function countryMatches(seedCountry, sofifaCountry) {
  const b = normalize(sofifaCountry);
  if (WEAK_SOFIFA_COUNTRIES.has(b)) return null;
  const a = normalize(seedCountry);
  if (a === b || a.includes(b) || b.includes(a)) return true;
  const aliases = COUNTRY_ALIASES[a] ?? [];
  return aliases.some((x) => b.includes(x) || x.includes(b));
}

const NAME_SUFFIXES = new Set(["jr", "junior", "ii", "iii", "sr", "senior"]);

export function lastNameOf(name) {
  const parts = normalize(name).split(" ").filter(Boolean);
  const core =
    parts.length > 1 && NAME_SUFFIXES.has(parts[parts.length - 1]) ? parts.slice(0, -1) : parts;
  return core.length ? core[core.length - 1] : "";
}

export function significantTokens(name) {
  return normalize(name)
    .split(" ")
    .filter((t) => t.length > 1 && !NAME_SUFFIXES.has(t));
}

function tokenInBlob(token, blob) {
  if (blob.includes(` ${token} `) || blob.endsWith(` ${token}`)) return true;
  for (const alt of TOKEN_ALIASES[token] ?? []) {
    if (blob.includes(` ${alt} `) || blob.endsWith(` ${alt}`)) return true;
  }
  return false;
}

export function allTokensMatch(seedName, row) {
  const tokens = significantTokens(seedName);
  if (!tokens.length) return false;
  const blob = ` ${normalize(`${row.name} ${row.full_name}`)} `;
  return tokens.every((t) => tokenInBlob(t, blob));
}

export function nameScore(seedName, row) {
  const n = normalize(seedName);
  const candidates = [normalize(row.name), normalize(row.full_name)].filter(Boolean);
  let best = 0;
  for (const c of candidates) {
    if (c === n) return 100;
    if (c.includes(n) || n.includes(c)) best = Math.max(best, 82);
    if (allTokensMatch(seedName, row)) best = Math.max(best, 93);
    const seedParts = significantTokens(seedName);
    const last = seedParts[seedParts.length - 1] ?? "";
    const first = seedParts[0] ?? "";
    const cParts = c.split(" ").filter(Boolean);
    if (last.length > 2 && cParts.includes(last)) best = Math.max(best, 74);
    if (first.length > 2 && last.length > 2 && cParts.includes(first) && cParts.includes(last)) {
      best = Math.max(best, 94);
    }
    if (first.length > 2 && last.length > 2 && c.endsWith(` ${last}`) && c.includes(` ${first}`)) {
      best = Math.max(best, 96);
    }
    if (first.length > 2 && cParts.includes(first) && seedParts.length === 1) {
      best = Math.max(best, 88);
    }
    if (normalize(row.name) === "vini jr" && seedParts.includes("vinicius")) {
      best = Math.max(best, 100);
    }
    if (normalize(row.name) === "neymar jr" && seedParts.includes("neymar")) {
      best = Math.max(best, 100);
    }
    if (normalize(row.name) === "k mbappe" && seedParts.includes("mbappe")) {
      best = Math.max(best, 100);
    }
    if (c === "lionel messi" && seedParts.includes("messi")) {
      best = Math.max(best, 100);
    }
    if (normalize(row.name) === "cristiano ronaldo" && seedParts.includes("cristiano")) {
      best = Math.max(best, 100);
    }
  }
  return best;
}

export function positionCompatible(player, row) {
  const isGk = player.position === "Goalkeeper";
  const pos = (row.positions || "").toLowerCase();
  if (isGk) return pos.includes("gk");
  return !pos.split(",").every((p) => p.trim() === "gk");
}

export function isPlaceholderPlayer(player) {
  return /\b(GK|DF|MF|FW|Star)\s+\d+$/i.test(player.playerName?.trim() ?? "");
}

export function rowToRating(player, sofifa) {
  const overall = val(sofifa, "overall_rating", "overall");
  const club = sofifa.club_name?.trim() ?? "";
  const league = sofifa.club_league_name?.trim() ?? "";
  const clubLogo = sofifa.club_logo?.trim() ?? "";
  if (player.position === "Goalkeeper") {
    const gk = goalkeeperFace(sofifa);
    return {
      player_id: player.playerId,
      club_name: club,
      club_league: league,
      club_logo_url: clubLogo,
      overall,
      pace: 0,
      shooting: 0,
      passing: 0,
      dribbling: 0,
      defending: 0,
      physical: 0,
      diving: gk.diving,
      handling: gk.handling,
      kicking: gk.kicking,
      reflexes: gk.reflexes,
      speed: gk.speed,
      positioning: gk.positioning,
    };
  }
  const out = outfieldFace(sofifa);
  return {
    player_id: player.playerId,
    club_name: club,
    club_league: league,
    club_logo_url: clubLogo,
    overall,
    pace: out.pace,
    shooting: out.shooting,
    passing: out.passing,
    dribbling: out.dribbling,
    defending: out.defending,
    physical: out.physical,
    diving: 0,
    handling: 0,
    kicking: 0,
    reflexes: 0,
    speed: 0,
    positioning: 0,
  };
}

export function emptyRatingRow(playerId) {
  return Object.fromEntries(RATINGS_CSV_COLUMNS.map((c) => [c, c === "player_id" ? playerId : ""]));
}

export function escapeCsv(v) {
  const s = String(v ?? "");
  return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
}

export function normalizeClubName(name) {
  return (name ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/&/g, " and ")
    .replace(/[^a-z0-9]+/g, " ")
    .replace(/\b(fc|cf|sc|ac|bk|sk|sv|vfb|vfl|rb|cd|ud|sd|ca|rc|afc)\b/g, "")
    .replace(/\s+/g, " ")
    .trim();
}

/** club normalized name → { league, logo } from SoFIFA rows */
export function buildSofifaClubIndex(sofifaRows) {
  const byNorm = new Map();
  for (const row of sofifaRows) {
    const club = row.club_name?.trim();
    if (!club) continue;
    const norm = normalizeClubName(club);
    if (!norm || byNorm.has(norm)) continue;
    byNorm.set(norm, {
      league: row.club_league_name?.trim() ?? "",
      logo: row.club_logo?.trim() ?? "",
    });
  }
  return byNorm;
}

export function lookupClubMeta(clubName, clubIndex) {
  const norm = normalizeClubName(clubName);
  if (!norm) return null;
  if (clubIndex.has(norm)) return clubIndex.get(norm);

  let best = null;
  let bestLen = 0;
  for (const [key, meta] of clubIndex) {
    if (norm.includes(key) || key.includes(norm)) {
      const len = Math.min(norm.length, key.length);
      if (len > bestLen) {
        bestLen = len;
        best = meta;
      }
    }
  }
  return best;
}

function sanitizeClubName(club) {
  return (club ?? "")
    .replace(/\[\[/g, "")
    .replace(/\]\]/g, "")
    .replace(/\s*\([^)]*\)\s*$/, "")
    .trim();
}

export function loadOfficialClubIndexes(root) {
  const p = path.join(root, "data", "official_squads_2026.json");
  const byPlayerId = new Map();
  const byTeamShirt = new Map();
  if (!fs.existsSync(p)) return { byPlayerId, byTeamShirt };
  const data = JSON.parse(fs.readFileSync(p, "utf8"));
  for (const [teamId, squad] of Object.entries(data.teams ?? {})) {
    for (const entry of squad) {
      const club = sanitizeClubName(entry.club?.trim());
      if (!club) continue;
      const info = { club, clubNat: entry.club_nat?.trim() ?? "" };
      byPlayerId.set(playerIdFor(teamId, entry.name), info);
      byTeamShirt.set(`${teamId}_${entry.shirt}`, info);
    }
  }
  return { byPlayerId, byTeamShirt };
}

export function loadOfficialClubsMap(root) {
  return loadOfficialClubIndexes(root).byPlayerId;
}

export function officialClubFor(player, indexes) {
  const byShirt = indexes.byTeamShirt.get(`${player.teamId}_${player.shirtNumber}`);
  if (byShirt) return byShirt;
  return indexes.byPlayerId.get(player.playerId) ?? null;
}

/** Wikipedia club overrides SoFIFA; league/logo resolved from SoFIFA club index. */
export function applyOfficialClub(row, player, officialIndexes, clubIndex) {
  const official = officialClubFor(player, officialIndexes);
  if (!official?.club) return row;
  const next = { ...row, club_name: official.club };
  const meta = lookupClubMeta(official.club, clubIndex);
  if (meta?.league) next.club_league = meta.league;
  if (meta?.logo && !next.club_logo_url?.trim()) next.club_logo_url = meta.logo;
  return next;
}

export function buildSofifaIndex(sofifaRows) {
  const byLastName = new Map();
  for (const row of sofifaRows) {
    const last = lastNameOf(row.name || row.full_name);
    if (last.length < 3) continue;
    if (!byLastName.has(last)) byLastName.set(last, []);
    byLastName.get(last).push(row);
  }
  return { byLastName, all: sofifaRows };
}

export function scoreCandidate(player, row, { requireCountry }) {
  if (!positionCompatible(player, row)) return 0;
  const ns = nameScore(player.playerName, row);
  if (ns < 55) return 0;
  for (const token of significantTokens(player.playerName)) {
    const min = MIN_OVR_HINT[token];
    if (min && ovr(row) > 0 && ovr(row) < min) return 0;
  }

  const country = countryMatches(player.countryName, row.country_name);
  if (requireCountry) {
    if (country !== true) return 0;
  } else {
    if (country === false) return 0;
    if (ns < 88) return 0;
    const last = lastNameOf(player.playerName);
    const full = normalize(row.full_name || row.name);
    if (!full.split(" ").includes(last) && !allTokensMatch(player.playerName, row)) return 0;
  }

  const isGk = player.position === "Goalkeeper";
  if (isGk && ns < 70) return 0;

  return (
    ns + versionScore(row) * 8 + leagueBonus(row) + (val(row, "overall_rating", "overall") > 0 ? 3 : 0)
  );
}

function ovr(row) {
  return val(row, "overall_rating", "overall");
}

function pickBest(player, pool, requireCountry, minScore) {
  let best = null;
  let bestScore = 0;
  for (const row of pool) {
    const s = scoreCandidate(player, row, { requireCountry });
    const better =
      s > bestScore ||
      (s >= bestScore - 3 &&
        best &&
        ovr(row) > ovr(best) &&
        nameScore(player.playerName, row) >= 88);
    if (better) {
      bestScore = Math.max(bestScore, s);
      best = row;
    }
  }
  return bestScore >= minScore ? best : null;
}

export function findBestMatch(player, index) {
  const last = lastNameOf(player.playerName);
  const pool =
    last.length >= 3 && index.byLastName.has(last) ? index.byLastName.get(last) : index.all;

  let hit = pickBest(player, pool, true, 63);
  if (hit) return hit;

  hit = pickBest(player, pool, false, 88);
  if (hit) return hit;

  let best = null;
  let bestScore = 0;
  for (const row of index.all) {
    if (!positionCompatible(player, row)) continue;
    if (!allTokensMatch(player.playerName, row)) continue;
    const country = countryMatches(player.countryName, row.country_name);
    if (country === false) continue;
    const ns = nameScore(player.playerName, row);
    const s = ns + versionScore(row) * 8 + 5;
    const better =
      s > bestScore || (s >= bestScore - 3 && best && ovr(row) > ovr(best));
    if (better) {
      bestScore = Math.max(bestScore, s);
      best = row;
    }
  }
  return bestScore >= 90 ? best : null;
}
