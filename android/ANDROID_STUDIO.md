# Run in Android Studio

## 1. Open the correct folder

**File → Open** and select this directory (not the monorepo root):

```text
WC app/android/
```

Gradle root is `android/`. Opening `WC app/` will not show the app module.

## 2. Sync project config

From repo root (keeps Firebase IDs, seeds, and constants aligned):

```bash
./scripts/sync_project.sh
```

See [`../SYNC.md`](../SYNC.md).

## 3. First-time setup

| Step | Action |
|------|--------|
| JDK | **Settings → Build → Gradle → Gradle JDK** → **17** (Embedded JDK 17 is fine) |
| SDK | **Settings → Languages & Frameworks → Android SDK** → install **API 35** platform + **Build-Tools 35** |
| Sync | **File → Sync Project with Gradle Files** (or elephant icon) |
| Device | **Device Manager** → create emulator **API 26+** (recommended API 34) |

Optional: copy [`local.properties.example`](local.properties.example) to `local.properties` if missing. Android Studio usually creates `local.properties` with `sdk.dir` automatically.

```bash
# From repo root — verify build before opening Studio
./scripts/prepare_android_studio.sh
```

## 4. Run the app

1. Wait for Gradle sync to finish (no errors in **Build** tool window).
2. Select run configuration **app** (or **WorldCup2026.app**).
3. Choose an emulator or USB device.
4. Click **Run** (green play).

Launcher: `com.techmomentum.wc2026.MainActivity`

## 5. Test without Firebase (fastest)

On the auth screen tap **Continue as Guest (offline demo)**.

- Full album, packs, slot, profile using bundled seed JSON
- No `google-services.json` or backend required

## 6. Test with Firebase

1. Replace [`app/google-services.json`](app/google-services.json) from Firebase Console (project `wc-2026-3110f`).
2. Enable **Email/Password** in Firebase Authentication.
3. **Create Firestore:** Firebase Console → **Build** → **Firestore Database** → **Create database** (same project `wc-2026-3110f`). Without this, logcat shows `NOT_FOUND: The database (default) does not exist` and profile setup fails after sign-in.
4. Deploy functions + rules: `npm run functions:deploy` (from repo root).
5. **Seed Firestore catalog** (required for packs/slots for signed-in users): set `GOOGLE_APPLICATION_CREDENTIALS` to a service account JSON, then `npm run seed:firestore`. Or run the full script: `npm run setup:auth-users`.
6. Sign up / sign in in the app (not Guest). Sign out/in after changing admin claims.
7. **Settings** shows Firebase connection status.

**Emulators:** set `firebase.emulators=true` in `local.properties`, run `firebase emulators:start` from repo root.

## 7. Troubleshooting

| Issue | Fix |
|-------|-----|
| Gradle sync failed | **File → Invalidate Caches → Restart**; run `./gradlew --stop` then sync again |
| SDK location not found | Create `android/local.properties` with `sdk.dir=/path/to/Android/sdk` |
| Hilt / Kapt errors | Use JDK **17**, Kotlin **2.3** (already in project) |
| INSTALL_FAILED | Uninstall old `com.techmomentum.wc2026` from device, run again |
| Firebase auth errors | Replace placeholder `google-services.json`; check package `com.techmomentum.wc2026` |
| Logcat: `DEVELOPER_ERROR`, `Unknown calling package name 'com.google.android.gms'`, `RecaptchaCallWrapper` + “network error” on email sign-in | **Add debug SHA-1** in Firebase (see below), **re-download** `google-services.json`, use a **Google Play** AVD (API 34 is more stable than 36), **Cold Boot** emulator |

### Email sign-in “network error” with Wi‑Fi on (Play Services)

Logcat often shows all of these together:

- `GoogleApiManager` → `ConnectionResult{statusCode=DEVELOPER_ERROR}`
- `SecurityException: Unknown calling package name 'com.google.android.gms'`
- `RecaptchaCallWrapper` → `signInWithPassword` → network error (~10s timeout)

Wi‑Fi in settings can still be fine. Firebase Auth is failing **app verification** (Play Services / reCAPTCHA), not because the emulator has no internet.

1. **Add your debug SHA-1** to Firebase → Project settings → Android app → **Add fingerprint**:

   ```bash
   ./scripts/android_debug_sha.sh
   ```

2. **Re-download** `android/app/google-services.json`. The file should include an `oauth_client` with `"client_type": 1` (Android), not only `"client_type": 3` (Web).

3. **Rebuild** and reinstall the app (`./gradlew installDebug` or Run in Studio).

4. **Emulator:** Device Manager → image with **Google Play** icon → **Cold Boot Now**. If API 36 still fails, create an **API 34** Google Play AVD.

5. **App Check** log `No AppCheckProvider installed` is expected in debug; it is not the cause of this failure.

## 8. Build variants

| Variant | Use |
|---------|-----|
| **debug** | Daily development (default) |
| **release** | Release testing; swap AdMob IDs before store |

Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`
