# wc2026-android — World Cup 2026 Sticker Album

Anime-style digital sticker album for the 2026 FIFA World Cup (USA, Canada, Mexico).  
**48 teams × 26 players (official FIFA squads) = 1,248 collectible stickers** (+ 48 team crests).

| | |
|---|---|
| **Package** | `com.techmomentum.wc2026` |
| **Firebase project** | `wc-2026-3110f` |
| **Android Studio** | Open the [`android/`](android/) folder (not repo root) |

## Quick start

```bash
git clone https://github.com/sofe1204/wc2026-android.git
cd wc2026-android
./scripts/sync_project.sh
```

1. Copy Firebase **`google-services.json`** → `android/app/google-services.json`  
   (template: [`android/app/google-services.json.example`](android/app/google-services.json.example))
2. Open **`android/`** in Android Studio → Sync Gradle → Run **app**
3. **No Firebase yet?** Tap **Continue as Guest (offline demo)** on the login screen.

Full Studio steps: [`android/ANDROID_STUDIO.md`](android/ANDROID_STUDIO.md)

## Stack

- **Android:** Kotlin, Jetpack Compose, Material 3, Hilt, MVVM, Coil, AdMob (rewarded)
- **Backend:** Firebase Auth, **Firestore**, Cloud Functions, Storage
- **Optional:** Firebase SQL Connect (Cloud SQL) — catalog; off by default in app
- **Rewards:** Server-authoritative (packs, daily claims, slot machine, ownership)

## Project structure

```text
wc2026-android/
├── android/                 # ← Open this in Android Studio
├── functions/               # Cloud Functions (TypeScript, us-central1)
├── dataconnect/             # SQL Connect schema (optional)
├── firestore.rules          # Security rules (deploy via script)
├── scripts/                 # sync, deploy, seed generator
├── docs/                    # Google Sign-In, etc.
└── project.config.json      # Single source of truth (run sync_project.sh)
```

## Firebase checklist (production)

