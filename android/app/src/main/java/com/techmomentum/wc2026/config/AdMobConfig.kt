package com.techmomentum.wc2026.config

import com.techmomentum.wc2026.BuildConfig

/**
 * AdMob IDs — **debug** builds use Google's official test units; **release** uses [local.properties].
 *
 * Set production unit IDs in [android/local.properties] (used by release only):
 * ```
 * admob.app.id=ca-app-pub-YOUR_APP_ID
 * admob.rewarded.unit.id=ca-app-pub-YOUR_UNIT_ID/rewarded
 * admob.interstitial.unit.id=ca-app-pub-YOUR_UNIT_ID/interstitial
 * admob.banner.unit.id=ca-app-pub-YOUR_UNIT_ID/banner
 * ```
 * See [android/local.properties.example].
 */
object AdMobConfig {
    /** Google sample app ID — always use for development unless overridden. */
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    /** Google sample rewarded unit — always use for development unless overridden. */
    const val TEST_REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    /** Google sample interstitial unit — always use for development unless overridden. */
    const val TEST_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    /** Google sample banner unit — always use for development unless overridden. */
    const val TEST_BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    val appId: String get() = BuildConfig.ADMOB_APP_ID

    val rewardedUnitId: String
        get() = if (useTestAds) TEST_REWARDED_UNIT_ID else BuildConfig.REWARDED_AD_UNIT_ID

    val interstitialUnitId: String
        get() = if (useTestAds) TEST_INTERSTITIAL_UNIT_ID else BuildConfig.INTERSTITIAL_AD_UNIT_ID

    val bannerUnitId: String
        get() = if (useTestAds) TEST_BANNER_UNIT_ID else BuildConfig.BANNER_AD_UNIT_ID

    val useTestAds: Boolean get() = BuildConfig.USE_TEST_ADMOB
}
