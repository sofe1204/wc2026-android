# World Cup 2026 — Sticker Album

Anime-style digital sticker album for the 2026 FIFA World Cup (USA, Canada, Mexico).  
**48 teams × 15 players = 720 collectible stickers.**

## Stack

- **Android:** Kotlin, Jetpack Compose, Material 3, Hilt, MVVM, Coil, AdMob (rewarded)
- **Backend:** Firebase Auth, Firestore, Cloud Functions, Storage
- **Rewards:** Server-authoritative (packs, daily claims, slot machine, ownership)

## Project structure

```text
WC app/
├── android/                 # Android Studio project
├── functions/               # Cloud Functions (TypeScript)
├── scripts/                 # Seed generator
├── firestore.rules
└── README.md
```

## Sync (run after pulling changes)

```bash
./scripts/sync_project.sh
```

See [`SYNC.md`](SYNC.md) and [`project.config.json`](project.config.json).

## Prerequisites

- Android Studio (Ladybug+), JDK 17
- Node.js 20, Firebase CLI (`npm i -g firebase-tools`)
- A Firebase project with **Email/Password** auth enabled

### Run in Android Studio

1. Open folder **`android/`** (not the repo root) — see [`android/ANDROID_STUDIO.md`](android/ANDROID_STUDIO.md).
2. Optional check: `./scripts/prepare_android_studio.sh`
3. Sync Gradle → Run **app** on an emulator (API 26+).
4. **Guest mode** works immediately without Firebase.

## Setup

### 1. Firebase project (`wc-2026-3110f`)

1. Project: **wc-2026-3110f** (see [`.firebaserc`](.firebaserc)).
2. Add an Android app with package `com.techmomentum.wc2026`.
3. Download `google-services.json` → replace  
   [`android/app/google-services.json`](android/app/google-services.json)  
   (template: [`google-services.json.example`](android/app/google-services.json.example)).
4. Enable sign-in providers in Firebase → **Authentication** → **Sign-in method**:  
   - **Email/Password** → Enable  
   - **Google** → Enable (GCP project `project-424696015515`) — see [`docs/GOOGLE_SIGN_IN.md`](docs/GOOGLE_SIGN_IN.md)  
   Direct link: [Authentication providers](https://console.firebase.google.com/project/wc-2026-3110f/authentication/providers)  
   After enabling Google, add debug **SHA-1** and re-download `google-services.json`.

The Android app wires Firebase automatically:

- **Cloud Functions** region: `us-central1` (matches `functions/`)
- **Auth** → Firestore profile + `user_stickers` listeners
- **Rewards** → HTTPS callables (`ensureUserProfile`, packs, daily, slot)
- **Catalog** → Firestore `teams` / `players` / `stickers` (falls back to bundled seed JSON)

**Local emulators** (optional): in `android/local.properties` set `firebase.emulators=true`, then:

```bash
firebase emulators:start
```

See Settings → Firebase connection for live status in the app.

### 2. SQL Connect (Cloud SQL)

Provisioning in Firebase Console uses:

- **Location:** `us-east4`
- **Instance:** `wc-2026-3110f-instance`
- **Database:** `wc-2026-3110f-database`
- **Service:** `wc-2026-3110f-service`

Repo config is in [`dataconnect/`](dataconnect/). After provisioning finishes:

```bash
firebase use wc-2026-3110f
firebase dataconnect:sql:diff
firebase dataconnect:sql:migrate
firebase deploy --only dataconnect
```

See [`dataconnect/README.md`](dataconnect/README.md) for details.

### 3. Deploy rules & functions (required for packs/rewards)

```bash
./scripts/deploy_functions.sh
```

Without this step, sign-in still works (Firestore bootstrap), but opening packs and daily rewards need Cloud Functions.

Manual equivalent:

```bash
cd "/Users/sofe/Desktop/Workspace/WC app"
firebase login
firebase use wc-2026-3110f
cd functions && npm install && npm run build && cd ..
firebase deploy --only firestore:rules,firestore:indexes,functions
```

### 4. Admin custom claim (for seeding)

```bash
# Set ADMIN_UID in Firebase Auth user you control
firebase functions:shell
# Or use Admin SDK script to set custom claim: admin: true
```

Example Node one-liner (run with service account):

```js
await admin.auth().setCustomUserClaims("YOUR_UID", { admin: true });
```

### 5. Seed Firestore (admin only)

From the app **Profile** screen (debug build) tap **Seed Firestore**, or call:

- `seedTeams`
- `seedPlayers`
- `seedStickers`

Seed JSON lives in:

- [`android/app/src/main/assets/seed/`](android/app/src/main/assets/seed/)
- [`functions/seed/`](functions/seed/) (copied for Cloud Functions)

Regenerate seeds:

```bash
python3 scripts/generate_seed_data.py
```

### 6. Run Android app

Open the `android/` folder in Android Studio, sync Gradle, then **Run ▶ app (debug)**.

From terminal:

```bash
cd android
./gradlew assembleDebug
# or install on connected device/emulator:
./gradlew installDebug
```

**No Firebase yet?** Tap **Continue as Guest (offline demo)** on the login screen. The full album (48 teams, 720 stickers), pack opening, daily rewards, and slot machine work locally with seed data.

### 7. Emulators (optional)

```bash
firebase emulators:start
```

Point the app to emulators in debug (add to `AppModule` if needed):

```kotlin
Firebase.firestore.useEmulator("10.0.2.2", 8080)
Firebase.functions.useEmulator("10.0.2.2", 5001)
Firebase.auth.useEmulator("10.0.2.2", 9099)
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

## TODO — before production

- [ ] **Legal/licensing:** Review use of “World Cup 2026” title and football branding; no official FIFA logos in app assets.
- [ ] **Squads:** Verify all 720 player names against final 2026 rosters (`scripts/generate_seed_data.py`).
- [ ] **Images:** Bulk-generate anime stickers from `animeStickerPrompt`; upload to Storage; set `imageUrl`.
- [ ] **Emblems:** Replace placeholder badges with custom `customEmblemUrl` art (not copied federation crests).
- [ ] **AdMob SSV:** Server-Side Verification for rewarded ads.
- [ ] Replace AdMob test unit IDs in release builds.

## License

Private / unlicensed for distribution until legal review is complete.
