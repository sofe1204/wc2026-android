import * as admin from "firebase-admin";
import { readFileSync } from "fs";
import { join } from "path";
import { HttpsError } from "firebase-functions/v2/https";

const db = () => admin.firestore();

type CountryRow = { code: string; name: string };

const COUNTRY_BY_CODE: Map<string, string> = (() => {
  const raw = readFileSync(join(__dirname, "../seed/countries.json"), "utf8");
  const rows = JSON.parse(raw) as CountryRow[];
  return new Map(rows.map((r) => [r.code.toUpperCase(), r.name]));
})();

const USERNAME_RE = /^[a-zA-Z0-9_]{3,20}$/;

export function normalizeUsername(raw: string): string {
  return raw.trim();
}

export function usernameKey(username: string): string {
  return username.trim().toLowerCase();
}

export function validateUsername(username: string): string | null {
  const value = normalizeUsername(username);
  if (!USERNAME_RE.test(value)) {
    return "Username must be 3–20 characters (letters, numbers, underscore).";
  }
  return null;
}

export function validateCountry(countryCode: string, countryName: string): string | null {
  const code = countryCode.trim().toUpperCase();
  if (!/^[A-Z]{2}$/.test(code)) {
    return "Select a valid country.";
  }
  const official = COUNTRY_BY_CODE.get(code);
  if (!official) {
    return "Unknown country code.";
  }
  const name = countryName.trim();
  if (!name) {
    return "Country name is required.";
  }
  return null;
}

export function validateProfileInput(input: {
  username: string;
  firstName: string;
  lastName: string;
  countryCode: string;
  countryName: string;
}): { username: string; firstName: string; lastName: string; countryCode: string; countryName: string } {
  const usernameErr = validateUsername(input.username);
  if (usernameErr) throw new HttpsError("invalid-argument", usernameErr);

  const firstName = input.firstName.trim();
  const lastName = input.lastName.trim();
  if (firstName.length < 1 || firstName.length > 50) {
    throw new HttpsError("invalid-argument", "First name is required (max 50 characters).");
  }
  if (lastName.length < 1 || lastName.length > 50) {
    throw new HttpsError("invalid-argument", "Surname is required (max 50 characters).");
  }

  const countryCode = input.countryCode.trim().toUpperCase();
  const countryErr = validateCountry(countryCode, input.countryName);
  if (countryErr) throw new HttpsError("invalid-argument", countryErr);
  const countryName = COUNTRY_BY_CODE.get(countryCode) || input.countryName.trim();

  return {
    username: normalizeUsername(input.username),
    firstName,
    lastName,
    countryCode,
    countryName,
  };
}

export function syncLeaderboardInTransaction(
  tx: FirebaseFirestore.Transaction,
  uid: string,
  data: FirebaseFirestore.DocumentData
): void {
  if (!data.profileComplete || !data.username || !data.countryCode) return;
  if (data.leaderboardOptIn === false) return;

  const ref = db().collection("leaderboard").doc(uid);
  tx.set(
    ref,
    {
      uid,
      username: data.username,
      countryCode: data.countryCode,
      countryName: data.countryName || "",
      albumUniqueCount: data.albumUniqueCount || 0,
      totalStickerCount: data.totalStickerCount || 0,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
  );
}

export async function reserveUsername(
  tx: FirebaseFirestore.Transaction,
  uid: string,
  newUsername: string,
  previousUsername: string | undefined
): Promise<void> {
  const newKey = usernameKey(newUsername);
  const newRef = db().collection("usernames").doc(newKey);
  const existing = await tx.get(newRef);
  if (existing.exists && existing.data()?.uid !== uid) {
    throw new HttpsError("already-exists", "Username is already taken.");
  }

  const prevKey = previousUsername ? usernameKey(previousUsername) : "";
  if (prevKey && prevKey !== newKey) {
    const prevRef = db().collection("usernames").doc(prevKey);
    const prevSnap = await tx.get(prevRef);
    if (prevSnap.exists && prevSnap.data()?.uid === uid) {
      tx.delete(prevRef);
    }
  }

  tx.set(newRef, {
    uid,
    username: newUsername,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
}

export type LeaderboardRow = {
  rank: number;
  username: string;
  countryCode: string;
  countryName: string;
  albumUniqueCount: number;
  totalStickerCount: number;
  isMe: boolean;
};

export async function fetchLeaderboardTop(
  countryCode: string | null,
  limit: number
): Promise<Omit<LeaderboardRow, "rank" | "isMe">[]> {
  let query: FirebaseFirestore.Query = db().collection("leaderboard");
  if (countryCode) {
    query = query.where("countryCode", "==", countryCode);
  }
  const snap = await query.orderBy("albumUniqueCount", "desc").limit(limit).get();
  return snap.docs.map((doc) => {
    const d = doc.data();
    return {
      username: String(d.username || ""),
      countryCode: String(d.countryCode || ""),
      countryName: String(d.countryName || ""),
      albumUniqueCount: Number(d.albumUniqueCount || 0),
      totalStickerCount: Number(d.totalStickerCount || 0),
    };
  });
}

export async function fetchUserRank(
  albumUniqueCount: number,
  countryCode: string | null
): Promise<number | null> {
  let query: FirebaseFirestore.Query = db()
    .collection("leaderboard")
    .where("albumUniqueCount", ">", albumUniqueCount);
  if (countryCode) {
    query = query.where("countryCode", "==", countryCode);
  }
  const snap = await query.count().get();
  return snap.data().count + 1;
}
