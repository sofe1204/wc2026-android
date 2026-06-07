package com.techmomentum.wc2026.domain.usecase

import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.data.repository.UserRepository
import com.techmomentum.wc2026.utils.GameConstants
import com.techmomentum.wc2026.utils.RewardEligibility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class HomeState(
    val profile: UserProfile? = null,
    val albumPercent: Float = 0f,
    val loginPackAvailable: Boolean = false,
    val adStickerAvailable: Boolean = true,
    val adStickerCooldownMinutes: Int = 0,
)

class ObserveHomeStateUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    operator fun invoke(): Flow<HomeState> =
        userRepository.observeUserProfile().map { profile ->
            val unique = profile?.albumUniqueCount ?: 0
            val lastLogin = profile?.lastLoginPackGrantedAtEpochMs ?: 0L
            val lastAd = profile?.lastRewardedAdStickerAtEpochMs ?: 0L
            HomeState(
                profile = profile,
                albumPercent = unique.toFloat() / GameConstants.TOTAL_STICKERS * 100f,
                loginPackAvailable = RewardEligibility.isLoginPackEligible(lastLogin),
                adStickerAvailable = RewardEligibility.isAdStickerAvailable(lastAd),
                adStickerCooldownMinutes = RewardEligibility.adStickerCooldownMinutesRemaining(lastAd),
            )
        }
}
