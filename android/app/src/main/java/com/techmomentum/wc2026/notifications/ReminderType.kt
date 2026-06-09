package com.techmomentum.wc2026.notifications

import com.techmomentum.wc2026.ui.navigation.Routes

enum class ReminderType(
    val workName: String,
    val notificationId: Int,
    val channelId: String,
) {
    AD_STICKERS(
        workName = "reminder_ad_stickers",
        notificationId = 1001,
        channelId = NotificationHelper.CHANNEL_REWARDS,
    ),
    SLOT_AD(
        workName = "reminder_slot_ad",
        notificationId = 1002,
        channelId = NotificationHelper.CHANNEL_REWARDS,
    ),
    LOGIN_PACK(
        workName = "reminder_login_pack",
        notificationId = 1003,
        channelId = NotificationHelper.CHANNEL_REWARDS,
    ),
    SLOT_DAILY_RESET(
        workName = "reminder_slot_daily_reset",
        notificationId = 1004,
        channelId = NotificationHelper.CHANNEL_DAILY,
    ),
    UNOPENED_PACKS(
        workName = "reminder_unopened_packs",
        notificationId = 1005,
        channelId = NotificationHelper.CHANNEL_DAILY,
    ),
    ;

    companion object {
        fun fromName(value: String?): ReminderType? =
            entries.firstOrNull { it.name == value }
    }
}

data class ReminderContent(
    val type: ReminderType,
    val title: String,
    val body: String,
    val route: String = Routes.HOME,
)
