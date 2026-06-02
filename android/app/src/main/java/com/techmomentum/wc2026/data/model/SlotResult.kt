package com.techmomentum.wc2026.data.model

data class SlotResult(
    val grid: List<List<String>> = emptyList(),
    val isWin: Boolean = false,
    val rewardGranted: Boolean = false,
    val spinsRemaining: Int = 0,
    val packsWonToday: Int = 0,
    val message: String = "",
)
