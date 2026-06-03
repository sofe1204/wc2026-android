#!/usr/bin/env node
import { CORE } from "./squads_core.mjs";
import { EXTENDED } from "./squads_extended.mjs";
import { playerIdFor } from "./seed_lib.mjs";

const merged = { ...CORE, ...EXTENDED };
let exitCode = 0;
for (const [teamId, tuples] of Object.entries(merged)) {
  const seen = new Map();
  for (const [shirt, name] of tuples) {
    const id = playerIdFor(teamId, name);
    if (seen.has(id)) {
      console.error(`DUPLICATE ${teamId}: #${shirt} ${name} (also #${seen.get(id).shirt} ${seen.get(id).name})`);
      exitCode = 1;
    } else seen.set(id, { shirt, name });
  }
}
process.exit(exitCode);
