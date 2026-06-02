package com.techmomentum.wc2026.utils

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object DateUtils {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC)

    fun todayUtc(): String = formatter.format(Instant.now())
}
