# Go live — sticker images + Firestore catalog

One-time production setup so **signed-in users** see stickers from Firestore and **guests** see bundled seed (rebuild APK after images).

## Before you start (tonight or tomorrow morning)

1. **fal.ai** — create API key, load **~$60** credits ([dashboard](https://fal.ai/dashboard)).
2. **Firebase** — `firebase login` (for deploy/seed if not done).
3. **Google Cloud auth** for Storage upload:
   ```bash
   gcloud auth application-default login
   ```
   Or set `GOOGLE_APPLICATION_CREDENTIALS` to a service account JSON with Storage + Firestore access.

4. **Repo setup:**
   ```bash
   cd "/Users/sofe/Desktop/Workspace/WC app"
   cp .env.example .env
   # Edit .env — set FAL_KEY=...
   cd functions && npm install && cd ..
   ```

5. **Preflight:**
   ```bash
   npm run go-live:check
   ```

## Tomorrow — generate all images

### Option A — Panini scans + Grok edit (recommended if you have `wc26/`)

Place Panini scan PNGs under `wc26/{country}/{player name}.png` (48 country folders, ~864 files). The script matches filenames to seed `playerId`, uploads each scan, and calls **Grok Imagine edit** on fal.ai to Pixar-ify the face and remove Panini branding.

```bash
npm run edit:stickers-grok -- --dry-run          # match report, no API calls
npm run go-live:edit-grok:pilot                 # 5 players
npm run go-live:edit-grok                       # all matched (~864, skips players without a file)
```

~**$0.022/image** → ~**$19** for 864 edits. Players with no `wc26/` file are skipped (~384). Progress: `data/grok_sticker_edit_progress.json`.

### Option B — Text-to-image / Kontext (no Panini scans)

**Recommended:** upload a master outfield sticker (+ optional GK master) to Storage, set `STICKER_MASTER_IMAGE_URL` in `.env` → script uses **FLUX Kontext edit** for consistent album layout. Goalkeepers get a **goalkeeper kit** prompt; outfield get **short-sleeve kit** + correct country crest.

Without master URLs: falls back to **Imagen 4** text-to-image (~**$0.04/image**, less consistent).

### Step 1 — Pilot (5 images)

```bash
npm run go-live:images:pilot
```

Open a few URLs in seed JSON or Firebase Storage (`stickers/players/…`). If quality is wrong, stop and adjust prompts before the full run.

### Step 2 — All players (~1,248)

```bash
npm run go-live:images
```

Resumes automatically: skips players that already have `imageUrl`. Expect **~2–4 hours** at concurrency 2.

### Step 3 — Slot machine symbols (7)

```bash
npm run upload:slot-symbols -- --dry-run
npm run upload:slot-symbols
```

Reel art from `slots/` (Messi, Ronaldo, Mbappé, Neymar, Salah, Kane, Trophy). Trophy is a **wildcard** on any winning line.

### Step 4 — Team emblems (48)

**If you have local logos** in `logos done/` (recommended):

```bash
npm run upload:emblems -- --dry-run
npm run upload:emblems
```

**Or** generate crests via fal.ai:

```bash
npm run go-live:images:emblems
```

### If a run stops mid-way

Re-run the same command; only missing `imageUrl` rows are processed.

### Force regenerate one player

```bash
npm run generate:sticker-images -- --player-id mexico_raul_rangel --force
```

## Tomorrow — publish catalog for real users

### Step 5 — Deploy storage rules (once, if never deployed)

```bash
npm run deploy:storage
```

### Step 6 — Backend (once, if not already live)

```bash
npm run setup:auth-users
```

This deploys Firestore rules + Cloud Functions and seeds Firestore **without** new images. After images exist, re-seed:

### Step 7 — Push catalog with image URLs

```bash
npm run go-live:publish
```

Equivalent to `npm run seed:firestore` (merge-updates `teams`, `players`, `stickers`).

### Step 8 — Android app

Rebuild and install so guests get updated assets:

```bash
cd android && ./gradlew installDebug
```

Signed-in users: sign **out and back in** after seeding if the album looked cached.

## Quick reference

| Command | What it does |
|---------|----------------|
| `npm run go-live:check` | Preflight (FAL_KEY, deps, cost estimate) |
| `npm run edit:stickers-grok -- --dry-run` | Match `wc26/` files to players (no API) |
| `npm run go-live:edit-grok:pilot` | 5 Grok edits from Panini scans |
| `npm run go-live:edit-grok` | All matched scans → Grok edit → Storage |
| `npm run go-live:images:pilot` | 5 player test images (Imagen/Kontext) |
| `npm run go-live:images` | All players → Storage + seed JSON |
| `npm run upload:emblems` | Upload `logos done/` → Storage + seed |
| `npm run upload:slot-symbols` | Upload `slots/` reel art → Storage + seed |
| `npm run go-live:images:emblems` | 48 AI-generated crests (fal) |
| `npm run go-live:publish` | Firestore seed for signed-in users |
| `npm run deploy:storage` | Public read on `stickers/`, `emblems/` |

## Environment (.env)

| Variable | Required | Notes |
|----------|----------|--------|
| `FAL_KEY` | Yes | fal.ai API key |
| `STICKER_SOURCE_DIR` | No | Panini scan root (default `wc26/`) |
| `FAL_MODEL` | No | Default `fal-ai/imagen4/preview` (Imagen/Kontext path only) |
| `STICKER_IMAGE_SEED` | No | Optional fixed seed |
| `GOOGLE_APPLICATION_CREDENTIALS` | For upload | Or use `gcloud auth application-default login` |

## Costs (realistic)

| Item | Approx |
|------|--------|
| Grok edit × ~864 wc26 scans | **~$19** |
| Imagen 4 × ~1,296 images | **$52** |
| Retries / pilots | **+$5–10** |
| Firebase Storage | negligible |
| **Grok path budget** | **~$25** |
| **Imagen path budget** | **~$60** |

## Troubleshooting

- **Missing FAL_KEY** — `.env` in repo root, not `functions/`.
- **Could not load default credentials** — run `gcloud auth application-default login`.
- **fal 429** — lower concurrency: `--concurrency 1`.
- **Imagen deprecated on fal** — set `FAL_MODEL=fal-ai/imagen4/preview/fast` in `.env` and retry.
- **Images in app but album empty (signed-in)** — run `npm run go-live:publish`.
- **Guest sees no images** — rebuild APK after seed JSON updated.

## Licensing

Settings notes: review branding and stylized likeness before store release. Prompts use generic “inspired” wording, not photo copies.
