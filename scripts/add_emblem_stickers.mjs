import { readFileSync, writeFileSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const config = JSON.parse(readFileSync(join(root, 'project.config.json'), 'utf8'));
const paths = [
  join(root, config.seed.android, 'stickers_seed.json'),
  join(root, config.seed.functions, 'stickers_seed.json'),
];

for (const file of paths) {
  const stickers = JSON.parse(readFileSync(file, 'utf8'));
  const withoutEmblems = stickers.filter((s) => !(s.stickerNumber === 0 && !s.playerId));
  const teams = new Map();
  for (const s of withoutEmblems) {
    if (!teams.has(s.teamId)) teams.set(s.teamId, s);
  }
  const emblems = [...teams.values()].map((sample) => {
    const code = sample.stickerId.split('-')[0];
    return {
      stickerId: `${code}-000`,
      stickerNumber: 0,
      playerId: '',
      teamId: sample.teamId,
      countryName: sample.countryName,
      group: sample.group,
      rarity: 'epic',
      imageUrl: '',
      isActive: true,
    };
  });
  const merged = [...emblems, ...withoutEmblems].sort((a, b) => {
    if (a.teamId !== b.teamId) return a.teamId.localeCompare(b.teamId);
    return a.stickerNumber - b.stickerNumber;
  });
  writeFileSync(file, JSON.stringify(merged, null, 2), 'utf8');
  console.log(`${file}: ${merged.length} stickers (${emblems.length} emblems)`);
}
