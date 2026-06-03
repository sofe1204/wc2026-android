# Project sync

All modules share one config file: **[`project.config.json`](project.config.json)**

It defines Firebase project IDs, Data Connect service, game balance numbers, emulator ports, and seed paths.

## Sync command

```bash
./scripts/sync_project.sh
# or
npm run sync
# or from android/
./gradlew syncProject
```

This will:

1. Validate `.firebaserc`, `dataconnect/`, `google-services.json`, and connector paths match the config
2. Verify Android and Functions seed JSON are **byte-identical**
3. Regenerate `functions/src/projectConfig.ts`
4. Regenerate `android/.../config/ProjectConfig.kt`

## When to run sync

| Change | Action |
|--------|--------|
| Edit `project.config.json` | `./scripts/sync_project.sh` then rebuild Android + Functions |
| Regenerate squads / stickers | `python3 scripts/generate_seed_data.py` (or `npm run generate:seed`) then `./scripts/sync_project.sh` |
| Refresh official FIFA squads | `npm run import:official-squads` then `npm run generate:seed` |
| New Firebase app / package | Update `project.config.json` + `google-services.json`, then sync |
| Before opening Android Studio | `./scripts/prepare_android_studio.sh` (includes build) |

## What stays aligned

| Area | Source |
|------|--------|
| Firebase project `wc-2026-3110f` | `project.config.json` → Gradle `BuildConfig`, `.firebaserc` |
| Android package `com.techmomentum.wc2026` | `project.config.json` → `applicationId`, `namespace` |
| Cloud Functions region | `project.config.json` → Android + `functions/src/index.ts` |
| Game constants (packs, spins) | `project.config.json` → `functions/src/projectConfig.ts`, `GameConstants.kt` |
| Seed JSON | `generate_seed_data.py` writes to **both** Android assets and `functions/seed/` |
| Data Connect IDs | `project.config.json` ↔ `dataconnect/dataconnect.yaml` |
| Emulator ports | `project.config.json` ↔ `firebase.json` emulators |

## Manual file (not auto-generated)

- **`android/app/google-services.json`** — download from Firebase Console; sync only checks `project_id` and package name.
