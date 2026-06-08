package com.techmomentum.wc2026.data.model

data class SlotGridPosition(
    val row: Int,
    val col: Int,
)

data class SlotCell(
    val row: Int,
    val col: Int,
    val spinId: String,
    val symbol: SlotSymbol,
)
