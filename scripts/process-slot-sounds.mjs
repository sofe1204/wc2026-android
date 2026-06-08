import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { execFileSync } from 'child_process';
import ffmpegInstaller from '@ffmpeg-installer/ffmpeg';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ffmpeg = ffmpegInstaller.path;
const outDir = path.join(__dirname, '..', 'android', 'app', 'src', 'main', 'res', 'raw');
const workDir = path.join(__dirname, '.slot-audio-work');
const kenneyCasino = path.join(workDir, 'kenney_casino', 'Audio');

fs.mkdirSync(outDir, { recursive: true });
fs.mkdirSync(workDir, { recursive: true });

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36';

async function download(url, dest, referer = 'https://opengameart.org/') {
  const res = await fetch(url, { headers: { 'User-Agent': UA, Referer: referer } });
  if (!res.ok) throw new Error(`${res.status} ${url}`);
  const buf = Buffer.from(await res.arrayBuffer());
  fs.writeFileSync(dest, buf);
  return buf.length;
}

async function ensureKenneyCasino() {
  const zip = path.join(workDir, 'kenney_casino-audio.zip');
  if (!fs.existsSync(path.join(kenneyCasino, 'chip-lay-1.ogg'))) {
    if (!fs.existsSync(zip)) {
      console.log('Downloading Kenney casino pack...');
      await download(
        'https://opengameart.org/sites/default/files/kenney_casino-audio.zip',
        zip,
      );
    }
    execFileSync('powershell', [
      '-NoProfile',
      '-Command',
      `Expand-Archive -Path '${zip.replace(/'/g, "''")}' -DestinationPath '${path.join(workDir, 'kenney_casino').replace(/'/g, "''")}' -Force`,
    ]);
  }
}

async function ensureUiClick() {
  const dest = path.join(workDir, 'click1.ogg');
  if (!fs.existsSync(dest)) {
    console.log('Downloading UI click...');
    await download(
      'https://gamesounds.xyz/Kenney%27s%20Sound%20Pack/UI%20Audio/click1.ogg',
      dest,
      'https://gamesounds.xyz/',
    );
  }
  return dest;
}

function runFfmpeg(args) {
  execFileSync(ffmpeg, args, { stdio: 'pipe' });
}

function toOgg(input, output, filter) {
  const args = ['-y', '-i', input];
  if (filter) args.push('-af', filter);
  args.push('-c:a', 'libvorbis', '-ar', '44100', '-q:a', '6', output);
  runFfmpeg(args);
}

async function main() {
  await ensureKenneyCasino();
  const uiClick = await ensureUiClick();

  const clickOut = path.join(outDir, 'slot_spin_click.ogg');
  const loopOut = path.join(outDir, 'slot_spin_loop.ogg');
  const landOut = path.join(outDir, 'slot_column_land.ogg');
  const winOut = path.join(outDir, 'slot_win.ogg');

  // Crisp button click
  toOgg(uiClick, clickOut, 'silenceremove=start_periods=1:start_silence=0.01:start_threshold=-40dB,loudnorm=I=-14:TP=-1:LRA=7');

  // Short single-hit column stop (trim chip lay to one transient)
  toOgg(
    path.join(kenneyCasino, 'chip-lay-1.ogg'),
    landOut,
    'atrim=0:0.18,asetpts=N/SR/TB,loudnorm=I=-12:TP=-0.5:LRA=7',
  );

  // Reel spin bed: dice shake excerpt, loop-friendly segment
  toOgg(
    path.join(kenneyCasino, 'dice-shake-1.ogg'),
    loopOut,
    'atrim=0:1.8,asetpts=N/SR/TB,loudnorm=I=-18:TP=-2:LRA=9',
  );

  console.log('Win sound: run node scripts/download-mixkit-win.mjs (Mixkit Casino bling achievement #2067)');

  for (const f of [clickOut, loopOut, landOut, winOut]) {
    const head = fs.readFileSync(f).subarray(0, 4).toString('ascii');
    console.log(`${path.basename(f)}: ${fs.statSync(f).size} bytes, magic=${head}`);
  }
  console.log('Done. Sources: Kenney CC0 casino + UI (gamesounds.xyz mirror).');
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
