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

fs.mkdirSync(outDir, { recursive: true });
fs.mkdirSync(workDir, { recursive: true });

const MIXKIT_ID = 2067; // Casino bling achievement

async function tryDownload(url, dest, referer = 'https://mixkit.co/') {
  const res = await fetch(url, {
    headers: { 'User-Agent': UA, Referer: referer },
    redirect: 'follow',
  });
  if (!res.ok) throw new Error(`${res.status}`);
  const buf = Buffer.from(await res.arrayBuffer());
  if (buf.length < 5000) throw new Error(`too small (${buf.length}b)`);
  fs.writeFileSync(dest, buf);
  return buf.length;
}

async function resolveMixkitAsset() {
  const pageUrl = `https://mixkit.co/free-sound-effects/download/${MIXKIT_ID}/?context=item+grid`;
  const html = await (await fetch(pageUrl, { headers: { 'User-Agent': UA } })).text();
  const match = html.match(/https:\/\/assets\.mixkit\.co\/active_storage\/sfx\/\d+\/\d+\.wav/);
  if (match) return match[0];

  return `https://assets.mixkit.co/active_storage/sfx/${MIXKIT_ID}/${MIXKIT_ID}.wav`;
}

function toWinOgg(input, output) {
  execFileSync(ffmpeg, [
    '-y',
    '-i',
    input,
    '-af',
    'silenceremove=start_periods=1:start_silence=0.01:start_threshold=-45dB,loudnorm=I=-8:TP=-0.3:LRA=8',
    '-c:a',
    'libvorbis',
    '-ar',
    '44100',
    '-q:a',
    '6',
    output,
  ], { stdio: 'pipe' });
}

const rawInput = path.join(workDir, 'casino_bling_src.bin');
const url = await resolveMixkitAsset();
console.log('Downloading', url);
const size = await tryDownload(url, rawInput);
console.log(`Downloaded ${size} bytes`);

const winOut = path.join(outDir, 'slot_win.ogg');
toWinOgg(rawInput, winOut);
const head = fs.readFileSync(winOut).subarray(0, 4).toString('ascii');
console.log(`Wrote ${winOut} (${fs.statSync(winOut).size} bytes, ${head})`);
