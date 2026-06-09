package com.techmomentum.wc2026

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.techmomentum.wc2026.data.remote.InterstitialAdManager
import com.techmomentum.wc2026.data.remote.RewardedAdManager
import com.techmomentum.wc2026.data.repository.AuthRepository
import com.techmomentum.wc2026.notifications.NotificationHelper
import com.techmomentum.wc2026.ui.navigation.AppNavigation
import com.techmomentum.wc2026.ui.theme.WorldCup2026Theme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var rewardedAdManager: RewardedAdManager
    @Inject lateinit var interstitialAdManager: InterstitialAdManager

    private var pendingNavRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingNavRoute = readNavRoute(intent)
        enableEdgeToEdge()
        setContent {
            WorldCup2026Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        authRepository = authRepository,
                        rewardedAdManager = rewardedAdManager,
                        interstitialAdManager = interstitialAdManager,
                        pendingNavRoute = pendingNavRoute,
                        onPendingNavRouteConsumed = { pendingNavRoute = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readNavRoute(intent)?.let { pendingNavRoute = it }
    }

    private fun readNavRoute(intent: Intent?): String? =
        intent?.getStringExtra(NotificationHelper.EXTRA_NAV_ROUTE)
}
