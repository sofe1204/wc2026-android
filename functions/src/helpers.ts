import * as admin from "firebase-admin";
import {
  DAILY_FREE_SLOT_SPINS,
  LOGIN_REWARD_INTERVAL_HOURS,
  LOGIN_REWARD_PACKS,
  RARITY_FALLBACK_ORDER,
  RARITY_WEIGHTS,
  REWARDED_AD_COOLDOWN_MINUTES,
  REWARDED_AD_STICKERS,
  STICKERS_PER_PACK,
  SWAP_DUPLICATES_FOR_PACK,
} from "./constants";
import { todayUtc } from "./admin";
import { syncLeaderboardInTransaction } from "./profileHelpers";

const db = () => admin.firestore();

export async function getUserRef(uid: string) {
  return db().collection("users").doc(uid);
}

export async function ensureUserDoc(
  uid: string,
  email: string,
  displayName: string
): Promise<FirebaseFirestore.DocumentSnapshot> {
  const ref = await getUserRef(uid);
  const snap = await ref.get();
  if (snap.exists) return snap;
  const now = admin.firestore.FieldValue.serverTimestamp();
  await ref.set({
    uid,
    email,
    displayName: displayName || email,
    username: "",
    firstName: "",
    lastName: "",
    countryCode: "",
    countryName: "",
    profileComplete: false,
    emailVerified: false,
    leaderboardOptIn: true,
    createdAt: now,
    unopenedPacks: 2,
    albumUniqueCount: 0,
    totalStickerCount: 0,
    lastDailyPackClaimDate: "",
    rewardedAdPackClaimDate: "",
    lastLoginPackGrantedAt: now,
    lastRewardedAdStickerAt: null,
    slotSpinsRemaining: DAILY_FREE_SLOT_SPINS,
    slotSpinsDate: todayUtc(),
    slotRewardDate: todayUtc(),
    slotRewardPacksWonToday: 0,
    lastRewardedSlotSpinAt: null,
  });
  return (await ref.get());
}

export function rollRarity(): string {
  const roll = Math.random() * 100;
  let acc = 0;
  for (const [rarity, weight] of Object.entries(RARITY_WEIGHTS)) {
    acc += weight;
    if (roll < acc) return rarity;
  }
  return "common";
}

export async function pickStickerByRarity(
  rarity: string
): Promise<string | null> {
  const stickersRef = db().collection("stickers");
  for (const r of [rarity, ...RARITY_FALLBACK_ORDER.filter((x) => x !== rarity)]) {
    const snap = await stickersRef
      .where("isActive", "==", true)
      .where("rarity", "==", r)
      .get();
    if (!snap.empty) {
      const docs = snap.docs;
      return docs[Math.floor(Math.random() * docs.length)].id;
    }
  }
  const all = await stickersRef.where("isActive", "==", true).limit(500).get();
  if (all.empty) return null;
  return all.docs[Math.floor(Math.random() * all.docs.length)].id;
}

export async function grantStickers(
  tx: FirebaseFirestore.Transaction,
  uid: string,
  stickerIds: string[]
): Promise<{ newUnique: number; totalAdded: number }> {
  const userStickersRef = db().collection("user_stickers").doc(uid).collection("items");
  let newUnique = 0;
  let totalAdded = 0;
  for (const stickerId of stickerIds) {
    const itemRef = userStickersRef.doc(stickerId);
    const itemSnap = await tx.get(itemRef);
    const stickerSnap = await tx.get(db().collection("stickers").doc(stickerId));
    const stickerData = stickerSnap.data() || {};
    const now = admin.firestore.FieldValue.serverTimestamp();
    if (itemSnap.exists) {
      tx.update(itemRef, {
        count: (itemSnap.data()?.count || 0) + 1,
        lastCollectedAt: now,
      });
    } else {
      newUnique += 1;
      tx.set(itemRef, {
        stickerId,
        playerId: stickerData.playerId || "",
        teamId: stickerData.teamId || "",
        count: 1,
        firstCollectedAt: now,
        lastCollectedAt: now,
      });
    }
    totalAdded += 1;
  }
  return { newUnique, totalAdded };
}

export async function resetDailySlotIfNeeded(
  tx: FirebaseFirestore.Transaction,
  userRef: FirebaseFirestore.DocumentReference,
  data: FirebaseFirestore.DocumentData
): Promise<FirebaseFirestore.DocumentData> {
  const today = todayUtc();
  let updated = { ...data };
  if (data.slotSpinsDate !== today) {
    updated = {
      ...updated,
      slotSpinsRemaining: DAILY_FREE_SLOT_SPINS,
      slotSpinsDate: today,
    };
  }
  if (data.slotRewardDate !== today) {
    updated = {
      ...updated,
      slotRewardPacksWonToday: 0,
      slotRewardDate: today,
    };
  }
  if (
    updated.slotSpinsRemaining !== data.slotSpinsRemaining ||
    updated.slotRewardPacksWonToday !== data.slotRewardPacksWonToday
  ) {
    tx.update(userRef, {
      slotSpinsRemaining: updated.slotSpinsRemaining,
      slotSpinsDate: updated.slotSpinsDate,
      slotRewardPacksWonToday: updated.slotRewardPacksWonToday,
      slotRewardDate: updated.slotRewardDate,
    });
  }
  return updated;
}

