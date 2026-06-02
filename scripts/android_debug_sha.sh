#!/usr/bin/env bash
# Print debug keystore SHA-1/SHA-256 for Firebase Console → Android app → Add fingerprint.
set -euo pipefail
KEYSTORE="${HOME}/.android/debug.keystore"
if [[ ! -f "${KEYSTORE}" ]]; then
  echo "Debug keystore not found: ${KEYSTORE}"
  echo "Run the app once from Android Studio to create it."
  exit 1
fi
echo "Add these in Firebase Console → Project settings → World Cup 2026 → Add fingerprint:"
echo ""
keytool -list -v -keystore "${KEYSTORE}" -alias androiddebugkey -storepass android -keypass android 2>/dev/null | grep -E "SHA1:|SHA256:"
echo ""
echo "Then re-download google-services.json → android/app/google-services.json and rebuild."
