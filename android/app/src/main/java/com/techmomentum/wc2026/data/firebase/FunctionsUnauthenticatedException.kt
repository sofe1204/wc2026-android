package com.techmomentum.wc2026.data.firebase

/**
 * Cloud Function rejected the call as unauthenticated while Firebase Auth still has a local session.
 * Often a stale/missing ID token — profile bootstrap can still write directly to Firestore.
 */
class FunctionsUnauthenticatedException(
    message: String = "Cloud Function did not receive a valid auth token.",
    cause: Throwable? = null,
) : Exception(message, cause)
