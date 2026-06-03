/** Shared seed helpers (Node port of player_data_lib.py). */
import fs from "fs";
import path from "path";

export function slugify(name) {
  return name
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "_")
    .replace(/^_+|_+$/g, "");
}

export function playerIdFor(teamId, playerName) {
  return `${teamId}_${slugify(playerName)}`;
}

export function emptyRatings() {
  return {
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
}

export function squadTuple(shirt, name, position, rarity) {
  return { shirt_number: shirt, player_name: name, position, rarity };
}

export function writeSquadsCsv(filePath, squadsByTeam) {
  const lines = ["team_id,shirt_number,player_name,position,rarity"];
  for (const [teamId, rows] of Object.entries(squadsByTeam).sort(([a], [b]) => a.localeCompare(b))) {
    for (const r of rows) {
      const name = r.player_name.includes(",") ? `"${r.player_name.replace(/"/g, '""')}"` : r.player_name;
      lines.push(`${teamId},${r.shirt_number},${name},${r.position},${r.rarity}`);
    }
  }
  fs.mkdirSync(path.dirname(path.resolve(filePath)), { recursive: true });
  fs.writeFileSync(filePath, lines.join("\n") + "\n", "utf8");
}

/** @param {Array<[number, string, string, string]>} tuples */
export function tuplesToSquad(tuples) {
  return tuples.map(([shirt, name, position, rarity]) => squadTuple(shirt, name, position, rarity));
}
