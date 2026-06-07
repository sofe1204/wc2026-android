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
  REWARDED_SLOT_SPIN_COOLDOWN_MINUTES,
  STICKERS_PER_PACK,
} from "./constants";
import {
  applyLoginPackGrant,
  checkSlotWin,
  ensureUserDoc,
  getUserRef,
  grantRewardedAdStickers,
  openPackForUser,
  pickRandomSlotSymbolIds,
  computeDailySlotReset,
  swapDuplicatesForPack,
} from "./helpers";
import {
  fetchLeaderboardTop,
  fetchUserRank,
  reserveUsername,
  syncLeaderboardInTransaction,
  validateProfileInput,
} from "./profileHelpers";
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
  const userRef = await getUserRef(uid);
  const existedBefore = (await userRef.get()).exists;
  await ensureUserDoc(uid, email, name);

  return db.runTransaction(async (tx) => {
    const snap = await tx.get(userRef);
    if (!snap.exists) throw new HttpsError("not-found", "User not found.");
    const data = snap.data()!;
    const hadLoginTimestamp = data.lastLoginPackGrantedAt != null;
    const result = applyLoginPackGrant(tx, userRef, data, existedBefore && !hadLoginTimestamp);
    return {
      success: true,
      unopenedPacks: result.packs,
      message: result.message,
      loginPackGranted: result.granted,
    };
  });
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
export const claimRewardedAdStickers = onCall(async (request) => {
  const uid = requireAuth(request);
  try {
    return await grantRewardedAdStickers(uid);
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : "Failed to claim ad reward.";
    throw new HttpsError("failed-precondition", msg);
  }
});

/** @deprecated Use claimRewardedAdStickers — kept for older app builds. */
export const claimRewardedAdPack = claimRewardedAdStickers;

