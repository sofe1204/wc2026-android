/** Shared helpers for sticker image generation (fal.ai + seed JSON). */
import fs from "fs";
import path from "path";
import { createRequire } from "module";

const require = createRequire(import.meta.url);

/** T2I fallback (~$0.04/image). Kontext edit used when STICKER_MASTER_IMAGE_URL is set. */
export const DEFAULT_FAL_MODEL = "fal-ai/imagen4/preview";
export const DEFAULT_KONTEXT_MODEL = "fal-ai/flux-pro/kontext";
export const IMAGEN4_COST_PER_IMAGE = 0.04;

export function isGoalkeeper(position) {
  return String(position || "").toLowerCase() === "goalkeeper";
}

export function getMasterImageUrl(position) {
  const gk = process.env.STICKER_MASTER_GK_IMAGE_URL?.trim();
  const outfield = process.env.STICKER_MASTER_IMAGE_URL?.trim();
  if (isGoalkeeper(position) && gk) return gk;
  return outfield || null;
}

export function usesKontextEdit() {
  return Boolean(
    process.env.STICKER_MASTER_IMAGE_URL?.trim() ||
      process.env.STICKER_MASTER_GK_IMAGE_URL?.trim()
  );
}

export function getFalModel() {
  if (process.env.FAL_MODEL?.trim()) return process.env.FAL_MODEL.trim();
  if (usesKontextEdit()) return DEFAULT_KONTEXT_MODEL;
  return DEFAULT_FAL_MODEL;
}

export function falRunUrl(model = getFalModel()) {
  return `https://fal.run/${model}`;
}

function isImagenModel(model) {
  return model.includes("imagen");
}

function isKontextModel(model) {
  return model.includes("kontext");
}

const STYLE_CORE =
  "centered head and upper torso portrait, front-facing, neutral slight smile, " +
  "Pixar-inspired stylized 3D illustration, soft even studio lighting, " +
  "green football pitch background with white penalty-box lines, rectangular white sticker border, " +
  "glossy reflection on right, peeled sticker corner bottom-right, 3:4 vertical";

/** Jersey line — outfield vs goalkeeper. */
export function kitInstructions(countryName, position) {
  if (isGoalkeeper(position)) {
    return (
      `Jersey: ${countryName} national team goalkeeper kit, long-sleeve goalkeeper shirt, ` +
      `goalkeeper gloves visible, correct ${countryName} national team crest on chest, ` +
      `clearly a goalkeeper kit not an outfield shirt`
    );
  }
  return (
    `Jersey: ${countryName} national team outfield home kit, short-sleeve shirt, ` +
    `correct ${countryName} national team crest on left chest, not a goalkeeper kit`
  );
}

/** Text-to-image prompt; name, country, and kit type (GK vs outfield) vary. */
export function stickerPrompt(playerName, countryName, position = "Midfielder") {
  return (
    `${playerName} as a stylized Pixar-style sticker album portrait. ${STYLE_CORE}. ` +
    `${kitInstructions(countryName, position)}. ` +
    `Stylized inspired character not copied from a photo. No text, no watermark.`
  );
}

/** Kontext edit prompt — requires STICKER_MASTER_IMAGE_URL reference. */
export function kontextEditPrompt(playerName, countryName, position = "Midfielder") {
  return (
    `Use the reference image as the exact composition template. ` +
    `Keep identical framing, crop, sticker border, peeled corner, green pitch background, ` +
    `white lines, lighting, and Pixar stylized 3D style. Do not change camera angle or layout. ` +
    `Replace only the player with ${playerName}. ${kitInstructions(countryName, position)}. ` +
    `Do not reuse the reference jersey badge or colors; use the correct ${countryName} national crest. ` +
    `Front-facing portrait, neutral slight smile. No text, no watermark.`
  );
}

/** Pick T2I or Kontext edit prompt from player seed row + .env masters. */
export function promptForPlayer(player) {
  if (getMasterImageUrl(player.position)) {
    return kontextEditPrompt(player.playerName, player.countryName, player.position);
  }
  return stickerPrompt(player.playerName, player.countryName, player.position);
}

export function loadEnvFile(root) {
  const envPath = path.join(root, ".env");
  if (!fs.existsSync(envPath)) return;
  for (const line of fs.readFileSync(envPath, "utf8").split("\n")) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#")) continue;
    const eq = trimmed.indexOf("=");
    if (eq <= 0) continue;
    const key = trimmed.slice(0, eq).trim();
    let value = trimmed.slice(eq + 1).trim();
    if (
      (value.startsWith('"') && value.endsWith('"')) ||
      (value.startsWith("'") && value.endsWith("'"))
    ) {
      value = value.slice(1, -1);
    }
    if (process.env[key] === undefined) process.env[key] = value;
  }
}

/** @deprecated Use promptForPlayer(player). Kept for dry-run on stored prompts. */
export function buildPlayerPrompt(animeStickerPrompt) {
  return animeStickerPrompt;
}

export function buildEmblemPrompt(team) {
  const { countryName } = team;
  return (
    `${countryName} as a stylized Pixar-style collectible football team crest emblem sticker, ` +
    `symmetric heraldic badge, friendly epic foil feel, subtle blurred football stadium and green pitch background, ` +
    `collectible sticker look, 3:4 vertical, generic fictional crest not a real trademark logo, ` +
    `inspired stylized design not copied from a real logo, no text, no watermark`
  );
}

