package com.techmomentum.wc2026.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.techmomentum.wc2026.data.local.AppPreferences
import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.ui.navigation.Routes
import com.techmomentum.wc2026.utils.DateUtils
import com.techmomentum.wc2026.utils.GameConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: AppPreferences,
) {
    fun reschedule(profile: UserProfile?) {
        cancelAll()
        if (profile == null || !preferences.notificationsEnabled) return
        if (!profile.profileComplete) return

        val now = System.currentTimeMillis()
        scheduleAdStickerReminder(profile, now)
        scheduleSlotAdReminder(profile, now)
        scheduleLoginPackReminder(profile, now)
        scheduleSlotDailyReset(now)
        scheduleUnopenedPackReminder(profile, now)
    }

    fun cancelAll() {
        ReminderType.entries.forEach { type ->
            WorkManager.getInstance(context).cancelUniqueWork(type.workName)
        }
    }

    private fun scheduleAdStickerReminder(profile: UserProfile, now: Long) {
        val last = profile.lastRewardedAdStickerAtEpochMs
        if (last <= 0L) return
        val readyAt = last + GameConstants.REWARDED_AD_COOLDOWN_MS
        if (readyAt <= now) return
        enqueue(
            ReminderContent(
                type = ReminderType.AD_STICKERS,
                title = "Free stickers ready",
                body = "Watch an ad to claim ${GameConstants.REWARDED_AD_STICKERS} bonus stickers.",
                route = Routes.HOME,
            ),
            readyAt,
        )
    }

    private fun scheduleSlotAdReminder(profile: UserProfile, now: Long) {
        val last = profile.lastRewardedSlotSpinAtEpochMs
        if (last <= 0L) return
        val readyAt = last + GameConstants.REWARDED_SLOT_SPIN_COOLDOWN_MS
        if (readyAt <= now) return
        enqueue(
            ReminderContent(
                type = ReminderType.SLOT_AD,
                title = "Bonus slot spins ready",
                body = "Watch an ad to claim ${GameConstants.REWARDED_SLOT_SPINS} extra slot spins.",
                route = Routes.SLOT,
            ),
            readyAt,
        )
    }

    private fun scheduleLoginPackReminder(profile: UserProfile, now: Long) {
        val last = profile.lastLoginPackGrantedAtEpochMs
        if (last <= 0L) return
        val readyAt = last + GameConstants.LOGIN_REWARD_INTERVAL_MS
        if (readyAt <= now) return
        enqueue(
            ReminderContent(
                type = ReminderType.LOGIN_PACK,
                title = "Login pack ready",
                body = "Open your free sticker pack with ${GameConstants.STICKERS_PER_PACK} stickers.",
                route = Routes.HOME,
            ),
            readyAt,
        )
    }

    private fun scheduleSlotDailyReset(now: Long) {
        enqueue(
            ReminderContent(
                type = ReminderType.SLOT_DAILY_RESET,
                title = "Slot spins reset",
                body = "Your ${GameConstants.DAILY_FREE_SLOT_SPINS} free slot spins are waiting!",
                route = Routes.SLOT,
            ),
            DateUtils.nextUtcMidnightEpochMs(now),
        )
    }

    private fun scheduleUnopenedPackReminder(profile: UserProfile, now: Long) {
        if (profile.unopenedPacks <= 0) return
        val packs = profile.unopenedPacks
        val label = if (packs == 1) "1 pack" else "$packs packs"
        enqueue(
            ReminderContent(
                type = ReminderType.UNOPENED_PACKS,
                title = "Packs waiting for you",
                body = "You have $label to open in your album.",
                route = Routes.PACK_OPEN,
            ),
            DateUtils.nextUtcHourEpochMs(UNOPENED_PACK_NUDGE_HOUR_UTC, now),
        )
    }

    private fun enqueue(content: ReminderContent, triggerAtMs: Long) {
        val delayMs = (triggerAtMs - System.currentTimeMillis()).coerceAtLeast(1L)
        val input = Data.Builder()
            .putString(RewardReminderWorker.KEY_TYPE, content.type.name)
            .putString(RewardReminderWorker.KEY_TITLE, content.title)
            .putString(RewardReminderWorker.KEY_BODY, content.body)
            .putString(RewardReminderWorker.KEY_ROUTE, content.route)
            .build()
        val request = OneTimeWorkRequestBuilder<RewardReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(input)
            .addTag(WORK_TAG)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            content.type.workName,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private companion object {
        const val WORK_TAG = "reward_reminders"
        const val UNOPENED_PACK_NUDGE_HOUR_UTC = 17
    }
}
