package com.techmomentum.wc2026.data.auth

import android.util.Log
import com.techmomentum.wc2026.BuildConfig

/** Logcat tag `WC2026_GoogleAuth` — filter in Android Studio while testing sign-in. */
object GoogleSignInDiagnostics {
    const val TAG = "WC2026_GoogleAuth"

    fun logConfiguration() {
        Log.i(TAG, summary())
        if (!BuildConfig.DEBUG) {
            Log.i(
                TAG,
                "Release build: ensure release SHA-1 is in Firebase (./scripts/android_release_sha.sh).",
            )
        }
    }

    fun logStep(step: String, detail: String? = null) {
        Log.d(TAG, if (detail.isNullOrBlank()) step else "$step — $detail")
    }

    fun logError(step: String, throwable: Throwable?) {
        Log.e(TAG, step, throwable)
    }

    fun summary(): String = buildString {
        append("enabled=${BuildConfig.GOOGLE_SIGN_IN_ENABLED}")
        append(", webClient=${BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()}")
        append(", androidOAuth=${BuildConfig.GOOGLE_ANDROID_OAUTH_CONFIGURED}")
        append(", build=${if (BuildConfig.DEBUG) "debug" else "release"}")
        if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()) {
            append(", webClientSuffix=...${BuildConfig.GOOGLE_WEB_CLIENT_ID.takeLast(24)}")
        }
    }

    fun debugPanelLines(): List<String> {
        if (!BuildConfig.DEBUG) return emptyList()
        return buildList {
            add("Logcat filter: $TAG")
            add("Build: debug")
            add("Sign-in enabled: ${BuildConfig.GOOGLE_SIGN_IN_ENABLED}")
            add("Web OAuth client (type 3): ${BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()}")
            add("Android OAuth client (type 1): ${BuildConfig.GOOGLE_ANDROID_OAUTH_CONFIGURED}")
            if (!BuildConfig.GOOGLE_SIGN_IN_ENABLED) {
                add("Fix: enable Google in Firebase → re-download google-services.json → rebuild")
            } else if (!BuildConfig.GOOGLE_ANDROID_OAUTH_CONFIGURED) {
                add("Fix: ./scripts/android_debug_sha.sh → add SHA-1 in Firebase → re-download json")
            }
        }
    }
}
