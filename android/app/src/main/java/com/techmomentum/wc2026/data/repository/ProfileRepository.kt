package com.techmomentum.wc2026.data.repository

import com.techmomentum.wc2026.data.firebase.CloudFunctionsClient
import com.techmomentum.wc2026.data.model.CallableResult
import com.techmomentum.wc2026.data.model.LeaderboardResult
import com.techmomentum.wc2026.data.model.ProfileUpdateRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val cloudFunctions: CloudFunctionsClient,
) {
    suspend fun updateUserProfile(request: ProfileUpdateRequest): CallableResult =
        cloudFunctions.updateUserProfile(request)

    suspend fun getLeaderboard(): LeaderboardResult = cloudFunctions.getLeaderboard()
}
