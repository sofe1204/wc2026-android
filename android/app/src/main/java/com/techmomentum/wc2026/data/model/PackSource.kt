package com.techmomentum.wc2026.data.model

enum class PackSource(val value: String) {
    SIGNUP("signup"),
    DAILY("daily"),
    REWARDED_AD("rewarded_ad"),
    SLOT("slot"),
    ADMIN("admin");

    companion object {
        fun from(value: String?): PackSource =
            entries.firstOrNull { it.value == value } ?: DAILY
    }
}
