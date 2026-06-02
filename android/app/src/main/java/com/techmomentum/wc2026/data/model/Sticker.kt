package com.techmomentum.wc2026.data.model

data class Sticker(
    val stickerId: String = "",
    val stickerNumber: Int = 0,
    val playerId: String = "",
    val teamId: String = "",
    val countryName: String = "",
    val group: String = "",
    val rarity: Rarity = Rarity.COMMON,
    val imageUrl: String = "",
    val isActive: Boolean = true,
)
