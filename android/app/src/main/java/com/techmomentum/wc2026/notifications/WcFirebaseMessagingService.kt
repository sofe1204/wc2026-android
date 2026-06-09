package com.techmomentum.wc2026.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.techmomentum.wc2026.data.repository.FcmTokenRepository
import com.techmomentum.wc2026.ui.navigation.Routes
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WcFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var fcmTokenRepository: FcmTokenRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch { fcmTokenRepository.registerCurrentToken() }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title
            ?: message.data["title"]
            ?: return
        val body = message.notification?.body
            ?: message.data["body"]
            ?: return
        val route = message.data["route"] ?: Routes.HOME
        NotificationHelper.showPushMessage(
            context = applicationContext,
            title = title,
            body = body,
            route = route,
        )
    }
}
