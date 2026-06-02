package com.techmomentum.wc2026.config

import com.techmomentum.wc2026.BuildConfig

/**
 * Single access point for project-wide constants (synced via [project.config.json]).
 * Regenerate / validate: `./scripts/sync_project.sh`
 */
object ProjectConfig {
    const val FIREBASE_PROJECT_ID = BuildConfig.FIREBASE_PROJECT_ID
    const val ANDROID_PACKAGE = BuildConfig.ANDROID_PACKAGE
    const val FUNCTIONS_REGION = BuildConfig.FUNCTIONS_REGION
    const val STORAGE_BUCKET = BuildConfig.STORAGE_BUCKET

    const val DATA_CONNECT_SERVICE_ID = BuildConfig.DATA_CONNECT_SERVICE_ID
    const val DATA_CONNECT_LOCATION = BuildConfig.DATA_CONNECT_LOCATION
    const val DATA_CONNECT_INSTANCE_ID = BuildConfig.DATA_CONNECT_INSTANCE_ID
    const val DATA_CONNECT_DATABASE = BuildConfig.DATA_CONNECT_DATABASE

    const val EMULATOR_AUTH_PORT = BuildConfig.EMULATOR_AUTH_PORT
    const val EMULATOR_FUNCTIONS_PORT = BuildConfig.EMULATOR_FUNCTIONS_PORT
    const val EMULATOR_FIRESTORE_PORT = BuildConfig.EMULATOR_FIRESTORE_PORT
    const val EMULATOR_STORAGE_PORT = BuildConfig.EMULATOR_STORAGE_PORT
    const val EMULATOR_DATA_CONNECT_PORT = BuildConfig.EMULATOR_DATA_CONNECT_PORT

    const val GCP_PUBLIC_PROJECT_ID = BuildConfig.GCP_PUBLIC_PROJECT_ID
    const val FIREBASE_PROJECT_NUMBER = BuildConfig.FIREBASE_PROJECT_NUMBER
    const val GOOGLE_WEB_CLIENT_ID = BuildConfig.GOOGLE_WEB_CLIENT_ID
    val GOOGLE_SIGN_IN_ENABLED: Boolean = BuildConfig.GOOGLE_SIGN_IN_ENABLED

    // Documented in project.config.json — keep GameConstants.kt in sync
    const val EXPECTED_PROJECT_ID = "wc-2026-3110f"
    const val EXPECTED_ANDROID_PACKAGE = "com.techmomentum.wc2026"
}
