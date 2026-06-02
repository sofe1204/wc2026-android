import * as admin from "firebase-admin";
import {
  DAILY_FREE_SLOT_SPINS,
  RARITY_FALLBACK_ORDER,
  RARITY_WEIGHTS,
  STICKERS_PER_PACK,
} from "./constants";
import { todayUtc } from "./admin";

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
    createdAt: now,
    unopenedPacks: 2,
    albumUniqueCount: 0,
    totalStickerCount: 0,
    lastDailyPackClaimDate: "",
    rewardedAdPackClaimDate: "",
    slotSpinsRemaining: DAILY_FREE_SLOT_SPINS,
    slotSpinsDate: todayUtc(),
    slotRewardDate: todayUtc(),
    slotRewardPacksWonToday: 0,
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

export async function pickRandomPlayerIds(count: number): Promise<string[]> {
  const snap = await db().collection("players").where("isActive", "==", true).limit(200).get();
  if (snap.empty) return Array(count).fill("unknown");
  const ids = snap.docs.map((d) => d.id);
  return Array.from({ length: count }, () => ids[Math.floor(Math.random() * ids.length)]);
}

export function checkSlotWin(grid: string[][]): boolean {
  const lines = [
    [grid[0][0], grid[0][1], grid[0][2]],
    [grid[1][0], grid[1][1], grid[1][2]],
    [grid[2][0], grid[2][1], grid[2][2]],
    [grid[0][0], grid[1][1], grid[2][2]],
    [grid[0][2], grid[1][1], grid[2][0]],
  ];
  return lines.some(
    (line) => line[0] === line[1] && line[1] === line[2] && line[0] !== ""
  );
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
    tx.update(userRef, {
      unopenedPacks: packs - 1,
      albumUniqueCount: (data.albumUniqueCount || 0) + newUnique,
      totalStickerCount: (data.totalStickerCount || 0) + totalAdded,
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
