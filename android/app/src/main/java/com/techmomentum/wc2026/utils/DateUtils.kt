package com.techmomentum.wc2026.utils

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateUtils {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC)

    fun todayUtc(): String = formatter.format(Instant.now())

    /** Next UTC midnight in epoch milliseconds. */
    fun nextUtcMidnightEpochMs(nowMs: Long = System.currentTimeMillis()): Long {
        val now = Instant.ofEpochMilli(nowMs).atZone(ZoneOffset.UTC)
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC)
        return nextMidnight.toInstant().toEpochMilli()
    }

    /** Next occurrence of [hourUtc] (0–23) in UTC; if still ahead today, uses today. */
    fun nextUtcHourEpochMs(hourUtc: Int, nowMs: Long = System.currentTimeMillis()): Long {
        require(hourUtc in 0..23)
        val now = Instant.ofEpochMilli(nowMs).atZone(ZoneOffset.UTC)
        var target = now.toLocalDate().atTime(hourUtc, 0).atZone(ZoneOffset.UTC)
        if (!target.toInstant().toEpochMilli().let { it > nowMs }) {
            target = target.plusDays(1)
        }
        return target.toInstant().toEpochMilli()
    }
}
