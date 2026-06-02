package com.techmomentum.wc2026.data.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val unopenedPacks: Int = 0,
    val albumUniqueCount: Int = 0,
    val totalStickerCount: Int = 0,
    val lastDailyPackClaimDate: String = "",
    val rewardedAdPackClaimDate: String = "",
    val slotSpinsRemaining: Int = 0,
    val slotSpinsDate: String = "",
    val slotRewardDate: String = "",
    val slotRewardPacksWonToday: Int = 0,
)
