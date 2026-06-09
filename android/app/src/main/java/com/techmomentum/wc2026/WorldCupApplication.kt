package com.techmomentum.wc2026

import android.app.Application
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.techmomentum.wc2026.config.AdMobConfig
import com.techmomentum.wc2026.data.firebase.FirebaseSessionCoordinator
import com.techmomentum.wc2026.dataconnect.DataConnectRuntime
import com.techmomentum.wc2026.notifications.NotificationCoordinator
import com.techmomentum.wc2026.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WorldCupApplication : Application() {
    @Inject lateinit var firebaseSessionCoordinator: FirebaseSessionCoordinator
    @Inject lateinit var notificationCoordinator: NotificationCoordinator

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
        if (AdMobConfig.useTestAds) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build(),
            )
        }
        MobileAds.initialize(this) {}
        DataConnectRuntime.configure(this)
        firebaseSessionCoordinator.start()
        notificationCoordinator.start()
    }
}
