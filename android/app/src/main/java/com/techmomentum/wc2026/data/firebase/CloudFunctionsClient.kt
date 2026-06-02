package com.techmomentum.wc2026.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.techmomentum.wc2026.data.model.CallableResult
import com.techmomentum.wc2026.data.model.PackOpenResult
import com.techmomentum.wc2026.data.model.SlotResult
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

class FirebaseNotSignedInException : IllegalStateException("Sign in to use cloud rewards.")

@Singleton
class CloudFunctionsClient @Inject constructor(
    private val functions: FirebaseFunctions,
    private val auth: FirebaseAuth,
    private val connectionRepository: FirebaseConnectionRepository,
) {
    suspend fun ensureUserProfile(): CallableResult = call("ensureUserProfile")

    suspend fun openStickerPack(): PackOpenResult {
        val data = invoke("openStickerPack")
        return PackOpenResult(
            stickerIds = (data["stickers"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            unopenedPacks = (data["unopenedPacks"] as? Number)?.toInt() ?: 0,
            message = data["message"] as? String ?: "",
        )
    }

    suspend fun claimDailyPacks(): CallableResult = call("claimDailyPacks")

    suspend fun claimRewardedAdPack(): CallableResult = call("claimRewardedAdPack")

    suspend fun spinSlotMachine(): SlotResult {
        val data = invoke("spinSlotMachine")
        @Suppress("UNCHECKED_CAST")
        val gridRaw = data["grid"] as? List<List<String>> ?: emptyList()
        return SlotResult(
            grid = gridRaw,
            isWin = data["isWin"] as? Boolean ?: false,
            rewardGranted = data["rewardGranted"] as? Boolean ?: false,
            spinsRemaining = (data["spinsRemaining"] as? Number)?.toInt() ?: 0,
            packsWonToday = (data["packsWonToday"] as? Number)?.toInt() ?: 0,
            message = data["message"] as? String ?: "",
        )
    }

    suspend fun claimRewardedSlotSpins(): CallableResult = call("claimRewardedSlotSpins")

    suspend fun seedTeams(): CallableResult = call("seedTeams")
    suspend fun seedPlayers(): CallableResult = call("seedPlayers")
    suspend fun seedStickers(): CallableResult = call("seedStickers")

    private suspend fun call(name: String): CallableResult {
        val data = invoke(name)
        return CallableResult(
            success = data["success"] as? Boolean ?: true,
            message = data["message"] as? String ?: "",
            unopenedPacks = (data["unopenedPacks"] as? Number)?.toInt(),
        )
    }

    private suspend fun invoke(name: String): Map<String, Any?> {
        requireSignedIn()
        connectionRepository.refreshConfigState()
        return try {
            @Suppress("UNCHECKED_CAST")
            functions.getHttpsCallable(name).call().await().getData() as? Map<String, Any?> ?: emptyMap()
        } catch (e: FirebaseFunctionsException) {
            throw mapFunctionsError(e)
        }
    }

    private fun requireSignedIn() {
        if (auth.currentUser == null) throw FirebaseNotSignedInException()
    }

    private fun mapFunctionsError(e: FirebaseFunctionsException): Exception {
        val detail = e.message?.takeIf { it.isNotBlank() } ?: e.code.name
        return when (e.code) {
            FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                IllegalStateException("Not signed in. Sign in again to continue.")
            FirebaseFunctionsException.Code.PERMISSION_DENIED ->
                IllegalStateException("Permission denied: $detail")
            FirebaseFunctionsException.Code.NOT_FOUND ->
                FunctionsNotDeployedException(
                    "Cloud Functions not deployed (${com.techmomentum.wc2026.config.ProjectConfig.FUNCTIONS_REGION}). " +
                        "Run: ./scripts/deploy_functions.sh",
                    e,
                )
            FirebaseFunctionsException.Code.FAILED_PRECONDITION ->
                IllegalStateException(detail)
            FirebaseFunctionsException.Code.UNAVAILABLE ->
                IllegalStateException("Firebase unavailable. Check network or emulators: $detail")
            else -> IllegalStateException(detail)
        }
    }
}
