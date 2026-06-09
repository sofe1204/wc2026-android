package com.techmomentum.wc2026.notifications

import com.google.firebase.auth.FirebaseAuth
import com.techmomentum.wc2026.data.local.AppPreferences
import com.techmomentum.wc2026.data.repository.UserRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationWorkerEntryPoint {
    fun appPreferences(): AppPreferences
    fun notificationScheduler(): NotificationScheduler
    fun userRepository(): UserRepository
    fun firebaseAuth(): FirebaseAuth
}
