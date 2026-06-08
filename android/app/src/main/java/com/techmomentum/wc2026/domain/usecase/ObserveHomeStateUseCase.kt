package com.techmomentum.wc2026.domain.usecase

import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.data.repository.CatalogRepository
import com.techmomentum.wc2026.data.repository.UserRepository
import com.techmomentum.wc2026.utils.RewardEligibility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class HomeState(
    val profile: UserProfile? = null,
    val albumPercent: Float = 0f,
    val totalCollectibleStickers: Int = 0,
    val loginPackAvailable: Boolean = false,
    val adStickerAvailable: Boolean = true,
    val adStickerCooldownMinutes: Int = 0,
)

class ObserveHomeStateUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val catalogRepository: CatalogRepository,
) {
    operator fun invoke(): Flow<HomeState> = flow {
        val totalCollectible = catalogRepository.getCollectibleStickerCount().coerceAtLeast(1)
        userRepository.observeUserProfile().collect { profile ->
            val unique = profile?.albumUniqueCount ?: 0
            val lastLogin = profile?.lastLoginPackGrantedAtEpochMs ?: 0L
            val lastAd = profile?.lastRewardedAdStickerAtEpochMs ?: 0L
            emit(
                HomeState(
                    profile = profile,
                    albumPercent = unique.toFloat() / totalCollectible * 100f,
                    totalCollectibleStickers = totalCollectible,
                    loginPackAvailable = RewardEligibility.isLoginPackEligible(lastLogin),
                    adStickerAvailable = RewardEligibility.isAdStickerAvailable(lastAd),
                    adStickerCooldownMinutes = RewardEligibility.adStickerCooldownMinutesRemaining(lastAd),
                ),
            )
        }
    }
}
