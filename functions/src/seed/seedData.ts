import * as fs from "fs";
import * as path from "path";

function loadJson<T>(filename: string): T {
  const p = path.join(__dirname, "..", "..", "seed", filename);
  const raw = fs.readFileSync(p, "utf8");
  return JSON.parse(raw) as T;
}

export function loadTeamsSeed() {
  return loadJson<Record<string, unknown>[]>("teams_seed.json");
}

export function loadPlayersSeed() {
  return loadJson<Record<string, unknown>[]>("players_seed.json");
}

export function loadStickersSeed() {
  return loadJson<Record<string, unknown>[]>("stickers_seed.json");
}
