package com.techmomentum.wc2026.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.techmomentum.wc2026.config.AdMobConfig

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val adWidthDp = maxWidth.value.toInt().coerceAtLeast(320)
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val adSize = remember(adWidthDp, context) {
            AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, adWidthDp)
                .takeUnless { it == AdSize.INVALID }
                ?: AdSize.getInlineAdaptiveBannerAdSize(adWidthDp, 90)
        }
        val adView = remember {
            AdView(context).apply {
                adUnitId = AdMobConfig.bannerUnitId
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
        }

        LaunchedEffect(adSize) {
            adView.setAdSize(adSize)
            adView.loadAd(AdRequest.Builder().build())
        }

        DisposableEffect(lifecycleOwner, adView) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> adView.resume()
                    Lifecycle.Event.ON_PAUSE -> adView.pause()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                adView.destroy()
            }
        }

        AndroidView(
            factory = { adView },
            modifier = Modifier.fillMaxWidth(),
            update = { view ->
                view.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            },
        )
    }
}
