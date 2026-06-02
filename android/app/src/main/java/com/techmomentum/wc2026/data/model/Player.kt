package com.techmomentum.wc2026.data.model

data class Player(
    val playerId: String = "",
    val teamId: String = "",
    val countryName: String = "",
    val group: String = "",
    val shirtNumber: Int = 0,
    val playerName: String = "",
    val position: String = "",
    val rarity: Rarity = Rarity.COMMON,
    val animeStickerPrompt: String = "",
    val imageUrl: String = "",
    val isActive: Boolean = true,
)
