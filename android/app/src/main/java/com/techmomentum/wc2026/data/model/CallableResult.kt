package com.techmomentum.wc2026.data.model

data class CallableResult(
    val success: Boolean = false,
    val message: String = "",
    val unopenedPacks: Int? = null,
    val stickerIds: List<String> = emptyList(),
)
