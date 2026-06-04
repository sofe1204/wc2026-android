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

Default model: **Imagen 4** (`fal-ai/imagen4/preview`), **3:4**, ~**$0.04/image** (~**$52** for 1,296 stickers).

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

### Step 3 — Team emblems (48)

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

### Step 4 — Deploy storage rules (once, if never deployed)

```bash
npm run deploy:storage
```

### Step 5 — Backend (once, if not already live)

```bash
npm run setup:auth-users
```

This deploys Firestore rules + Cloud Functions and seeds Firestore **without** new images. After images exist, re-seed:

### Step 6 — Push catalog with image URLs

```bash
npm run go-live:publish
```

Equivalent to `npm run seed:firestore` (merge-updates `teams`, `players`, `stickers`).

### Step 7 — Android app

Rebuild and install so guests get updated assets:

```bash
cd android && ./gradlew installDebug
```

Signed-in users: sign **out and back in** after seeding if the album looked cached.

## Quick reference

| Command | What it does |
|---------|----------------|
| `npm run go-live:check` | Preflight (FAL_KEY, deps, cost estimate) |
| `npm run go-live:images:pilot` | 5 player test images |
| `npm run go-live:images` | All players → Storage + seed JSON |
| `npm run go-live:images:emblems` | 48 crests |
| `npm run go-live:publish` | Firestore seed for signed-in users |
| `npm run deploy:storage` | Public read on `stickers/`, `emblems/` |

## Environment (.env)

| Variable | Required | Notes |
|----------|----------|--------|
| `FAL_KEY` | Yes | fal.ai API key |
| `FAL_MODEL` | No | Default `fal-ai/imagen4/preview` |
| `STICKER_IMAGE_SEED` | No | Optional fixed seed |
| `GOOGLE_APPLICATION_CREDENTIALS` | For upload | Or use `gcloud auth application-default login` |

## Costs (realistic)

| Item | Approx |
|------|--------|
| Imagen 4 × ~1,296 images | **$52** |
| Retries / pilots | **+$5–10** |
| Firebase Storage | negligible |
| **Total fal budget** | **~$60** |

## Troubleshooting

- **Missing FAL_KEY** — `.env` in repo root, not `functions/`.
- **Could not load default credentials** — run `gcloud auth application-default login`.
- **fal 429** — lower concurrency: `--concurrency 1`.
- **Imagen deprecated on fal** — set `FAL_MODEL=fal-ai/imagen4/preview/fast` in `.env` and retry.
- **Images in app but album empty (signed-in)** — run `npm run go-live:publish`.
- **Guest sees no images** — rebuild APK after seed JSON updated.

## Licensing

Settings notes: review branding and stylized likeness before store release. Prompts use generic “inspired” wording, not photo copies.
