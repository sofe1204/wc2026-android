package com.techmomentum.wc2026.data.remote

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.techmomentum.wc2026.config.AdMobConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Full-screen interstitial — pack opening and bottom-nav tab switches (1 min cooldown). */
@Singleton
class InterstitialAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var interstitialAd: InterstitialAd? = null
    private var lastNavInterstitialAtMs = 0L

    val isReady: Boolean get() = interstitialAd != null

    fun load(onReady: (() -> Unit)? = null, onFailed: ((String) -> Unit)? = null) {
        InterstitialAd.load(
            context,
            AdMobConfig.interstitialUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    onReady?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    onFailed?.invoke(error.message)
                }
            },
        )
    }

    fun show(activity: Activity, onDismiss: () -> Unit) {
        present(activity, onDismiss = onDismiss, onShown = null)
    }

    /** Shows an interstitial when switching tabs if [NAV_COOLDOWN_MS] has elapsed since the last nav ad. */
    fun tryShowOnNavigation(activity: Activity, onContinue: () -> Unit) {
        val elapsed = System.currentTimeMillis() - lastNavInterstitialAtMs
        if (elapsed < NAV_COOLDOWN_MS) {
            onContinue()
            return
        }
        present(
            activity,
            onDismiss = onContinue,
            onShown = { lastNavInterstitialAtMs = System.currentTimeMillis() },
        )
    }

    private fun present(
        activity: Activity,
        onDismiss: () -> Unit,
        onShown: (() -> Unit)?,
    ) {
        val ad = interstitialAd
        if (ad == null) {
            onDismiss()
            load()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                onShown?.invoke()
                load()
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                load()
                onDismiss()
            }
        }
        ad.show(activity)
    }

    private companion object {
        const val NAV_COOLDOWN_MS = 60_000L
    }
}
