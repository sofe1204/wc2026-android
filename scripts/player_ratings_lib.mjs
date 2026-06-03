/**
 * Shared helpers for SoFIFA CSV parsing and face-stat computation.
 */
export const SOFIFA_URL =
  "https://github.com/SolideSpoke/sofifa-web-scraper/raw/main/output/player-data-full.csv";

export const RATINGS_CSV_COLUMNS = [
  "player_id",
  "club_name",
  "club_league",
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

export function lastNameOf(name) {
  const parts = normalize(name).split(" ").filter(Boolean);
  return parts.length ? parts[parts.length - 1] : "";
}

export function nameScore(seedName, row) {
  const n = normalize(seedName);
  const candidates = [normalize(row.name), normalize(row.full_name)].filter(Boolean);
  let best = 0;
  for (const c of candidates) {
    if (c === n) return 100;
    if (c.includes(n) || n.includes(c)) best = Math.max(best, 82);
    const seedParts = n.split(" ").filter(Boolean);
    const last = seedParts[seedParts.length - 1];
    const first = seedParts[0];
    const cParts = c.split(" ").filter(Boolean);
    if (last.length > 2 && cParts.includes(last)) best = Math.max(best, 74);
    if (first.length > 2 && last.length > 2 && cParts.includes(first) && cParts.includes(last)) {
      best = Math.max(best, 94);
    }
    if (first.length > 2 && last.length > 2 && c.endsWith(` ${last}`) && c.includes(` ${first}`)) {
      best = Math.max(best, 96);
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
  if (player.position === "Goalkeeper") {
    const gk = goalkeeperFace(sofifa);
    return {
      player_id: player.playerId,
      club_name: club,
      club_league: league,
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

  const country = countryMatches(player.countryName, row.country_name);
  if (requireCountry) {
    if (country !== true) return 0;
  } else {
    if (country === false) return 0;
    if (ns < 88) return 0;
    const last = lastNameOf(player.playerName);
    const full = normalize(row.full_name || row.name);
    if (!full.split(" ").includes(last)) return 0;
  }

  const isGk = player.position === "Goalkeeper";
  if (isGk && ns < 70) return 0;

  return ns + versionScore(row) * 8 + (val(row, "overall_rating", "overall") > 0 ? 3 : 0);
}

export function findBestMatch(player, index) {
  const last = lastNameOf(player.playerName);
  const pool =
    last.length >= 3 && index.byLastName.has(last) ? index.byLastName.get(last) : index.all;

  let best = null;
  let bestScore = 0;
  for (const row of pool) {
    const s = scoreCandidate(player, row, { requireCountry: true });
    if (s > bestScore) {
      bestScore = s;
      best = row;
    }
  }
  if (best && bestScore >= 63) return best;

  best = null;
  bestScore = 0;
  for (const row of pool) {
    const s = scoreCandidate(player, row, { requireCountry: false });
    if (s > bestScore) {
      bestScore = s;
      best = row;
    }
  }
  return bestScore >= 91 ? best : null;
}