export const spinSlotMachine = onCall(async (request) => {
  const uid = requireAuth(request);
  const userRef = await getUserRef(uid);

  const flatIds = await pickRandomSlotSymbolIds(9);
  const grid = [
    [flatIds[0], flatIds[1], flatIds[2]],
    [flatIds[3], flatIds[4], flatIds[5]],
    [flatIds[6], flatIds[7], flatIds[8]],
  ];
  const isWin = checkSlotWin(grid);

  try {
    return await db.runTransaction(async (tx) => {
      const snap = await tx.get(userRef);
      if (!snap.exists) throw new HttpsError("not-found", "User not found.");
      const data = computeDailySlotReset(snap.data()!);

      const spins = data.slotSpinsRemaining ?? 0;
      if (spins <= 0) {
        throw new HttpsError("failed-precondition", "No slot spins remaining.");
      }

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

      const spinsRemaining = spins - 1;
      tx.update(userRef, {
        slotSpinsRemaining: spinsRemaining,
        slotSpinsDate: data.slotSpinsDate,
        slotRewardPacksWonToday: packsWonToday,
        slotRewardDate: data.slotRewardDate,
        unopenedPacks,
      });

      const spinRef = db.collection("slot_history").doc();
      tx.set(spinRef, {
        uid,
        symbolIds: flatIds,
        isWin,
        rewardGranted,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      return {
        success: true,
        grid,
        isWin,
        rewardGranted,
        spinsRemaining,
        packsWonToday,
        message,
      };
    });
  } catch (e: unknown) {
    if (e instanceof HttpsError) throw e;
    const msg = e instanceof Error ? e.message : "Slot spin failed.";
    console.error("spinSlotMachine error:", e);
    throw new HttpsError("internal", msg);
  }
});

export const redeemSwapDeck = onCall(async (request) => {
  const uid = requireAuth(request);
  try {
    return await swapDuplicatesForPack(uid);
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : "Swap failed.";
    throw new HttpsError("failed-precondition", msg);
  }
});

export const updateUserProfile = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = request.data as Record<string, unknown>;
  const validated = validateProfileInput({
    username: String(data.username ?? ""),
    firstName: String(data.firstName ?? ""),
    lastName: String(data.lastName ?? ""),
    countryCode: String(data.countryCode ?? ""),
    countryName: String(data.countryName ?? ""),
  });

  const userRef = await getUserRef(uid);
  const emailVerified = request.auth?.token?.email_verified === true;
  const provider = String(
    (request.auth?.token as { firebase?: { sign_in_provider?: string } })?.firebase
      ?.sign_in_provider ?? ""
  );
  if (provider === "password" && !emailVerified) {
    throw new HttpsError(
      "failed-precondition",
      "Verify your email before completing your profile."
    );
  }

  return db.runTransaction(async (tx) => {
    const snap = await tx.get(userRef);
    if (!snap.exists) throw new HttpsError("not-found", "User not found.");
    const existing = snap.data()!;
    await reserveUsername(tx, uid, validated.username, existing.username as string | undefined);

    const displayName = `${validated.firstName} ${validated.lastName}`;
    const updatedData = {
      ...existing,
      username: validated.username,
      firstName: validated.firstName,
      lastName: validated.lastName,
      displayName,
      countryCode: validated.countryCode,
      countryName: validated.countryName,
      profileComplete: true,
      emailVerified,
      leaderboardOptIn: true,
    };

    tx.update(userRef, {
      username: validated.username,
      firstName: validated.firstName,
      lastName: validated.lastName,
      displayName,
      countryCode: validated.countryCode,
      countryName: validated.countryName,
      profileComplete: true,
      emailVerified,
      leaderboardOptIn: true,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    syncLeaderboardInTransaction(tx, uid, updatedData);

    return {
      success: true,
      message: "Profile saved.",
      profileComplete: true,
    };
  });
});

export const getLeaderboard = onCall(async (request) => {
  const uid = requireAuth(request);
  const userRef = await getUserRef(uid);
  const userSnap = await userRef.get();
  if (!userSnap.exists) throw new HttpsError("not-found", "User not found.");
  const userData = userSnap.data()!;
  const myCount = Number(userData.albumUniqueCount || 0);
  const myCountry = String(userData.countryCode || "");
  const myUsername = String(userData.username || "");

  const globalTop = await fetchLeaderboardTop(null, 50);
  const countryTop = myCountry ? await fetchLeaderboardTop(myCountry, 50) : [];

  const profileComplete = userData.profileComplete === true;
  const myGlobalRank = profileComplete ? await fetchUserRank(myCount, null) : null;
  const myCountryRank =
    profileComplete && myCountry ? await fetchUserRank(myCount, myCountry) : null;

  const mapRows = (
    rows: Awaited<ReturnType<typeof fetchLeaderboardTop>>
  ) =>
    rows.map((row, index) => ({
      rank: index + 1,
      username: row.username,
      countryCode: row.countryCode,
      countryName: row.countryName,
      albumUniqueCount: row.albumUniqueCount,
      totalStickerCount: row.totalStickerCount,
      isMe: row.username === myUsername,
    }));

  return {
    success: true,
    global: mapRows(globalTop),
    country: mapRows(countryTop),
    myGlobalRank,
    myCountryRank,
    myUsername,
    myAlbumUniqueCount: myCount,
    myCountryCode: myCountry,
    myCountryName: String(userData.countryName || ""),
  };
});

export const claimRewardedSlotSpins = onCall(async (request) => {
  const uid = requireAuth(request);
  const userRef = await getUserRef(uid);
  const cooldownMs = REWARDED_SLOT_SPIN_COOLDOWN_MINUTES * 60 * 1000;

  return db.runTransaction(async (tx) => {
    const snap = await tx.get(userRef);
    if (!snap.exists) throw new HttpsError("not-found", "User not found.");
    const data = snap.data()!;
    const last = data.lastRewardedSlotSpinAt as admin.firestore.Timestamp | undefined;
    if (last && Date.now() - last.toMillis() < cooldownMs) {
      const waitMin = Math.ceil((cooldownMs - (Date.now() - last.toMillis())) / 60000);
      return {
        success: false,
        message: `Wait ${waitMin} min for next spin ad reward.`,
        spinsRemaining: data.slotSpinsRemaining || 0,
      };
    }
    const spins = (data.slotSpinsRemaining || 0) + REWARDED_SLOT_SPINS;
    tx.update(userRef, {
      slotSpinsRemaining: spins,
      lastRewardedSlotSpinAt: admin.firestore.FieldValue.serverTimestamp(),
    });
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
