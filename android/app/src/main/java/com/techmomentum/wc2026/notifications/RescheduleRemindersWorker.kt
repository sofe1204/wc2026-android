package com.techmomentum.wc2026.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class RescheduleRemindersWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            NotificationWorkerEntryPoint::class.java,
        )
        val preferences = entryPoint.appPreferences()
        val scheduler = entryPoint.notificationScheduler()
        if (!preferences.notificationsEnabled) {
            scheduler.cancelAll()
            return Result.success()
        }
        if (entryPoint.firebaseAuth().currentUser == null) {
            scheduler.cancelAll()
            return Result.success()
        }
        val profile = entryPoint.userRepository().observeUserProfile().first()
        scheduler.reschedule(profile)
        return Result.success()
    }
}
