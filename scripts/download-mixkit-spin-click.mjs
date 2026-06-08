import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { execFileSync } from 'child_process';
import ffmpegInstaller from '@ffmpeg-installer/ffmpeg';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ffmpeg = ffmpegInstaller.path;
const outDir = path.join(__dirname, '..', 'android', 'app', 'src', 'main', 'res', 'raw');
const workDir = path.join(__dirname, '.slot-audio-work');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/122.0.0.0 Safari/537.36';

const MIXKIT_ID = 2037; // Player select notification

fs.mkdirSync(outDir, { recursive: true });
fs.mkdirSync(workDir, { recursive: true });

async function resolveMixkitAsset(id) {
  const pageUrl = `https://mixkit.co/free-sound-effects/download/${id}/?context=item+grid`;
  const pageRes = await fetch(pageUrl, { headers: { 'User-Agent': UA } });
  const cookies = pageRes.headers.getSetCookie?.().map((c) => c.split(';')[0]).join('; ') ?? '';
  const html = await pageRes.text();
  const match = html.match(/https:\/\/assets\.mixkit\.co\/active_storage\/sfx\/\d+\/\d+\.(wav|mp3)/);
  if (!match) throw new Error(`Could not resolve Mixkit asset URL for id ${id}`);
  return { url: match[0], cookies };
}

const { url, cookies } = await resolveMixkitAsset(MIXKIT_ID);
const rawInput = path.join(workDir, 'player_select_src.bin');
console.log('Downloading', url);
const res = await fetch(url, {
  headers: { 'User-Agent': UA, Referer: 'https://mixkit.co/', Cookie: cookies },
});
if (!res.ok) throw new Error(`download failed: ${res.status}`);
const buf = Buffer.from(await res.arrayBuffer());
fs.writeFileSync(rawInput, buf);
console.log(`Downloaded ${buf.length} bytes`);

const clickOut = path.join(outDir, 'slot_spin_click.ogg');
execFileSync(ffmpeg, [
  '-y',
  '-i',
  rawInput,
  '-af',
  'silenceremove=start_periods=1:start_silence=0.01:start_threshold=-45dB,loudnorm=I=-12:TP=-1:LRA=7',
  '-c:a',
  'libvorbis',
  '-ar',
  '44100',
  '-q:a',
  '6',
  clickOut,
], { stdio: 'pipe' });

const head = fs.readFileSync(clickOut).subarray(0, 4).toString('ascii');
console.log(`Wrote ${clickOut} (${fs.statSync(clickOut).size} bytes, ${head})`);
