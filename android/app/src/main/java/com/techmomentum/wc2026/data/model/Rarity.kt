package com.techmomentum.wc2026.data.model

enum class Rarity(val value: String) {
    COMMON("common"),
    RARE("rare"),
    EPIC("epic"),
    LEGENDARY("legendary");

    companion object {
        fun from(value: String?): Rarity =
            entries.firstOrNull { it.value == value?.lowercase() } ?: COMMON
    }
}
