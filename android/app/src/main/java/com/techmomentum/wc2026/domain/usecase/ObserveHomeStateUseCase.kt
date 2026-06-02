package com.techmomentum.wc2026.domain.usecase

import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.data.repository.UserRepository
import com.techmomentum.wc2026.utils.GameConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class HomeState(
    val profile: UserProfile? = null,
    val albumPercent: Float = 0f,
    val dailyClaimedToday: Boolean = false,
    val adPackClaimedToday: Boolean = false,
)

class ObserveHomeStateUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    operator fun invoke(todayUtc: String): Flow<HomeState> =
        userRepository.observeUserProfile().map { profile ->
            val unique = profile?.albumUniqueCount ?: 0
            HomeState(
                profile = profile,
                albumPercent = unique.toFloat() / GameConstants.TOTAL_STICKERS * 100f,
                dailyClaimedToday = profile?.lastDailyPackClaimDate == todayUtc,
                adPackClaimedToday = profile?.rewardedAdPackClaimDate == todayUtc,
            )
        }
}
