package com.techmomentum.wc2026.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import com.techmomentum.wc2026.BuildConfig
import com.techmomentum.wc2026.data.model.CallableResult
import com.techmomentum.wc2026.debug.DebugAgentLog
import com.techmomentum.wc2026.utils.DateUtils
import com.techmomentum.wc2026.utils.GameConstants
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creates `users/{uid}` in Firestore when Cloud Functions are not deployed yet.
 * Matches server [ensureUserDoc] defaults (signup packs + slot spins).
 */
@Singleton
class FirestoreUserBootstrap @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    suspend fun ensureUserProfile(createNewAccount: Boolean = false): CallableResult {
        val user = auth.currentUser
            ?: return CallableResult(success = false, message = "Not signed in.")
        return try {
            prepareFirestoreNetwork()
            ensureUserProfileInternal(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName,
                createNewAccount = createNewAccount,
            )
        } catch (e: FirebaseFirestoreException) {
            // #region agent log
            DebugAgentLog.log(
                location = "FirestoreUserBootstrap.kt:ensureUserProfile",
                message = "FirestoreException",
                hypothesisId = "H2",
                data = mapOf(
                    "code" to e.code.name,
                    "msgSnippet" to (e.message?.take(120) ?: ""),
                ),
            )
            // #endregion
            CallableResult(success = false, message = mapFirestoreError(e))
        } catch (e: Exception) {
            // #region agent log
            DebugAgentLog.log(
                location = "FirestoreUserBootstrap.kt:ensureUserProfile",
                message = "GenericException",
                hypothesisId = "H3",
                data = mapOf(
                    "type" to e.javaClass.simpleName,
                    "msgSnippet" to ((e.message ?: "") + (e.cause?.message ?: "")).take(120),
                ),
            )
            // #endregion
            CallableResult(success = false, message = mapGenericError(e))
        }
    }

    private suspend fun prepareFirestoreNetwork() {
        runCatching { firestore.enableNetwork().await() }
    }

    private suspend fun ensureUserProfileInternal(
        uid: String,
        email: String?,
        displayName: String?,
        createNewAccount: Boolean,
    ): CallableResult {
        val ref = firestore.collection("users").document(uid)
        if (!createNewAccount) {
            val existing = getFromServerWithRetry(ref)
            if (existing.exists()) {
                val packs = existing.getLong("unopenedPacks")?.toInt() ?: 0
                return CallableResult(
                    success = true,
                    message = "Profile ready.",
                    unopenedPacks = packs,
                )
            }
        }
        val today = DateUtils.todayUtc()
        val data = mapOf(
            "uid" to uid,
            "email" to (email ?: ""),
            "displayName" to (displayName?.takeIf { it.isNotBlank() } ?: email ?: ""),
            "unopenedPacks" to GameConstants.SIGNUP_FREE_PACKS,
            "albumUniqueCount" to 0,
            "totalStickerCount" to 0,
            "lastDailyPackClaimDate" to "",
            "rewardedAdPackClaimDate" to "",
            "slotSpinsRemaining" to GameConstants.DAILY_FREE_SLOT_SPINS,
            "slotSpinsDate" to today,
            "slotRewardDate" to today,
            "slotRewardPacksWonToday" to 0,
        )
        ref.set(data).await()
        return CallableResult(
            success = true,
            message = "Welcome! Your collector profile is ready.",
            unopenedPacks = GameConstants.SIGNUP_FREE_PACKS,
        )
    }

    private suspend fun getFromServerWithRetry(ref: DocumentReference) =
        try {
            ref.get(Source.SERVER).await()
        } catch (first: Exception) {
            if (!isOfflineError(first)) throw first
            prepareFirestoreNetwork()
            ref.get(Source.SERVER).await()
        }

    private fun isOfflineError(e: Exception): Boolean {
        if (isMissingFirestoreDatabaseError(e)) return false
        val msg = (e.message ?: "") + (e.cause?.message ?: "")
        return msg.contains("offline", ignoreCase = true) ||
            (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.UNAVAILABLE)
    }

    private fun isMissingFirestoreDatabaseError(e: Exception): Boolean {
        val msg = (e.message ?: "") + (e.cause?.message ?: "")
        return msg.contains("database (default) does not exist", ignoreCase = true) ||
            (e is FirebaseFirestoreException &&
                e.code == FirebaseFirestoreException.Code.NOT_FOUND &&
                msg.contains("does not exist", ignoreCase = true))
    }

    private fun mapFirestoreError(e: FirebaseFirestoreException): String = when {
        isMissingFirestoreDatabaseError(e) -> missingDatabaseHelpMessage()
        isOfflineError(e) -> offlineHelpMessage()
        e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Firestore rules blocked profile creation. Run ./scripts/deploy_functions.sh (deploys rules)."
        else -> e.message ?: "Could not save profile (${e.code})"
    }

    private fun mapGenericError(e: Exception): String = when {
        isMissingFirestoreDatabaseError(e) -> missingDatabaseHelpMessage()
        isOfflineError(e) -> offlineHelpMessage()
        else -> e.message ?: "Could not save profile"
    }

    private fun missingDatabaseHelpMessage(): String =
        "Firestore is not set up for project wc-2026-3110f. In Firebase Console → Build → Firestore Database → Create database (production mode). Then deploy rules: ./scripts/deploy_functions.sh"

    private fun offlineHelpMessage(): String = buildString {
        append("Can't reach Firestore (cloud database). ")
        append("Check internet on your phone/emulator. ")
        if (BuildConfig.USE_FIREBASE_EMULATORS) {
            append("Emulator mode is ON — run: firebase emulators:start, or set firebase.emulators=false in android/local.properties. ")
        }
        append("Auth may work while Firestore is unreachable.")
    }
}
