/** Shared helpers for sticker image generation (fal.ai + seed JSON). */
import fs from "fs";
import path from "path";
import { createRequire } from "module";

const require = createRequire(import.meta.url);

/** Default: Imagen 4 preview (~$0.04/image, 3:4). Override with FAL_MODEL in .env. */
export const DEFAULT_FAL_MODEL = "fal-ai/imagen4/preview";
export const IMAGEN4_COST_PER_IMAGE = 0.04;

export function getFalModel() {
  return (process.env.FAL_MODEL || DEFAULT_FAL_MODEL).trim();
}

export function falRunUrl(model = getFalModel()) {
  return `https://fal.run/${model}`;
}

function isImagenModel(model) {
  return model.includes("imagen");
}

/** Pixar-style album prompt; only player name and country vary. */
export function stickerPrompt(playerName, countryName) {
  return (
    `${playerName} as a stylized Pixar-style sticker album portrait, head and upper torso only, ` +
    `wearing ${countryName} football jersey, friendly confident expression, detailed hair and looks, ` +
    `subtle blurred football stadium and green pitch background, collectible sticker look, 3:4 vertical, ` +
    `inspired stylized character not copied from a photo, no text, no watermark`
  );
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

/** Seed JSON already contains the full prompt; pass through unchanged. */
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
  if (isImagenModel(model)) {
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

  const model = getFalModel();
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