Complete these in [Firebase Console](https://console.firebase.google.com/project/wc-2026-3110f) before cloud sign-in works end-to-end:

| Step | Where | Notes |
|------|--------|--------|
| Android app | Project settings | Package `com.techmomentum.wc2026`, download `google-services.json` |
| Debug SHA-1 | Project settings → fingerprints | `./scripts/android_debug_sha.sh` — required for Google Sign-In |
| Auth providers | Authentication → Sign-in method | Enable **Email/Password** and **Google** |
| **Firestore** | Build → Firestore Database | **Create database** `(default)` — separate from SQL Connect |
| Rules + functions | Terminal | `./scripts/deploy_functions.sh` |

**SQL Connect** (Cloud SQL in `us-east4`) is optional and does **not** replace Firestore for user profiles and rewards.

- Google Sign-In: [`docs/GOOGLE_SIGN_IN.md`](docs/GOOGLE_SIGN_IN.md)
- Config sync: [`SYNC.md`](SYNC.md)

## Deploy backend

```bash
./scripts/deploy_functions.sh
```

Deploys Firestore rules, indexes, and Cloud Functions to `wc-2026-3110f`.  
**Do not** paste `deploy_functions.sh` into the Firestore Rules editor — use [`firestore.rules`](firestore.rules) or let the CLI deploy it.

## Official squads (FIFA WC 2026)

Seed data comes from **`data/official_squads_2026.json`** (48 teams, 26 players each, 1,248 total).

```bash
# Optional: re-fetch from Wikipedia
npm run import:official-squads

# Regenerate squads.csv + Android/Functions seed JSON
npm run generate:seed
./scripts/sync_project.sh
```

## Sync (after pull)

```bash
./scripts/sync_project.sh
```

Regenerates `ProjectConfig.kt`, `projectConfig.ts`, and related constants from [`project.config.json`](project.config.json).

## Prerequisites

- Android Studio (Ladybug+), JDK 17
- Node.js 20, Firebase CLI (`npm i -g firebase-tools`)

### Run in Android Studio

1. Open folder **`android/`** — see [`android/ANDROID_STUDIO.md`](android/ANDROID_STUDIO.md)
2. Optional: `./scripts/prepare_android_studio.sh`
3. Sync Gradle → Run **app** on emulator (API 26+, **Google Play** image recommended)
4. **Guest mode** works without Firebase

## Setup (detailed)

### 1. Firebase project (`wc-2026-3110f`)

1. Project: **wc-2026-3110f** (see [`.firebaserc`](.firebaserc)).
2. Add an Android app with package `com.techmomentum.wc2026`.
3. Download `google-services.json` → `android/app/google-services.json`.
4. Enable **Email/Password** and **Google** in Authentication → Sign-in method.  
   [Authentication providers](https://console.firebase.google.com/project/wc-2026-3110f/authentication/providers)  
   After Google: add debug **SHA-1**, re-download `google-services.json` (should include `oauth_client` with `client_type: 1`).

The app uses:

- **Cloud Functions** region: `us-central1`
- **Auth** → Firestore profile + `user_stickers`
- **Rewards** → callables (`ensureUserProfile`, packs, daily, slot)
- **Catalog** → Firestore or bundled seed JSON (guest)

**Local emulators** (optional): `firebase.emulators=true` in `android/local.properties`, then `firebase emulators:start`.

### 2. SQL Connect (optional)

- **Location:** `us-east4`
- **Instance:** `wc-2026-3110f-instance`
- **Database:** `wc-2026-3110f-database`
- **Service:** `wc-2026-3110f-service`

See [`dataconnect/README.md`](dataconnect/README.md). App flag `USE_SQL_CONNECT` is `false` until you generate the SDK and deploy.

### 3. Admin seeding

Set Firebase Auth custom claim `admin: true` on your user, then use **Profile → Seed Firestore** (debug) or call `seedTeams` / `seedPlayers` / `seedStickers`.

Regenerate seed JSON:

```bash
python3 scripts/generate_seed_data.py
```

### 4. Build from terminal

```bash
cd android
./gradlew assembleDebug
./gradlew installDebug
```

## Cloud Functions

| Callable | Description |
|----------|-------------|
| `ensureUserProfile` | Creates user doc + 2 signup packs |
| `openStickerPack` | Opens 1 pack (5 stickers, rarity weights) |
| `claimDailyPacks` | +2 packs/day (UTC) |
| `claimRewardedAdPack` | +1 pack/day after ad |
| `spinSlotMachine` | 3×3 slot spin, win → pack (max 5/day) |
| `claimRewardedSlotSpins` | +5 spins after ad |
| `seedTeams` / `seedPlayers` / `seedStickers` | Admin batch import |

## Game constants

| Constant | Value |
|----------|-------|
| Stickers per pack | 5 |
| Signup packs | 2 |
| Daily packs | 2 |
| Daily ad pack | 1 |
| Daily slot spins | 15 |
| Ad slot spins | 5 |
| Max slot packs/day | 5 |

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|----------------|-----|
| Email sign-in “network error” but browser works | Play Services / reCAPTCHA / SHA-1 | Add debug SHA-1, Google Play AVD, cold boot |
| `DEVELOPER_ERROR` in logcat | Missing SHA-1 in Firebase | `./scripts/android_debug_sha.sh` |
| `database (default) does not exist` | Firestore not created | Firebase → Build → Firestore → Create database |
| Rules editor syntax errors on `!` or `#` | Pasted shell script by mistake | Deploy with `./scripts/deploy_functions.sh` or paste only `firestore.rules` |
| Auth works, profile fails | Firestore empty / rules not deployed | Create Firestore + run deploy script |

## TODO — before production

- [ ] Legal/licensing review for “World Cup 2026” branding
- [ ] Re-run `npm run import:official-squads` if FIFA squads change mid-tournament
- [ ] Sticker images + Storage URLs
- [ ] AdMob SSV + production ad unit IDs

## License

Private / unlicensed for distribution until legal review is complete.
