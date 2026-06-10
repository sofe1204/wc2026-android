package com.techmomentum.wc2026.utils

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object RewardEligibility {
    private val utcDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC)

    /** Login pack is available once per UTC calendar day (resets at midnight UTC). */
    fun isLoginPackEligible(lastGrantedAtEpochMs: Long, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (lastGrantedAtEpochMs <= 0L) return false
        val lastDate = utcDateFormatter.format(Instant.ofEpochMilli(lastGrantedAtEpochMs))
        val today = utcDateFormatter.format(Instant.ofEpochMilli(nowMs))
        return lastDate != today
    }

    fun adStickerCooldownRemainingMs(
        lastGrantedAtEpochMs: Long,
        nowMs: Long = System.currentTimeMillis(),
    ): Long {
        if (lastGrantedAtEpochMs <= 0L) return 0L
        val elapsed = nowMs - lastGrantedAtEpochMs
        val cooldown = GameConstants.REWARDED_AD_COOLDOWN_MS
        return (cooldown - elapsed).coerceAtLeast(0L)
    }

    fun isAdStickerAvailable(lastGrantedAtEpochMs: Long, nowMs: Long = System.currentTimeMillis()): Boolean =
        adStickerCooldownRemainingMs(lastGrantedAtEpochMs, nowMs) == 0L

    fun adStickerCooldownMinutesRemaining(lastGrantedAtEpochMs: Long, nowMs: Long = System.currentTimeMillis()): Int {
        val remainingMs = adStickerCooldownRemainingMs(lastGrantedAtEpochMs, nowMs)
        return if (remainingMs <= 0L) 0 else ((remainingMs + 59_999L) / 60_000L).toInt()
    }

    fun slotSpinAdCooldownRemainingMs(
        lastGrantedAtEpochMs: Long,
        nowMs: Long = System.currentTimeMillis(),
    ): Long {
        if (lastGrantedAtEpochMs <= 0L) return 0L
        val elapsed = nowMs - lastGrantedAtEpochMs
        return (GameConstants.REWARDED_SLOT_SPIN_COOLDOWN_MS - elapsed).coerceAtLeast(0L)
    }

    fun isSlotSpinAdAvailable(lastGrantedAtEpochMs: Long, nowMs: Long = System.currentTimeMillis()): Boolean =
        slotSpinAdCooldownRemainingMs(lastGrantedAtEpochMs, nowMs) == 0L

    fun isAtSlotPackCap(packsWonToday: Int): Boolean =
        packsWonToday >= GameConstants.DAILY_SLOT_PACK_REWARD_CAP

    fun canSpinSlots(spinsRemaining: Int, packsWonToday: Int): Boolean =
        spinsRemaining > 0 && !isAtSlotPackCap(packsWonToday)

    fun isSlotSpinAdRewardAvailable(
        lastGrantedAtEpochMs: Long,
        packsWonToday: Int,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean = !isAtSlotPackCap(packsWonToday) && isSlotSpinAdAvailable(lastGrantedAtEpochMs, nowMs)

    fun isSlotSpinsRowAvailable(
        spinsRemaining: Int,
        lastGrantedAtEpochMs: Long,
        packsWonToday: Int,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean = !isAtSlotPackCap(packsWonToday) &&
        (spinsRemaining > 0 || isSlotSpinAdRewardAvailable(lastGrantedAtEpochMs, packsWonToday, nowMs))

    fun slotSpinAdCooldownMinutesRemaining(
        lastGrantedAtEpochMs: Long,
        nowMs: Long = System.currentTimeMillis(),
    ): Int {
        val remainingMs = slotSpinAdCooldownRemainingMs(lastGrantedAtEpochMs, nowMs)
        return if (remainingMs <= 0L) 0 else ((remainingMs + 59_999L) / 60_000L).toInt()
    }
}
