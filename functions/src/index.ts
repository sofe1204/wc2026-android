import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { setGlobalOptions } from "firebase-functions/v2";
import {
  assertAdmin,
  requireAuth,
  todayUtc,
} from "./admin";
import {
  DAILY_FREE_PACKS,
  DAILY_SLOT_PACK_REWARD_CAP,
  REWARDED_SLOT_SPINS,
  STICKERS_PER_PACK,
} from "./constants";
import {
  checkSlotWin,
  ensureUserDoc,
  getUserRef,
  openPackForUser,
  pickRandomSlotSymbolIds,
  resetDailySlotIfNeeded,
} from "./helpers";
import {
  loadPlayersSeed,
  loadStickersSeed,
  loadTeamsSeed,
} from "./seed/seedData";
import { FUNCTIONS_REGION } from "./projectConfig";

admin.initializeApp();
setGlobalOptions({ region: FUNCTIONS_REGION });

const db = admin.firestore();

export const ensureUserProfile = onCall(async (request) => {
  const uid = requireAuth(request);
  const email = request.auth?.token?.email || "";
  const name = request.auth?.token?.name || email;
  await ensureUserDoc(uid, email, name);
  const snap = await (await getUserRef(uid)).get();
  return {
    success: true,
    unopenedPacks: snap.data()?.unopenedPacks ?? 0,
    message: "Profile ready.",
  };
});

export const openStickerPack = onCall(async (request) => {
  const uid = requireAuth(request);
  try {
    const result = await openPackForUser(uid);
    return {
      success: true,
      stickers: result.stickers,
      unopenedPacks: result.unopenedPacks,
      message: `Opened ${STICKERS_PER_PACK} stickers!`,
    };
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : "Failed to open pack.";
    throw new HttpsError("failed-precondition", msg);
  }
});

export const claimDailyPacks = onCall(async (request) => {
  const uid = requireAuth(request);
  const today = todayUtc();
  const userRef = await getUserRef(uid);

  return db.runTransaction(async (tx) => {
    const snap = await tx.get(userRef);
    if (!snap.exists) throw new HttpsError("not-found", "User not found.");
    const data = snap.data()!;
    if (data.lastDailyPackClaimDate === today) {
      return {
        success: false,
        message: "Daily packs already claimed today.",
        unopenedPacks: data.unopenedPacks,
      };
    }
    tx.update(userRef, {
      unopenedPacks: (data.unopenedPacks || 0) + DAILY_FREE_PACKS,
      lastDailyPackClaimDate: today,
    });
    return {
      success: true,
      message: `Added ${DAILY_FREE_PACKS} daily packs.`,
      unopenedPacks: (data.unopenedPacks || 0) + DAILY_FREE_PACKS,
    };
  });
});

// TODO: Implement AdMob Server-Side Verification before production ad rewards.
export const claimRewardedAdPack = onCall(async (request) => {
  const uid = requireAuth(request);
  const today = todayUtc();
  const userRef = await getUserRef(uid);

  return db.runTransaction(async (tx) => {
    const snap = await tx.get(userRef);
    if (!snap.exists) throw new HttpsError("not-found", "User not found.");
    const data = snap.data()!;
    if (data.rewardedAdPackClaimDate === today) {
      return {
        success: false,
        message: "Rewarded ad pack already claimed today.",
        unopenedPacks: data.unopenedPacks,
      };
    }
    tx.update(userRef, {
      unopenedPacks: (data.unopenedPacks || 0) + 1,
      rewardedAdPackClaimDate: today,
    });
    return {
      success: true,
      message: "Added 1 pack from rewarded ad.",
      unopenedPacks: (data.unopenedPacks || 0) + 1,
    };
  });
});

export const spinSlotMachine = onCall(async (request) => {
  const uid = requireAuth(request);
  const userRef = await getUserRef(uid);

  return db.runTransaction(async (tx) => {
    const snap = await tx.get(userRef);
    if (!snap.exists) throw new HttpsError("not-found", "User not found.");
    let data = snap.data()!;
    data = await resetDailySlotIfNeeded(tx, userRef, data);

    const spins = data.slotSpinsRemaining ?? 0;
    if (spins <= 0) {
      throw new HttpsError("failed-precondition", "No slot spins remaining.");
    }

    const flatIds = await pickRandomSlotSymbolIds(9);
    const grid = [
      [flatIds[0], flatIds[1], flatIds[2]],
      [flatIds[3], flatIds[4], flatIds[5]],
      [flatIds[6], flatIds[7], flatIds[8]],
    ];
    const isWin = checkSlotWin(grid);
    let rewardGranted = false;
    let packsWonToday = data.slotRewardPacksWonToday ?? 0;
    let unopenedPacks = data.unopenedPacks ?? 0;
    let message = isWin ? "" : "No match — try again!";

    if (isWin) {
      if (packsWonToday < DAILY_SLOT_PACK_REWARD_CAP) {
        rewardGranted = true;
        packsWonToday += 1;
        unopenedPacks += 1;
        message = "You won a sticker pack!";
      } else {
        message = "Daily slot reward limit reached.";
      }
    }

    tx.update(userRef, {
      slotSpinsRemaining: spins - 1,
      slotRewardPacksWonToday: packsWonToday,
      unopenedPacks,
    });

    const spinRef = db.collection("slot_history").doc();
    tx.set(spinRef, {
      uid,
      grid,
      isWin,
      rewardGranted,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return {
      success: true,
      grid,
      isWin,
      rewardGranted,
      spinsRemaining: spins - 1,
      packsWonToday,
      message,
    };
  });
});

export const claimRewardedSlotSpins = onCall(async (request) => {
  const uid = requireAuth(request);
  const userRef = await getUserRef(uid);

  return db.runTransaction(async (tx) => {
    const snap = await tx.get(userRef);
    if (!snap.exists) throw new HttpsError("not-found", "User not found.");
    const data = snap.data()!;
    const spins = (data.slotSpinsRemaining || 0) + REWARDED_SLOT_SPINS;
    tx.update(userRef, { slotSpinsRemaining: spins });
    return {
      success: true,
      message: `Added ${REWARDED_SLOT_SPINS} slot spins.`,
      spinsRemaining: spins,
    };
  });
});

async function batchSeed(
  collection: string,
  items: Record<string, unknown>[],
  idField: string
) {
  const now = admin.firestore.FieldValue.serverTimestamp();
  const batches: FirebaseFirestore.WriteBatch[] = [];
  let batch = db.batch();
  let count = 0;
  for (const item of items) {
    const id = String(item[idField]);
    const ref = db.collection(collection).doc(id);
    batch.set(
      ref,
      {
        ...item,
        createdAt: now,
        updatedAt: now,
      },
      { merge: true }
    );
    count++;
    if (count % 400 === 0) {
      batches.push(batch);
      batch = db.batch();
    }
  }
  batches.push(batch);
  for (const b of batches) {
    await b.commit();
  }
  return items.length;
}

export const seedTeams = onCall(async (request) => {
  assertAdmin(request);
  const teams = loadTeamsSeed();
  const n = await batchSeed("teams", teams, "teamId");
  return { success: true, message: `Seeded ${n} teams.` };
});

export const seedPlayers = onCall(async (request) => {
  assertAdmin(request);
  const players = loadPlayersSeed();
  const n = await batchSeed("players", players, "playerId");
  return { success: true, message: `Seeded ${n} players.` };
});

export const seedStickers = onCall(async (request) => {
  assertAdmin(request);
  const stickers = loadStickersSeed();
  const n = await batchSeed("stickers", stickers, "stickerId");
  return { success: true, message: `Seeded ${n} stickers.` };
});
