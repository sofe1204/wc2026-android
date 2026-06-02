package com.techmomentum.wc2026

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.techmomentum.wc2026.data.firebase.FirebaseSessionCoordinator
import com.techmomentum.wc2026.dataconnect.DataConnectRuntime
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WorldCupApplication : Application() {
    @Inject lateinit var firebaseSessionCoordinator: FirebaseSessionCoordinator

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
        DataConnectRuntime.configure(this)
        firebaseSessionCoordinator.start()
    }
}
