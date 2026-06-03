#!/usr/bin/env node
/**
 * Grant Firebase Auth custom claim { admin: true } so Profile → Seed Firestore works.
 *
 * Usage:
 *   node scripts/set_admin_claim.mjs your@email.com
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { createRequire } from "module";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, "..");
const require = createRequire(import.meta.url);

const email = process.argv[2];
if (!email) {
  console.error("Usage: node scripts/set_admin_claim.mjs <email>");
  process.exit(1);
}

const projectId = JSON.parse(
  fs.readFileSync(path.join(root, "project.config.json"), "utf8")
).firebase.projectId;

async function main() {
  const admin = require(path.join(root, "functions/node_modules/firebase-admin"));
  if (!admin.apps.length) {
    admin.initializeApp({ projectId });
  }
  const user = await admin.auth().getUserByEmail(email);
  await admin.auth().setCustomUserClaims(user.uid, { admin: true });
  console.log(`Admin claim set for ${email} (uid ${user.uid}).`);
  console.log("Sign out and sign in again on the device so the ID token refreshes.");
}

main().catch((e) => {
  console.error(e.message || e);
  process.exit(1);
});
