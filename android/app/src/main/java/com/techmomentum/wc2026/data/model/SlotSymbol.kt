package com.techmomentum.wc2026.data.model

data class SlotSymbol(
    val symbolId: String,
    val playerId: String = "",
    val label: String = "",
    val type: String = "player",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    /** Firestore document id when it differs from [symbolId]. */
    val documentId: String = "",
) {
    val isTrophy: Boolean get() = type == "trophy" || symbolId == "trophy"

    companion object {
        fun placeholder(spinId: String): SlotSymbol = SlotSymbol(
            symbolId = spinId.ifBlank { "unknown" },
            label = spinId.takeLast(8).ifBlank { "?" },
        )
    }
}
