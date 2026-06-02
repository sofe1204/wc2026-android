# Authentication (WC 2026)

The app supports **two cloud sign-in methods** (plus guest offline mode):

| Method | App flow | Firebase Console |
|--------|----------|------------------|
| **Email + password** | Sign up / Sign in form | Enable **Email/Password** |
| **Google** | Continue with Google | Enable **Google** + SHA-1 + `google-services.json` |

Both use the same Firebase project and call `ensureUserProfile` after login.

---

# Google Sign-In setup

| Setting | Value |
|---------|--------|
| Firebase project | `wc-2026-3110f` |
| GCP public project ID | `project-424696015515` |
| Project number | `424696015515` |
| Android package | `com.techmomentum.wc2026` |

## 1. Enable Google in Firebase

1. [Authentication → Sign-in method](https://console.firebase.google.com/project/wc-2026-3110f/authentication/providers)
2. Open **Google** → **Enable** → set support email → **Save**

## 2. Add SHA-1 (required for Android)

Debug keystore (local development):

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android 2>/dev/null | grep SHA1
```

1. Firebase Console → **Project settings** → your Android app **World Cup 2026**
2. **Add fingerprint** → paste SHA-1 → Save

## 3. Re-download `google-services.json`

After Google is enabled and SHA-1 is added, download a fresh file:

**Project settings → Your apps → World Cup 2026 → google-services.json**

Replace:

`android/app/google-services.json`

The file must include an `oauth_client` entry with `"client_type": 3` (Web client). The app reads this as `default_web_client_id`.

## 4. Rebuild

```bash
./scripts/sync_project.sh
cd android && ./gradlew assembleDebug
```

**Continue with Google** appears on the auth screen when the Web client ID is present.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| No Google button / setup hint shown | Re-download `google-services.json` with `oauth_client` populated |
| Error code 10 | Enable Google provider + add SHA-1 + fresh json |
| `operation not allowed` | Enable Google (and Email/Password if using both) in Sign-in method |
| Logcat `DEVELOPER_ERROR` + `RecaptchaCallWrapper` network error on **email** sign-in | Add SHA-1 (`./scripts/android_debug_sha.sh`), re-download json (needs `client_type": 1`), Google Play AVD + cold boot — see [`android/ANDROID_STUDIO.md`](../android/ANDROID_STUDIO.md) |
