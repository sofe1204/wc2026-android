package com.techmomentum.wc2026.data.model

data class LeaderboardEntry(
    val rank: Int = 0,
    val username: String = "",
    val countryCode: String = "",
    val countryName: String = "",
    val albumUniqueCount: Int = 0,
    val totalStickerCount: Int = 0,
    val isMe: Boolean = false,
)

data class LeaderboardResult(
    val global: List<LeaderboardEntry> = emptyList(),
    val country: List<LeaderboardEntry> = emptyList(),
    val myGlobalRank: Int? = null,
    val myCountryRank: Int? = null,
    val myUsername: String = "",
    val myAlbumUniqueCount: Int = 0,
    val myCountryCode: String = "",
    val myCountryName: String = "",
)

data class CountryOption(
    val code: String,
    val name: String,
)

data class ProfileUpdateRequest(
    val username: String,
    val firstName: String,
    val lastName: String,
    val countryCode: String,
    val countryName: String,
)
