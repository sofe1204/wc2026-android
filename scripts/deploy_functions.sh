#!/usr/bin/env bash
# Deploy Cloud Functions + Firestore + Storage rules to wc-2026-3110f
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "==> Building functions..."
cd functions
npm install
npm run build
cd "$ROOT"

echo "==> Deploying to Firebase (project wc-2026-3110f)..."
if command -v firebase >/dev/null 2>&1; then
  FIREBASE_BIN=firebase
else
  echo "    Using npx firebase-tools..."
  FIREBASE_BIN="npx --yes firebase-tools"
fi

export npm_config_cache="${npm_config_cache:-$ROOT/.npm-cache}"
mkdir -p "$npm_config_cache"

$FIREBASE_BIN login --reauth 2>/dev/null || $FIREBASE_BIN login
$FIREBASE_BIN use wc-2026-3110f
echo "==> Deploying Firestore rules first (needed for login/register)..."
$FIREBASE_BIN deploy --only firestore:rules,firestore:indexes
echo "==> Deploying Storage rules (profile avatars, assets)..."
$FIREBASE_BIN deploy --only storage
echo "==> Deploying Cloud Functions..."
$FIREBASE_BIN deploy --only functions

echo ""
echo "==> Deploy complete. Restart the app and sign in again."
echo "    Functions region: us-central1"
