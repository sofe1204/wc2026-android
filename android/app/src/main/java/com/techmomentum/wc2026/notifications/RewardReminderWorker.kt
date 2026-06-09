package com.techmomentum.wc2026.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors

class RewardReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            NotificationWorkerEntryPoint::class.java,
        )
        val preferences = entryPoint.appPreferences()
        if (!preferences.notificationsEnabled) return Result.success()
        if (!NotificationPermissionHelper.hasPermission(applicationContext)) return Result.success()

        val type = ReminderType.fromName(inputData.getString(KEY_TYPE)) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val body = inputData.getString(KEY_BODY).orEmpty()
        val route = inputData.getString(KEY_ROUTE).orEmpty()
        if (title.isBlank() || body.isBlank()) return Result.failure()

        NotificationHelper.showReminder(
            applicationContext,
            ReminderContent(type = type, title = title, body = body, route = route),
        )
        return Result.success()
    }

    companion object {
        const val KEY_TYPE = "type"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_ROUTE = "route"
    }
}