export const TROPHY_SYMBOL_ID = "trophy";

export async function pickRandomSlotSymbolIds(count: number): Promise<string[]> {
  const snap = await db().collection("slot_symbols").where("isActive", "==", true).get();
  if (!snap.empty) {
    const ids = snap.docs.map((d) => d.id);
    return Array.from({ length: count }, () => ids[Math.floor(Math.random() * ids.length)]);
  }
  return pickRandomPlayerIds(count);
}

export async function pickRandomPlayerIds(count: number): Promise<string[]> {
  const snap = await db().collection("players").where("isActive", "==", true).limit(200).get();
  if (snap.empty) return Array(count).fill("unknown");
  const ids = snap.docs.map((d) => d.id);
  return Array.from({ length: count }, () => ids[Math.floor(Math.random() * ids.length)]);
}

function slotLineWins(line: string[]): boolean {
  if (line.some((s) => !s || s === "unknown")) return false;
  const nonWild = line.filter((s) => s !== TROPHY_SYMBOL_ID);
  if (nonWild.length === 0) return true;
  const target = nonWild[0];
  return (
    nonWild.every((s) => s === target) &&
    line.every((s) => s === target || s === TROPHY_SYMBOL_ID)
  );
}

/** Win only on a diagonal — 3 matching symbols (trophy = wildcard). */
export function checkSlotWin(grid: string[][]): boolean {
  const diagonals = [
    [grid[0][0], grid[1][1], grid[2][2]],
    [grid[0][2], grid[1][1], grid[2][0]],
  ];
  return diagonals.some(slotLineWins);
}

const LOGIN_REWARD_INTERVAL_MS = LOGIN_REWARD_INTERVAL_HOURS * 60 * 60 * 1000;
const REWARDED_AD_COOLDOWN_MS = REWARDED_AD_COOLDOWN_MINUTES * 60 * 1000;

export function isLoginPackEligible(
  last: admin.firestore.Timestamp | undefined | null,
  existedBeforeField: boolean
): boolean {
  if (!last) return existedBeforeField;
  return Date.now() - last.toMillis() >= LOGIN_REWARD_INTERVAL_MS;
}

