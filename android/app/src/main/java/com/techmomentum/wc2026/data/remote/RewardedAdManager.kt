package com.techmomentum.wc2026.data.remote

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.techmomentum.wc2026.config.AdMobConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Rewarded ads for sticker + slot spin bonuses. Uses test AdMob until production IDs are set. */
@Singleton
class RewardedAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var rewardedAd: RewardedAd? = null

    fun load(onReady: (() -> Unit)? = null, onFailed: ((String) -> Unit)? = null) {
        RewardedAd.load(
            context,
            AdMobConfig.rewardedUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    onReady?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    onFailed?.invoke(error.message)
                }
            },
        )
    }

    fun show(activity: Activity, onReward: () -> Unit, onDismiss: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            load(onReady = { show(activity, onReward, onDismiss) })
            return
        }
        ad.show(activity) {
            onReward()
        }
        rewardedAd = null
        onDismiss()
    }
}
