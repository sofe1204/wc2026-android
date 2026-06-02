package com.techmomentum.wc2026.data.repository

import com.google.firebase.functions.FirebaseFunctionsException
import com.techmomentum.wc2026.data.demo.DemoRewardsEngine
import com.techmomentum.wc2026.data.firebase.CloudFunctionsClient
import com.techmomentum.wc2026.data.firebase.FirestoreUserBootstrap
import com.techmomentum.wc2026.data.firebase.FunctionsNotDeployedException
import com.techmomentum.wc2026.data.model.CallableResult
import com.techmomentum.wc2026.data.model.PackOpenResult
import com.techmomentum.wc2026.data.model.SlotResult
import com.techmomentum.wc2026.data.session.AppSession
import com.techmomentum.wc2026.debug.DebugAgentLog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardsRepository @Inject constructor(
    private val cloudFunctions: CloudFunctionsClient,
    private val firestoreUserBootstrap: FirestoreUserBootstrap,
    private val appSession: AppSession,
    private val demoRewardsEngine: DemoRewardsEngine,
) {
    /**
     * Ensures Firestore user profile exists after Firebase Auth sign-in/sign-up.
     * Uses Cloud Function when deployed; otherwise creates profile directly in Firestore.
     */
    suspend fun ensureUserProfile(createNewAccount: Boolean = false): CallableResult {
        if (useDemo()) return demoRewardsEngine.ensureUserProfile()
        return try {
            val fromFunctions = cloudFunctions.ensureUserProfile()
            // #region agent log
            DebugAgentLog.log(
                location = "RewardsRepository.kt:ensureUserProfile",
                message = "cloudFunctions ok",
                hypothesisId = "H5",
                data = mapOf("success" to fromFunctions.success),
            )
            // #endregion
            fromFunctions
        } catch (e: Exception) {
            val useBootstrap = shouldUseFirestoreBootstrap(e)
            // #region agent log
            DebugAgentLog.log(
                location = "RewardsRepository.kt:ensureUserProfile",
                message = "cloudFunctions failed",
                hypothesisId = "H5",
                data = mapOf(
                    "useFirestoreBootstrap" to useBootstrap,
                    "type" to e.javaClass.simpleName,
                    "msgSnippet" to (e.message?.take(80) ?: ""),
                ),
            )
            // #endregion
            if (useBootstrap) {
                val bootstrap = firestoreUserBootstrap.ensureUserProfile(createNewAccount = createNewAccount)
                // #region agent log
                DebugAgentLog.log(
                    location = "RewardsRepository.kt:ensureUserProfile",
                    message = "firestore bootstrap result",
                    hypothesisId = "H2",
                    data = mapOf("success" to bootstrap.success, "messageSnippet" to bootstrap.message.take(80)),
                )
                // #endregion
                bootstrap
            } else {
                CallableResult(
                    success = false,
                    message = e.message ?: "Profile setup failed",
                )
            }
        }
    }

    suspend fun openStickerPack(): PackOpenResult =
        if (useDemo()) demoRewardsEngine.openStickerPack() else cloudFunctions.openStickerPack()

    suspend fun claimDailyPacks(): CallableResult =
        if (useDemo()) demoRewardsEngine.claimDailyPacks() else cloudFunctions.claimDailyPacks()

    suspend fun claimRewardedAdPack(): CallableResult =
        if (useDemo()) demoRewardsEngine.claimRewardedAdPack() else cloudFunctions.claimRewardedAdPack()

    suspend fun spinSlotMachine(): SlotResult =
        if (useDemo()) demoRewardsEngine.spinSlotMachine() else cloudFunctions.spinSlotMachine()

    suspend fun claimRewardedSlotSpins(): CallableResult =
        if (useDemo()) demoRewardsEngine.claimRewardedSlotSpins() else cloudFunctions.claimRewardedSlotSpins()

    suspend fun seedTeams(): CallableResult = cloudFunctions.seedTeams()

    suspend fun seedPlayers(): CallableResult = cloudFunctions.seedPlayers()

    suspend fun seedStickers(): CallableResult = cloudFunctions.seedStickers()

    private fun useDemo(): Boolean = appSession.isActive()

    private fun shouldUseFirestoreBootstrap(e: Exception): Boolean {
        if (e is FunctionsNotDeployedException) return true
        if (e.cause is FunctionsNotDeployedException) return true
        val functions = e as? FirebaseFunctionsException
        if (functions?.code == FirebaseFunctionsException.Code.NOT_FOUND) return true
        val msg = (e.message ?: "") + (e.cause?.message ?: "")
        return msg.contains("NOT_FOUND", ignoreCase = true) ||
            msg.contains("Cloud Functions not deployed", ignoreCase = true) ||
            msg.contains("NOT_FOUND", ignoreCase = true)
    }
}
