/** Match slot reel image files to slot_symbols_seed rows. */
import fs from "fs";
import path from "path";
import { normalizeMatchKey } from "./wc26_sticker_match_lib.mjs";

const IMAGE_EXT = new Set([".png", ".jpg", ".jpeg", ".webp"]);
const SLOTS_SUFFIX = /\s+slots$/i;

export function parseSlotFileBaseName(fileBaseName) {
  return String(fileBaseName).replace(SLOTS_SUFFIX, "").trim();
}

/** @param {Array<{ symbolId: string, playerId?: string, label: string, type: string }>} symbols */
export function buildSlotSymbolLookup(symbols, players) {
  const byKey = new Map();
  const add = (key, symbol) => {
    const k = normalizeMatchKey(key);
    if (k && !byKey.has(k)) byKey.set(k, symbol);
  };

  for (const symbol of symbols) {
    add(symbol.symbolId, symbol);
    add(symbol.label, symbol);
    if (symbol.playerId) {
      const player = players.find((p) => p.playerId === symbol.playerId);
      if (player) add(player.playerName, symbol);
    }
  }

  for (const player of players) {
    add(player.playerName, null);
    add(player.playerId, null);
  }

  return { byKey, players };
}

export function matchFileToSymbol(fileBaseName, symbols, players) {
  const label = parseSlotFileBaseName(fileBaseName);
  const key = normalizeMatchKey(label);

  if (key === "trophy") {
    return symbols.find((s) => s.symbolId === "trophy") ?? null;
  }

  const direct = symbols.find(
    (s) =>
      normalizeMatchKey(s.label) === key ||
      normalizeMatchKey(s.symbolId) === key ||
      (s.playerId && normalizeMatchKey(s.playerId.split("_").slice(1).join(" ")) === key)
  );
  if (direct) return direct;

  const player = players.find((p) => normalizeMatchKey(p.playerName) === key);
  if (!player) return null;

  return (
    symbols.find((s) => s.playerId === player.playerId) ??
    symbols.find((s) => s.symbolId === player.playerId) ??
    null
  );
}

export function scanSlotSourceDir(sourceDir, symbols, players) {
  if (!fs.existsSync(sourceDir)) {
    throw new Error(`Slot source directory not found: ${sourceDir}`);
  }

  const matched = [];
  const unmatchedFiles = [];
  const matchedIds = new Set();

  for (const ent of fs.readdirSync(sourceDir, { withFileTypes: true })) {
    if (!ent.isFile()) continue;
    const ext = path.extname(ent.name).toLowerCase();
    if (!IMAGE_EXT.has(ext)) continue;

    const fileBaseName = path.basename(ent.name, ext);
    const filePath = path.join(sourceDir, ent.name);
    const symbol = matchFileToSymbol(fileBaseName, symbols, players);

    const row = { filePath, fileBaseName, ext, parsedLabel: parseSlotFileBaseName(fileBaseName) };
    if (symbol) {
      matched.push({ ...row, symbol });
      matchedIds.add(symbol.symbolId);
    } else {
      unmatchedFiles.push(row);
    }
  }

  matched.sort((a, b) => a.symbol.symbolId.localeCompare(b.symbol.symbolId));
  unmatchedFiles.sort((a, b) => a.filePath.localeCompare(b.filePath));

  const symbolsWithoutFile = symbols
    .filter((s) => s.isActive !== false && !matchedIds.has(s.symbolId))
    .sort((a, b) => a.symbolId.localeCompare(b.symbolId));

  return { matched, unmatchedFiles, symbolsWithoutFile };
}
