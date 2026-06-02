import { CallableRequest, HttpsError } from "firebase-functions/v2/https";

export function requireAuth(request: CallableRequest): string {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required.");
  }
  return request.auth.uid;
}

export function assertAdmin(request: CallableRequest): void {
  requireAuth(request);
  if (request.auth?.token?.admin !== true) {
    throw new HttpsError("permission-denied", "Admin access required.");
  }
}

export function todayUtc(): string {
  return new Date().toISOString().slice(0, 10);
}
