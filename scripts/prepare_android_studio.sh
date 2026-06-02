#!/usr/bin/env bash
# Verifies the Android project is ready to open in Android Studio.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ANDROID="$ROOT/android"
DEFAULT_SDK="$HOME/Library/Android/sdk"

echo "==> World Cup 2026 — Android Studio prep"
echo "    Project: $ANDROID"
echo ""

if [[ -f "$ROOT/scripts/sync_project.py" ]]; then
  python3 "$ROOT/scripts/sync_project.py" || exit 1
  echo ""
fi

# local.properties
if [[ ! -f "$ANDROID/local.properties" ]]; then
  if [[ -d "$DEFAULT_SDK" ]]; then
    echo "sdk.dir=$DEFAULT_SDK" > "$ANDROID/local.properties"
    echo "Created android/local.properties (sdk.dir=$DEFAULT_SDK)"
  else
    echo "WARN: android/local.properties missing and SDK not at $DEFAULT_SDK"
    echo "      Open Android Studio once — it will create local.properties."
  fi
else
  echo "OK: android/local.properties exists"
fi

# Gradle wrapper
if [[ ! -x "$ANDROID/gradlew" ]]; then
  chmod +x "$ANDROID/gradlew"
  echo "Fixed gradlew executable bit"
fi

# google-services.json
if grep -q "REPLACE_WITH_YOUR_FIREBASE_API_KEY" "$ANDROID/app/google-services.json" 2>/dev/null; then
  echo "NOTE: google-services.json is still a placeholder — guest mode works; sign-in needs real file."
else
  echo "OK: google-services.json looks configured"
fi

echo ""
echo "==> Gradle sync + debug APK (this may take a few minutes on first run)"
cd "$ANDROID"
./gradlew --stop 2>/dev/null || true
./gradlew assembleDebug

echo ""
echo "==> Ready for Android Studio"
echo "    Open folder: $ANDROID"
echo "    See: android/ANDROID_STUDIO.md"
