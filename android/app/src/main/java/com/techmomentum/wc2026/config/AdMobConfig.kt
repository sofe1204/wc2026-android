package com.techmomentum.wc2026.config

import com.techmomentum.wc2026.BuildConfig

/**
 * AdMob IDs — defaults are Google's official **test** units (safe for dev).
 *
 * Production: set in [android/local.properties]:
 * ```
 * admob.app.id=ca-app-pub-YOUR_APP_ID
 * admob.rewarded.unit.id=ca-app-pub-YOUR_UNIT_ID/rewarded
 * ```
 * Then rebuild. See [android/local.properties.example].
 */
object AdMobConfig {
    /** Google sample app ID — always use for development unless overridden. */
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    /** Google sample rewarded unit — always use for development unless overridden. */
    const val TEST_REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    val appId: String get() = TEST_APP_ID

    val rewardedUnitId: String get() = BuildConfig.REWARDED_AD_UNIT_ID

    val useTestAds: Boolean get() = BuildConfig.USE_TEST_ADMOB
}
