/** Map local emblem/logo files to teamId (flat folder of country-named images). */
import fs from "fs";
import path from "path";
import {
  buildTeamFolderIndex,
  normalizeMatchKey,
  resolveFolderToTeamId,
  WC26_FOLDER_ALIASES,
} from "./wc26_sticker_match_lib.mjs";

const IMAGE_EXT = new Set([".png", ".jpg", ".jpeg", ".webp"]);

/** Filename quirks beyond wc26 country folder aliases. */
export const LOGO_FILENAME_ALIASES = {
  ...WC26_FOLDER_ALIASES,
  swedeb: "sweden",
};

/** @param {Array<{ teamId: string, countryName: string }>} teams */
export function buildLogoFilenameIndex(teams) {
  const index = buildTeamFolderIndex(teams);
  for (const [label, teamId] of Object.entries(LOGO_FILENAME_ALIASES)) {
    const key = normalizeMatchKey(label);
    if (key) index.set(key, teamId);
  }
  return index;
}

export function resolveLogoToTeamId(fileBaseName, filenameIndex) {
  return resolveFolderToTeamId(fileBaseName, filenameIndex);
}

/**
 * Scan a flat directory of logo images named after countries.
 * @returns {{ matched: object[], unmatchedFiles: object[], teamsWithoutLogo: object[] }}
 */
export function scanLogoSourceDir(sourceDir, teams) {
  if (!fs.existsSync(sourceDir)) {
    throw new Error(`Logo source directory not found: ${sourceDir}`);
  }

  const filenameIndex = buildLogoFilenameIndex(teams);
  const teamById = new Map(teams.map((t) => [t.teamId, t]));
  const matched = [];
  const unmatchedFiles = [];
  const matchedTeamIds = new Set();

  for (const ent of fs.readdirSync(sourceDir, { withFileTypes: true })) {
    if (!ent.isFile()) continue;
    const ext = path.extname(ent.name).toLowerCase();
    if (!IMAGE_EXT.has(ext)) continue;

    const fileBaseName = path.basename(ent.name, ext);
    const filePath = path.join(sourceDir, ent.name);
    const teamId = resolveLogoToTeamId(fileBaseName, filenameIndex);
    const row = { filePath, fileBaseName, ext };

    if (!teamId || !teamById.has(teamId)) {
      unmatchedFiles.push({ ...row, teamId: teamId ?? null });
      continue;
    }

    matchedTeamIds.add(teamId);
    matched.push({ ...row, teamId, team: teamById.get(teamId) });
  }

  matched.sort((a, b) => a.teamId.localeCompare(b.teamId));
  unmatchedFiles.sort((a, b) => a.filePath.localeCompare(b.filePath));

  const teamsWithoutLogo = teams
    .filter((t) => t.isActive !== false && !matchedTeamIds.has(t.teamId))
    .sort((a, b) => a.teamId.localeCompare(b.teamId));

  return { matched, unmatchedFiles, teamsWithoutLogo };
}

/** Emblem sticker rows: stickerNumber 0, no playerId. */
export function indexEmblemStickersByTeamId(stickers) {
  const map = new Map();
  for (const s of stickers) {
    if (s.stickerNumber === 0 && !s.playerId && s.teamId) {
      map.set(s.teamId, s);
    }
  }
  return map;
}
