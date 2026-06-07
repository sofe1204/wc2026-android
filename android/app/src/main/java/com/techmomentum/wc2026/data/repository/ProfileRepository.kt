package com.techmomentum.wc2026.data.repository

import com.techmomentum.wc2026.data.firebase.CloudFunctionsClient
import com.techmomentum.wc2026.data.model.CallableResult
import com.techmomentum.wc2026.data.model.LeaderboardResult
import com.techmomentum.wc2026.data.model.ProfileUpdateRequest
import com.techmomentum.wc2026.data.session.AppSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val cloudFunctions: CloudFunctionsClient,
    private val appSession: AppSession,
) {
    suspend fun updateUserProfile(request: ProfileUpdateRequest): CallableResult {
        if (appSession.isActive()) {
            return CallableResult(success = false, message = "Sign in to save your profile.")
        }
        return cloudFunctions.updateUserProfile(request)
    }

    suspend fun getLeaderboard(): LeaderboardResult {
        if (appSession.isActive()) return LeaderboardResult()
        return cloudFunctions.getLeaderboard()
    }
}
