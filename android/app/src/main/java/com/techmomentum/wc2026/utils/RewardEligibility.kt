package com.techmomentum.wc2026.utils

object RewardEligibility {
    fun isLoginPackEligible(lastGrantedAtEpochMs: Long, nowMs: Long = System.currentTimeMillis()): Boolean =
        lastGrantedAtEpochMs > 0L &&
            nowMs - lastGrantedAtEpochMs >= GameConstants.LOGIN_REWARD_INTERVAL_MS

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

    fun slotSpinAdCooldownMinutesRemaining(
        lastGrantedAtEpochMs: Long,
        nowMs: Long = System.currentTimeMillis(),
    ): Int {
        val remainingMs = slotSpinAdCooldownRemainingMs(lastGrantedAtEpochMs, nowMs)
        return if (remainingMs <= 0L) 0 else ((remainingMs + 59_999L) / 60_000L).toInt()
    }
}
