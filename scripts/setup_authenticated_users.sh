#!/usr/bin/env bash
# One-shot backend setup for signed-in (non-guest) app users.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "==> Sync seed JSON (Android + Functions)"
python3 scripts/sync_project.py

echo "==> Install functions deps (firebase-admin for seed scripts)"
(cd functions && npm install)

if [[ -z "${GOOGLE_APPLICATION_CREDENTIALS:-}" ]]; then
  echo ""
  echo "NOTE: Set GOOGLE_APPLICATION_CREDENTIALS to your Firebase service account JSON"
  echo "      (Console → Project settings → Service accounts → Generate new private key)"
  echo "      before seeding Firestore, or use: gcloud auth application-default login"
  echo ""
fi

echo "==> Deploy Firestore rules + Cloud Functions (requires: firebase login)"
bash scripts/deploy_functions.sh

echo "==> Seed Firestore catalog (teams, players, stickers)"
node scripts/seed_firestore.mjs

echo ""
echo "==> Optional: grant yourself admin (for in-app Seed Firestore button)"
echo "    node scripts/set_admin_claim.mjs YOUR_EMAIL"
echo ""
echo "==> On device: sign out/in, then sign in with Email or Google."
echo "    Rebuild APK if you changed seed: cd android && ./gradlew installDebug"