export function parseArgs(argv) {
  const out = {
    dryRun: false,
    force: false,
    skipUpload: false,
    playersOnly: false,
    emblemsOnly: false,
    limit: null,
    concurrency: 2,
    playerId: null,
    teamId: null,
  };
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a === "--dry-run") out.dryRun = true;
    else if (a === "--force") out.force = true;
    else if (a === "--skip-upload") out.skipUpload = true;
    else if (a === "--players-only") out.playersOnly = true;
    else if (a === "--emblems-only") out.emblemsOnly = true;
    else if (a === "--limit" && argv[i + 1]) out.limit = Number(argv[++i]);
    else if (a.startsWith("--limit=")) out.limit = Number(a.split("=")[1]);
    else if (a === "--concurrency" && argv[i + 1]) out.concurrency = Number(argv[++i]);
    else if (a.startsWith("--concurrency=")) out.concurrency = Number(a.split("=")[1]);
    else if (a === "--player-id" && argv[i + 1]) out.playerId = argv[++i];
    else if (a.startsWith("--player-id=")) out.playerId = a.split("=")[1];
    else if (a === "--team-id" && argv[i + 1]) out.teamId = argv[++i];
    else if (a.startsWith("--team-id=")) out.teamId = a.split("=")[1];
  }
  if (out.playersOnly && out.emblemsOnly) {
    throw new Error("Use only one of --players-only or --emblems-only");
  }
  return out;
}

export function readJson(filePath) {
  return JSON.parse(fs.readFileSync(filePath, "utf8"));
}

export function writeJson(filePath, data) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2) + "\n", "utf8");
}

export function writeSeedPair(seedPaths, fileName, data) {
  for (const dir of seedPaths) {
    writeJson(path.join(dir, fileName), data);
  }
}

export async function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

export async function fetchImageBuffer(url) {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`Failed to download image: ${res.status} ${url}`);
  return Buffer.from(await res.arrayBuffer());
}

/**
 * @param {string} prompt
 * @param {{ seed?: number }} opts
 */
function buildFalRequestBody(model, prompt, opts = {}) {
  let body;
  if (isKontextModel(model)) {
    const imageUrl = opts.masterUrl?.trim();
    if (!imageUrl) {
      throw new Error(
        "Kontext model requires STICKER_MASTER_IMAGE_URL (and optional STICKER_MASTER_GK_IMAGE_URL)"
      );
    }
    body = {
      prompt,
      image_url: imageUrl,
      aspect_ratio: "3:4",
      output_format: "jpeg",
      num_images: 1,
      guidance_scale: 3.5,
      enhance_prompt: false,
      safety_tolerance: "2",
    };
  } else if (isImagenModel(model)) {
    body = {
      prompt,
      aspect_ratio: "3:4",
      output_format: "jpeg",
      num_images: 1,
      resolution: "1K",
      safety_tolerance: "4",
    };
  } else {
    body = {
      prompt,
      image_size: "portrait_4_3",
      num_inference_steps: 4,
      guidance_scale: 3.5,
      enable_safety_checker: false,
      output_format: "jpeg",
      num_images: 1,
    };
  }
  if (opts.seed != null && Number.isFinite(opts.seed)) {
    body.seed = opts.seed;
  }
  return body;
}

export async function generateWithFal(prompt, opts = {}) {
  const falKey = process.env.FAL_KEY;
  if (!falKey) {
    throw new Error("FAL_KEY is not set. Add it to .env or export FAL_KEY=...");
  }

  const model = opts.model || getFalModel();
  const url = falRunUrl(model);
  const body = buildFalRequestBody(model, prompt, opts);

  let lastErr;
  for (let attempt = 0; attempt < 5; attempt++) {
    try {
      const res = await fetch(url, {
        method: "POST",
        headers: {
          Authorization: `Key ${falKey}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
      });
      const text = await res.text();
      let json;
      try {
        json = JSON.parse(text);
      } catch {
        throw new Error(`fal.ai returned non-JSON (${res.status}): ${text.slice(0, 200)}`);
      }
      if (!res.ok) {
        const msg = json.detail || json.message || text;
        throw new Error(`fal.ai ${res.status}: ${typeof msg === "string" ? msg : JSON.stringify(msg)}`);
      }
      const imageUrl = json?.images?.[0]?.url;
      if (!imageUrl) throw new Error("fal.ai response missing images[0].url");
      return { imageUrl, seed: json.seed, model };
    } catch (e) {
      lastErr = e;
      const retryable =
        String(e.message).includes("429") ||
        String(e.message).includes("503") ||
        String(e.message).includes("502");
      if (!retryable || attempt === 4) break;
      await sleep(2000 * (attempt + 1));
    }
  }
  throw lastErr;
}

export function initFirebaseAdmin(root, projectConfig) {
  let admin;
  try {
    admin = require(path.join(root, "functions/node_modules/firebase-admin"));
  } catch {
    throw new Error("Run: cd functions && npm install");
  }
  if (!admin.apps.length) {
    admin.initializeApp({
      projectId: projectConfig.firebase.projectId,
      storageBucket: projectConfig.firebase.storageBucket,
    });
  }
  return admin;
}

export async function uploadToStorage(bucket, storagePath, buffer, contentType) {
  const file = bucket.file(storagePath);
  await file.save(buffer, {
    metadata: {
      contentType,
      cacheControl: "public, max-age=31536000",
    },
  });
  try {
    await file.makePublic();
  } catch {
    // Bucket may already allow public reads via IAM.
  }
  return `https://storage.googleapis.com/${bucket.name}/${storagePath}`;
}

export async function runPool(items, concurrency, worker) {
  const results = [];
  let index = 0;

  async function runOne() {
    while (index < items.length) {
      const i = index++;
      results[i] = await worker(items[i], i);
    }
  }

  const workers = Array.from({ length: Math.min(concurrency, items.length) }, () => runOne());
  await Promise.all(workers);
  return results;
}
