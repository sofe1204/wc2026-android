package com.techmomentum.wc2026.dataconnect

import android.content.Context
import com.techmomentum.wc2026.BuildConfig
import com.techmomentum.wc2026.config.ProjectConfig

/**
 * Kotlin entry point for Firebase SQL Connect.
 * Call [configure] from [com.techmomentum.wc2026.WorldCupApplication].
 */
object DataConnectRuntime {
    /** Synced from [project.config.json] / [dataconnect/dataconnect.yaml]. */
    val SERVICE_ID: String get() = ProjectConfig.DATA_CONNECT_SERVICE_ID

    /** Synced from [project.config.json]. */
    val LOCATION: String get() = ProjectConfig.DATA_CONNECT_LOCATION

    fun isEnabled(): Boolean = BuildConfig.USE_SQL_CONNECT

    fun configure(@Suppress("UNUSED_PARAMETER") context: Context) {
        if (!isEnabled()) return
        // After `firebase dataconnect:sdk:generate`, use generated DefaultConnector.instance.
        // if (BuildConfig.USE_FIREBASE_EMULATORS) {
        //     DefaultConnector.instance.useEmulator("10.0.2.2", 9399)
        // }
    }
}
