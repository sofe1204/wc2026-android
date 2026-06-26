#!/usr/bin/env bash
# Print release keystore SHA-1/SHA-256 for Firebase Console → Android app → Add fingerprint.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROPS="${ROOT}/android/keystore.properties"
if [[ ! -f "${PROPS}" ]]; then
  echo "Missing ${PROPS} — copy from android/keystore.properties.example"
  exit 1
fi
STORE_FILE="$(grep '^storeFile=' "${PROPS}" | cut -d= -f2-)"
STORE_PASS="$(grep '^storePassword=' "${PROPS}" | cut -d= -f2-)"
KEY_ALIAS="$(grep '^keyAlias=' "${PROPS}" | cut -d= -f2-)"
KEY_PASS="$(grep '^keyPassword=' "${PROPS}" | cut -d= -f2-)"
if [[ ! -f "${STORE_FILE}" ]]; then
  echo "Keystore not found: ${STORE_FILE}"
  exit 1
fi
echo "Add these in Firebase Console → Project settings → World Cup 2026 → Add fingerprint:"
echo "(Required for Google Sign-In on release / Play Store builds.)"
echo ""
keytool -list -v \
  -keystore "${STORE_FILE}" \
  -alias "${KEY_ALIAS}" \
  -storepass "${STORE_PASS}" \
  -keypass "${KEY_PASS}" 2>/dev/null | grep -E "SHA1:|SHA256:"
echo ""
echo "Then re-download google-services.json → android/app/google-services.json and rebuild."
