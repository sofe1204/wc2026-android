package com.techmomentum.wc2026.data.model

data class PackOpenResult(
    val stickerIds: List<String> = emptyList(),
    val unopenedPacks: Int = 0,
    val message: String = "",
)
