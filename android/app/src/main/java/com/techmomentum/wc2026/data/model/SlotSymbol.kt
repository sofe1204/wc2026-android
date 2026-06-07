package com.techmomentum.wc2026.data.model

data class SlotSymbol(
    val symbolId: String,
    val playerId: String = "",
    val label: String = "",
    val type: String = "player",
    val imageUrl: String = "",
    val isActive: Boolean = true,
) {
    val isTrophy: Boolean get() = type == "trophy" || symbolId == "trophy"
}
