package com.techmomentum.wc2026.data.model

data class Team(
    val teamId: String = "",
    val countryName: String = "",
    val group: String = "",
    val teamCode: String = "",
    val flagEmoji: String = "",
    val customEmblemUrl: String = "",
    val primaryColor: String = "",
    val secondaryColor: String = "",
    val isActive: Boolean = true,
)
