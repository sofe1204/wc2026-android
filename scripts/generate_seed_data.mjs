#!/usr/bin/env node
/**
 * Regenerates seed JSON from official FIFA WC 2026 squads (delegates to Python).
 * Source of truth: data/official_squads_2026.json via scripts/generate_seed_data.py
 */
import { spawnSync } from "child_process";
import path from "path";
import { fileURLToPath } from "url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const py = path.join(ROOT, "scripts", "generate_seed_data.py");
const result = spawnSync("python3", [py], { cwd: ROOT, stdio: "inherit" });
if (result.status !== 0) {
  process.exit(result.status ?? 1);
}
