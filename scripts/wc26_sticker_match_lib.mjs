/** Match Panini scan files under wc26/ to players_seed rows. */
import fs from "fs";
import path from "path";
import { slugify } from "./seed_lib.mjs";

/** wc26 folder name → teamId (when folder name ≠ countryName / teamId). */
/** Panini filename (teamId:normalized) → playerId when romanization/order cannot be inferred. */
export const WC26_FILE_PLAYER_ID_ALIASES = {
  "south_korea:hyeongyuoh": "south_korea_oh_hyeon_gyu",
};

export const WC26_FOLDER_ALIASES = {
  bosnia: "bosnia_herzegovina",
  korea: "south_korea",
  usa: "united_states",
  "czech republic": "czechia",
  congo: "dr_congo",
  turkey: "turkiye",
  "ivory coast": "ivory_coast",
  "cape verde": "cape_verde",
  "saudi arabia": "saudi_arabia",
  "south africa": "south_africa",
  "new zealand": "new_zealand",
};

const IMAGE_EXT = new Set([".png", ".jpg", ".jpeg", ".webp"]);

/** Accent-insensitive, punctuation-stripped key for filename ↔ playerName matching. */
export function normalizeMatchKey(s) {
  return String(s ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[''`´]/g, "")
    .replace(/[^a-z0-9]+/g, "");
}

/** @param {Array<{ teamId: string, countryName: string }>} teams */
export function buildTeamFolderIndex(teams) {
  const byFolderKey = new Map();
  const add = (label, teamId) => {
    const key = normalizeMatchKey(label);
    if (key) byFolderKey.set(key, teamId);
  };

  for (const team of teams) {
    add(team.teamId, team.teamId);
    add(team.teamId.replace(/_/g, " "), team.teamId);
    add(team.countryName, team.teamId);
  }
  for (const [folder, teamId] of Object.entries(WC26_FOLDER_ALIASES)) {
    add(folder, teamId);
  }
  return byFolderKey;
}

export function resolveFolderToTeamId(folderName, folderIndex) {
  return folderIndex.get(normalizeMatchKey(folderName)) ?? null;
}

/** Alternate keys for the same player (handles hyphens, underscores, playerId slug). */
export function playerMatchKeys(player) {
  const keys = new Set();
  keys.add(normalizeMatchKey(player.playerName));

  const underscore = player.playerId.indexOf("_");
  if (underscore >= 0) {
    const slugPart = player.playerId.slice(underscore + 1);
    keys.add(normalizeMatchKey(slugPart.replace(/_/g, " ")));
    keys.add(normalizeMatchKey(slugPart.replace(/_/g, "")));
  }

  keys.add(normalizeMatchKey(slugify(player.playerName).replace(/_/g, "")));
  return [...keys].filter(Boolean);
}

/** @param {Array<{ playerId: string, teamId: string, playerName: string, isActive?: boolean }>} players */
export function buildPlayerLookup(players) {
  const byTeamAndKey = new Map();
  const conflicts = [];

  for (const player of players) {
    if (player.isActive === false) continue;
    for (const key of playerMatchKeys(player)) {
      const composite = `${player.teamId}:${key}`;
      const existing = byTeamAndKey.get(composite);
      if (existing && existing.playerId !== player.playerId) {
        conflicts.push({ key: composite, a: existing.playerId, b: player.playerId });
        continue;
      }
      byTeamAndKey.set(composite, player);
    }
  }

  return { lookup: byTeamAndKey, conflicts };
}

/** Keys to try for a scan filename (direct + reversed given/family order). */
export function fileMatchKeys(fileBaseName) {
  const keys = [normalizeMatchKey(fileBaseName)];
  const parts = String(fileBaseName ?? "")
    .trim()
    .split(/\s+/)
    .filter(Boolean);
  if (parts.length >= 2) {
    keys.push(normalizeMatchKey([...parts].reverse().join(" ")));
  }
  return [...new Set(keys.filter(Boolean))];
}

export function matchFileToPlayer(fileBaseName, teamId, lookup, playerById = null) {
  for (const fileKey of fileMatchKeys(fileBaseName)) {
    const hit = lookup.get(`${teamId}:${fileKey}`);
    if (hit) return hit;

    const aliasPlayerId = WC26_FILE_PLAYER_ID_ALIASES[`${teamId}:${fileKey}`];
    if (aliasPlayerId && playerById) {
      const aliased = playerById.get(aliasPlayerId);
      if (aliased) return aliased;
    }
  }
  return null;
}

/**
 * Scan source tree: one folder per country, image files named after players.
 * @returns {{ matched: object[], unmatchedFiles: object[], unknownFolders: string[], conflicts: object[] }}
 */
export function scanWc26SourceDir(sourceDir, teams, players) {
  if (!fs.existsSync(sourceDir)) {
    throw new Error(`Source directory not found: ${sourceDir}`);
  }

  const folderIndex = buildTeamFolderIndex(teams);
  const { lookup, conflicts } = buildPlayerLookup(players);
  const playerById = new Map(players.map((p) => [p.playerId, p]));
  const matched = [];
  const unmatchedFiles = [];
  const unknownFolders = new Set();

  for (const ent of fs.readdirSync(sourceDir, { withFileTypes: true })) {
    if (!ent.isDirectory()) continue;
    const folderName = ent.name;
    const teamId = resolveFolderToTeamId(folderName, folderIndex);
    if (!teamId) {
      unknownFolders.add(folderName);
      continue;
    }

    const dir = path.join(sourceDir, folderName);
    for (const fileEnt of fs.readdirSync(dir, { withFileTypes: true })) {
      if (!fileEnt.isFile()) continue;
      const ext = path.extname(fileEnt.name).toLowerCase();
      if (!IMAGE_EXT.has(ext)) continue;

      const fileBaseName = path.basename(fileEnt.name, ext);
      const filePath = path.join(dir, fileEnt.name);
      const player = matchFileToPlayer(fileBaseName, teamId, lookup, playerById);

      const row = { filePath, folderName, teamId, fileBaseName, ext };
      if (player) {
        matched.push({ ...row, player });
      } else {
        unmatchedFiles.push(row);
      }
    }
  }

  matched.sort((a, b) => a.player.playerId.localeCompare(b.player.playerId));
  unmatchedFiles.sort((a, b) => a.filePath.localeCompare(b.filePath));

  return {
    matched,
    unmatchedFiles,
    unknownFolders: [...unknownFolders].sort(),
    conflicts,
  };
}

/** Active seed players with no matching file in wc26/. */
export function playersWithoutWc26File(players, matched) {
  const matchedIds = new Set(matched.map((m) => m.player.playerId));
  return players
    .filter((p) => p.isActive !== false && !matchedIds.has(p.playerId))
    .sort((a, b) => a.playerId.localeCompare(b.playerId));
}
