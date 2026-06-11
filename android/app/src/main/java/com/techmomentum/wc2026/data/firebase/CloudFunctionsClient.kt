package com.techmomentum.wc2026.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.techmomentum.wc2026.data.model.CallableResult
import com.techmomentum.wc2026.data.model.EmailAvailabilityResult
import com.techmomentum.wc2026.data.model.LeaderboardEntry
import com.techmomentum.wc2026.data.model.LeaderboardResult
import com.techmomentum.wc2026.data.model.PackOpenResult
import com.techmomentum.wc2026.data.model.ProfileUpdateRequest
import com.techmomentum.wc2026.data.model.SlotResult
import com.techmomentum.wc2026.data.slot.SlotGridParser
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
    suspend fun checkEmailAvailable(email: String): EmailAvailabilityResult {
        val data = invokePublic("checkEmailAvailable", mapOf("email" to email))
        return EmailAvailabilityResult(
            available = data["available"] as? Boolean ?: false,
            message = data["message"] as? String ?: "",
        )
    }

    suspend fun ensureUserProfile(): CallableResult = call("ensureUserProfile")

    suspend fun openStickerPack(): PackOpenResult {
        val data = invoke("openStickerPack")
        return PackOpenResult(
            stickerIds = (data["stickers"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            unopenedPacks = (data["unopenedPacks"] as? Number)?.toInt() ?: 0,
            message = data["message"] as? String ?: "",
        )
    }

    suspend fun claimRewardedAdStickers(): CallableResult = call("claimRewardedAdStickers")

    suspend fun updateUserProfile(request: ProfileUpdateRequest): CallableResult {
        val data = invoke(
            "updateUserProfile",
            mapOf(
                "username" to request.username,
                "firstName" to request.firstName,
                "lastName" to request.lastName,
                "countryCode" to request.countryCode,
                "countryName" to request.countryName,
            ),
        )
        return CallableResult(
            success = data["success"] as? Boolean ?: false,
            message = data["message"] as? String ?: "",
        )
    }

    suspend fun getLeaderboard(): LeaderboardResult {
        val data = invoke("getLeaderboard")
        return LeaderboardResult(
            global = parseLeaderboardRows(data["global"]),
            country = parseLeaderboardRows(data["country"]),
            myGlobalRank = (data["myGlobalRank"] as? Number)?.toInt(),
            myCountryRank = (data["myCountryRank"] as? Number)?.toInt(),
            myUsername = data["myUsername"] as? String ?: "",
            myAlbumUniqueCount = (data["myAlbumUniqueCount"] as? Number)?.toInt() ?: 0,
            myCountryCode = data["myCountryCode"] as? String ?: "",
            myCountryName = data["myCountryName"] as? String ?: "",
        )
    }

    suspend fun spinSlotMachine(): SlotResult {
        val data = invoke("spinSlotMachine")
        val grid = SlotGridParser.parseSpinIds(data)
        if (!SlotGridParser.isValid(grid)) {
            throw IllegalStateException("Could not read spin result. Please try again.")
        }
        return SlotResult(
            grid = grid,
            isWin = parseBoolean(data["isWin"]),
            rewardGranted = parseBoolean(data["rewardGranted"]),
            spinsRemaining = (data["spinsRemaining"] as? Number)?.toInt() ?: 0,
            packsWonToday = (data["packsWonToday"] as? Number)?.toInt() ?: 0,
            message = data["message"] as? String ?: "",
        )
    }

    suspend fun claimRewardedSlotSpins(): CallableResult = call("claimRewardedSlotSpins")

    suspend fun registerFcmToken(token: String): CallableResult {
        val data = invoke("registerFcmToken", mapOf("token" to token))
        return CallableResult(success = data["success"] as? Boolean ?: true, message = "")
    }

    suspend fun clearFcmToken(): CallableResult = call("clearFcmToken")

    suspend fun redeemSwapDeck(): CallableResult = call("redeemSwapDeck")

    suspend fun seedTeams(): CallableResult = call("seedTeams")
    suspend fun seedPlayers(): CallableResult = call("seedPlayers")
    suspend fun seedStickers(): CallableResult = call("seedStickers")

    private fun parseBoolean(value: Any?): Boolean = when (value) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        is String -> value.equals("true", ignoreCase = true)
        else -> false
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseLeaderboardRows(raw: Any?): List<LeaderboardEntry> {
        val rows = raw as? List<Map<String, Any?>> ?: return emptyList()
        return rows.map { row ->
            LeaderboardEntry(
                rank = (row["rank"] as? Number)?.toInt() ?: 0,
                username = row["username"] as? String ?: "",
                countryCode = row["countryCode"] as? String ?: "",
                countryName = row["countryName"] as? String ?: "",
                albumUniqueCount = (row["albumUniqueCount"] as? Number)?.toInt() ?: 0,
                totalStickerCount = (row["totalStickerCount"] as? Number)?.toInt() ?: 0,
                isMe = row["isMe"] as? Boolean ?: false,
            )
        }
    }

    private suspend fun call(name: String): CallableResult {
        val data = invoke(name)
        return CallableResult(
            success = data["success"] as? Boolean ?: true,
            message = data["message"] as? String ?: "",
            unopenedPacks = (data["unopenedPacks"] as? Number)?.toInt(),
            stickerIds = (data["stickerIds"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
        )
    }

    private suspend fun invoke(name: String, payload: Map<String, Any?> = emptyMap()): Map<String, Any?> {
        requireSignedIn()
        refreshAuthToken()
        return invokeCallable(name, payload)
    }

    private suspend fun invokePublic(name: String, payload: Map<String, Any?> = emptyMap()): Map<String, Any?> =
        invokeCallable(name, payload)

    private suspend fun invokeCallable(name: String, payload: Map<String, Any?>): Map<String, Any?> {
        connectionRepository.refreshConfigState()
        return try {
            @Suppress("UNCHECKED_CAST")
            functions.getHttpsCallable(name).call(payload).await().getData() as? Map<String, Any?> ?: emptyMap()
        } catch (e: FirebaseFunctionsException) {
            throw mapFunctionsError(e)
        }
    }

    private fun requireSignedIn() {
        if (auth.currentUser == null) throw FirebaseNotSignedInException()
    }

    private suspend fun refreshAuthToken() {
        auth.currentUser?.getIdToken(true)?.await()
    }

    private fun mapFunctionsError(e: FirebaseFunctionsException): Exception {
        val detail = userFacingDetail(e)
        return when (e.code) {
            FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                FunctionsUnauthenticatedException(
                    if (auth.currentUser != null) {
                        "Cloud rewards could not verify your session. Retrying profile setup."
                    } else {
                        "Not signed in. Sign in again to continue."
                    },
                    e,
                )
            FirebaseFunctionsException.Code.PERMISSION_DENIED ->
                IllegalStateException("Permission denied: $detail")
            FirebaseFunctionsException.Code.NOT_FOUND ->
                FunctionsNotDeployedException(
                    "Cloud Functions not deployed (${com.techmomentum.wc2026.config.ProjectConfig.FUNCTIONS_REGION}). " +
                        "Run: ./scripts/deploy_functions.sh",
                    e,
                )
            FirebaseFunctionsException.Code.INVALID_ARGUMENT ->
                IllegalStateException(detail)
            FirebaseFunctionsException.Code.FAILED_PRECONDITION ->
                IllegalStateException(detail)
            FirebaseFunctionsException.Code.ALREADY_EXISTS ->
                IllegalStateException(detail)
            FirebaseFunctionsException.Code.UNAVAILABLE ->
                IllegalStateException("Firebase unavailable. Check network or emulators: $detail")
            FirebaseFunctionsException.Code.INTERNAL ->
                IllegalStateException(detail)
            FirebaseFunctionsException.Code.DEADLINE_EXCEEDED ->
                IllegalStateException(detail)
            FirebaseFunctionsException.Code.UNKNOWN ->
                IllegalStateException(detail)
            else -> IllegalStateException(detail)
        }
    }

    private fun userFacingDetail(e: FirebaseFunctionsException): String {
        val serverMessage = e.message?.takeIf { it.isNotBlank() }
        if (serverMessage != null && !isRawErrorCode(serverMessage)) {
            return serverMessage
        }
        return when (e.code) {
            FirebaseFunctionsException.Code.INTERNAL ->
                "Something went wrong on the server. Please try again."
            FirebaseFunctionsException.Code.DEADLINE_EXCEEDED ->
                "Request timed out. Check your connection and try again."
            FirebaseFunctionsException.Code.UNAVAILABLE ->
                "Firebase is temporarily unavailable. Try again in a moment."
            FirebaseFunctionsException.Code.UNKNOWN ->
                "Could not reach the server. Check your connection and try again."
            else -> serverMessage ?: "Something went wrong. Please try again."
        }
    }

    private fun isRawErrorCode(message: String): Boolean =
        message.equals("INTERNAL", ignoreCase = true) ||
            message.equals("UNKNOWN", ignoreCase = true) ||
            message.equals("UNAVAILABLE", ignoreCase = true) ||
            message.equals("DEADLINE_EXCEEDED", ignoreCase = true)
}