export function applyLoginPackGrant(
  tx: FirebaseFirestore.Transaction,
  userRef: FirebaseFirestore.DocumentReference,
  data: FirebaseFirestore.DocumentData,
  existedBeforeField: boolean
): { granted: boolean; packs: number; message: string } {
  const packs = data.unopenedPacks || 0;
  const last = data.lastLoginPackGrantedAt as admin.firestore.Timestamp | undefined;
  if (!isLoginPackEligible(last, existedBeforeField)) {
    return { granted: false, packs, message: "Profile ready." };
  }
  const newPacks = packs + LOGIN_REWARD_PACKS;
  tx.update(userRef, {
    unopenedPacks: newPacks,
    lastLoginPackGrantedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
  return {
    granted: true,
    packs: newPacks,
    message: `Welcome back! +${LOGIN_REWARD_PACKS} pack (${STICKERS_PER_PACK} stickers each).`,
  };
}

export async function grantRewardedAdStickers(uid: string): Promise<{
  success: boolean;
  message: string;
  stickerIds: string[];
  unopenedPacks: number;
}> {
  const userRef = await getUserRef(uid);
  const stickerIds: string[] = [];
  for (let i = 0; i < REWARDED_AD_STICKERS; i++) {
    const id = await pickStickerByRarity(rollRarity());
    if (id) stickerIds.push(id);
  }
  if (stickerIds.length === 0) {
    return {
      success: false,
      message: "No stickers available.",
      stickerIds: [],
      unopenedPacks: 0,
    };
  }

  return db().runTransaction(async (tx) => {
    const snap = await tx.get(userRef);
    if (!snap.exists) {
      throw new Error("User not found.");
    }
    const data = snap.data()!;
    const last = data.lastRewardedAdStickerAt as admin.firestore.Timestamp | undefined;
    if (last && Date.now() - last.toMillis() < REWARDED_AD_COOLDOWN_MS) {
      const waitMin = Math.ceil(
        (REWARDED_AD_COOLDOWN_MS - (Date.now() - last.toMillis())) / 60000
      );
      return {
        success: false,
        message: `Wait ${waitMin} min for next ad reward.`,
        stickerIds: [],
        unopenedPacks: data.unopenedPacks || 0,
      };
    }
    const { newUnique, totalAdded } = await grantStickers(tx, uid, stickerIds);
    const albumUniqueCount = (data.albumUniqueCount || 0) + newUnique;
    const totalStickerCount = (data.totalStickerCount || 0) + totalAdded;
    tx.update(userRef, {
      lastRewardedAdStickerAt: admin.firestore.FieldValue.serverTimestamp(),
      albumUniqueCount,
      totalStickerCount,
    });
    syncLeaderboardInTransaction(tx, uid, {
      ...data,
      albumUniqueCount,
      totalStickerCount,
    });
    const adRef = db().collection("pack_history").doc();
    tx.set(adRef, {
      uid,
      source: "rewarded_ad",
      stickers: stickerIds,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    return {
      success: true,
      message: `You earned ${stickerIds.length} stickers!`,
      stickerIds,
      unopenedPacks: data.unopenedPacks || 0,
    };
  });
}

export function duplicateCount(stickerCount: number): number {
  return Math.max(0, stickerCount - 1);
}

export async function countTotalSwapDuplicates(uid: string): Promise<number> {
  const snap = await db()
    .collection("user_stickers")
    .doc(uid)
    .collection("items")
    .get();
  return snap.docs.reduce((sum, doc) => {
    const count = doc.data().count || 0;
    return sum + duplicateCount(count);
  }, 0);
}

export async function swapDuplicatesForPack(uid: string): Promise<{
  success: boolean;
  message: string;
  unopenedPacks: number;
  duplicatesConsumed: number;
}> {
  const userRef = await getUserRef(uid);
  const itemsRef = db().collection("user_stickers").doc(uid).collection("items");
  const itemsSnap = await itemsRef.get();

  const totalDupes = itemsSnap.docs.reduce((sum, doc) => {
    return sum + duplicateCount(doc.data().count || 0);
  }, 0);

  if (totalDupes < SWAP_DUPLICATES_FOR_PACK) {
    const userSnap = await userRef.get();
    return {
      success: false,
      message: `Need ${SWAP_DUPLICATES_FOR_PACK} duplicate stickers in your swap deck (${totalDupes}/${SWAP_DUPLICATES_FOR_PACK}).`,
      unopenedPacks: userSnap.data()?.unopenedPacks || 0,
      duplicatesConsumed: 0,
    };
  }

  const sorted = itemsSnap.docs
    .map((doc) => ({ ref: doc.ref, count: doc.data().count || 0 }))
    .filter((item) => duplicateCount(item.count) > 0)
    .sort((a, b) => duplicateCount(b.count) - duplicateCount(a.count));

  return db().runTransaction(async (tx) => {
    const userSnap = await tx.get(userRef);
    if (!userSnap.exists) throw new Error("User profile not found.");
    const userData = userSnap.data()!;

    let remaining = SWAP_DUPLICATES_FOR_PACK;
    for (const item of sorted) {
      if (remaining <= 0) break;
      const itemSnap = await tx.get(item.ref);
      if (!itemSnap.exists) continue;
      const count = itemSnap.data()?.count || 0;
      const dupes = duplicateCount(count);
      if (dupes <= 0) continue;
      const take = Math.min(dupes, remaining);
      const newCount = count - take;
      if (newCount <= 0) {
        tx.delete(item.ref);
      } else {
        tx.update(item.ref, { count: newCount });
      }
      remaining -= take;
    }

    if (remaining > 0) {
      throw new Error("Could not consume enough duplicates.");
    }

    const packs = (userData.unopenedPacks || 0) + 1;
    tx.update(userRef, {
      unopenedPacks: packs,
      totalStickerCount: Math.max(
        0,
        (userData.totalStickerCount || 0) - SWAP_DUPLICATES_FOR_PACK
      ),
    });

    const historyRef = db().collection("pack_history").doc();
    tx.set(historyRef, {
      uid,
      source: "swap_deck",
      stickers: [],
      duplicatesConsumed: SWAP_DUPLICATES_FOR_PACK,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return {
      success: true,
      message: `Swapped ${SWAP_DUPLICATES_FOR_PACK} duplicates for 1 sticker pack!`,
      unopenedPacks: packs,
      duplicatesConsumed: SWAP_DUPLICATES_FOR_PACK,
    };
  });
}

export async function openPackForUser(uid: string): Promise<{
  stickers: string[];
  unopenedPacks: number;
}> {
  const userRef = await getUserRef(uid);
  return db().runTransaction(async (tx) => {
    const userSnap = await tx.get(userRef);
    if (!userSnap.exists) {
      throw new Error("User profile not found.");
    }
    const data = userSnap.data()!;
    const packs = data.unopenedPacks || 0;
    if (packs <= 0) {
      throw new Error("No unopened packs available.");
    }
    const stickerIds: string[] = [];
    for (let i = 0; i < STICKERS_PER_PACK; i++) {
      const rarity = rollRarity();
      const id = await pickStickerByRarity(rarity);
      if (id) stickerIds.push(id);
    }
    const { newUnique, totalAdded } = await grantStickers(tx, uid, stickerIds);
    const albumUniqueCount = (data.albumUniqueCount || 0) + newUnique;
    const totalStickerCount = (data.totalStickerCount || 0) + totalAdded;
    tx.update(userRef, {
      unopenedPacks: packs - 1,
      albumUniqueCount,
      totalStickerCount,
    });
    syncLeaderboardInTransaction(tx, uid, {
      ...data,
      albumUniqueCount,
      totalStickerCount,
    });
    const packRef = db().collection("pack_history").doc();
    tx.set(packRef, {
      uid,
      source: "pack_open",
      stickers: stickerIds,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    return { stickers: stickerIds, unopenedPacks: packs - 1 };
  });
}
