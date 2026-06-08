#!/usr/bin/env node
/**
 * Recompute albumUniqueCount / totalStickerCount from user_stickers and sync leaderboard.
 *
 * Usage:
 *   GOOGLE_APPLICATION_CREDENTIALS=./service-account.json npm run reconcile:leaderboard
 *   npm run reconcile:leaderboard -- <uid>   # single user
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { createRequire } from "module";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, "..");
const require = createRequire(import.meta.url);

const projectId = JSON.parse(
  fs.readFileSync(path.join(root, "project.config.json"), "utf8")
).firebase.projectId;

const singleUid = process.argv[2]?.trim() || "";

async function countAlbumStats(db, uid) {
  const snap = await db
    .collection("user_stickers")
    .doc(uid)
    .collection("items")
    .get();
  let totalStickerCount = 0;
  for (const doc of snap.docs) {
    totalStickerCount += Number(doc.data().count || 0);
  }
  return { albumUniqueCount: snap.size, totalStickerCount };
}

async function recomputeUser(db, admin, uid) {
  const stats = await countAlbumStats(db, uid);
  const userRef = db.collection("users").doc(uid);
  const userSnap = await userRef.get();
  if (!userSnap.exists) {
    console.log(`  skip ${uid}: no users doc`);
    return null;
  }
  const data = userSnap.data();
  const prevUnique = Number(data.albumUniqueCount || 0);
  const prevTotal = Number(data.totalStickerCount || 0);

  await db.runTransaction(async (tx) => {
    const snap = await tx.get(userRef);
    if (!snap.exists) return;
    const userData = snap.data();
    tx.update(userRef, {
      albumUniqueCount: stats.albumUniqueCount,
      totalStickerCount: stats.totalStickerCount,
    });
    if (
      userData.profileComplete &&
      userData.username &&
      userData.countryCode &&
      userData.leaderboardOptIn !== false
    ) {
      tx.set(
        db.collection("leaderboard").doc(uid),
        {
          uid,
          username: userData.username,
          countryCode: userData.countryCode,
          countryName: userData.countryName || "",
          albumUniqueCount: stats.albumUniqueCount,
          totalStickerCount: stats.totalStickerCount,
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        },
        { merge: true }
      );
    }
  });

  const label = data.username ? `@${data.username}` : uid;
  if (prevUnique !== stats.albumUniqueCount || prevTotal !== stats.totalStickerCount) {
    console.log(
      `  ${label}: unique ${prevUnique} → ${stats.albumUniqueCount}, total ${prevTotal} → ${stats.totalStickerCount}`
    );
  } else {
    console.log(`  ${label}: ok (${stats.albumUniqueCount} unique)`);
  }
  return stats;
}

async function main() {
  const admin = require(path.join(root, "functions/node_modules/firebase-admin"));
  if (!admin.apps.length) {
    admin.initializeApp({ projectId });
  }
  const db = admin.firestore();

  let uids = [];
  if (singleUid) {
    uids = [singleUid];
  } else {
    const [usersSnap, leaderboardSnap] = await Promise.all([
      db.collection("users").where("profileComplete", "==", true).get(),
      db.collection("leaderboard").get(),
    ]);
    uids = [
      ...new Set([
        ...usersSnap.docs.map((d) => d.id),
        ...leaderboardSnap.docs.map((d) => d.id),
      ]),
    ];
  }

  console.log(`Reconciling ${uids.length} user(s) in ${projectId}...`);
  for (const uid of uids) {
    await recomputeUser(db, admin, uid);
  }
  console.log("Done.");
}

main().catch((e) => {
  console.error(e.message || e);
  process.exit(1);
});
