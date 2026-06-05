#!/usr/bin/env node
/**
 * Refresh animeStickerPrompt on existing players_seed.json (keeps ratings, imageUrl, etc.).
 * playerName, countryName, and position (goalkeeper vs outfield kit) go into the template.
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { stickerPrompt, writeJson, readJson } from "./sticker_images_lib.mjs";

const root = path.join(path.dirname(fileURLToPath(import.meta.url)), "..");
const config = readJson(path.join(root, "project.config.json"));
const seedDirs = [
  path.join(root, config.seed.android),
  path.join(root, config.seed.functions),
];

let updated = 0;
for (const dir of seedDirs) {
  const file = path.join(dir, "players_seed.json");
  const players = readJson(file);
  for (const p of players) {
    const next = stickerPrompt(p.playerName, p.countryName, p.position);
    if (p.animeStickerPrompt !== next) {
      p.animeStickerPrompt = next;
      updated++;
    }
  }
  writeJson(file, players);
  console.log(`${file}: ${players.length} players`);
}

console.log(`Updated ${updated} prompt(s) across ${seedDirs.length} seed copies.`);
