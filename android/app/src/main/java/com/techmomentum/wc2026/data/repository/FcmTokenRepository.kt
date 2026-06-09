package com.techmomentum.wc2026.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.techmomentum.wc2026.data.firebase.CloudFunctionsClient
import com.techmomentum.wc2026.data.local.AppPreferences
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val cloudFunctions: CloudFunctionsClient,
    private val preferences: AppPreferences,
) {
    suspend fun registerCurrentToken() {
        if (!preferences.notificationsEnabled) return
        if (auth.currentUser == null) return
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            if (token.isNotBlank()) {
                cloudFunctions.registerFcmToken(token)
            }
        }
    }

    suspend fun clearToken() {
        if (auth.currentUser == null) return
        runCatching { cloudFunctions.clearFcmToken() }
    }
}
