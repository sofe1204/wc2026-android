package com.techmomentum.wc2026.data.model

data class SwapDeckItem(
    val stickerId: String,
    val label: String,
    val imageUrl: String,
    val teamId: String,
    val duplicateCount: Int,
    val rarity: Rarity = Rarity.COMMON,
)
