package com.techmomentum.wc2026.data.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.techmomentum.wc2026.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val isConfigured: Boolean
        get() = BuildConfig.GOOGLE_SIGN_IN_ENABLED

    val hasWebClientId: Boolean
        get() = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    val hasAndroidOAuthClient: Boolean
        get() = BuildConfig.GOOGLE_ANDROID_OAUTH_CONFIGURED

    val publicGcpProjectId: String
        get() = BuildConfig.GCP_PUBLIC_PROJECT_ID

    fun getSignInIntent(): Intent? {
        val unavailableReason = signInUnavailableReason()
        if (unavailableReason != null) {
            GoogleSignInDiagnostics.logStep("getSignInIntent", "skipped — $unavailableReason")
            return null
        }
        GoogleSignInDiagnostics.logStep("getSignInIntent", "launching account picker")
        return googleSignInClient().signInIntent
    }

    fun signInUnavailableReason(): String? {
        if (!isConfigured) {
            return "Google Sign-In is not configured."
        }
        val playServicesStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (playServicesStatus != ConnectionResult.SUCCESS) {
            return "Google Play services is unavailable or outdated (code $playServicesStatus)."
        }
        return null
    }

    fun idTokenFromResult(data: Intent?): Result<String> {
        return try {
            if (data == null) {
                return Result.failure(Exception("Google sign-in returned no account data."))
            }
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)
            GoogleSignInDiagnostics.logStep(
                "idTokenFromResult",
                "account email=${account.email ?: "?"} hasToken=${!account.idToken.isNullOrBlank()}",
            )
            tokenFromAccount(account)
        } catch (e: ApiException) {
            GoogleSignInDiagnostics.logError("GoogleSignIn ApiException status=${e.statusCode}", e)
            Result.failure(Exception(mapGoogleApiException(e), e))
        } catch (e: Exception) {
            GoogleSignInDiagnostics.logError("idTokenFromResult failed", e)
            Result.failure(e)
        }
    }

    suspend fun signOutGoogle() {
        if (!hasWebClientId) return
        runCatching { googleSignInClient().signOut() }
    }

    fun configurationHint(): String? {
        signInUnavailableReason()?.let { reason ->
            if (reason.contains("Google Play services")) {
                return "Google Play services is required for Google sign-in. Update Play services and retry."
            }
        }
        if (!hasWebClientId) {
            return "Enable Google in Firebase Authentication, then re-download google-services.json."
        }
        if (!hasAndroidOAuthClient) {
            return "Add your debug SHA-1 in Firebase (run ./scripts/android_debug_sha.sh), " +
                "then re-download google-services.json and rebuild."
        }
        return null
    }

    private fun googleSignInClient() = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build(),
    )

    private fun tokenFromAccount(account: GoogleSignInAccount?): Result<String> {
        val token = account?.idToken
        return if (token.isNullOrBlank()) {
            Result.failure(
                Exception("Google sign-in did not return an ID token. Re-download google-services.json from Firebase."),
            )
        } else {
            Result.success(token)
        }
    }

    private fun mapGoogleApiException(e: ApiException): String = when (e.statusCode) {
        12501 -> "Google sign-in was cancelled."
        12500 -> "Google sign-in failed on this device. Check Google Play services and connected account."
        13 -> "Google sign-in failed due to an unavailable service. Please try again."
        10 -> if (BuildConfig.DEBUG) {
            "Google sign-in misconfigured (code 10). Add debug SHA-1 in Firebase " +
                "(./scripts/android_debug_sha.sh), re-download google-services.json, then rebuild."
        } else {
            "Google sign-in misconfigured (code 10). Release builds need the release SHA-1 in Firebase " +
                "(./scripts/android_release_sha.sh), re-download google-services.json, then rebuild."
        }
        7 -> "Google Sign-In failed: network error. Check connection and try again."
        8 -> "Google Sign-In failed: internal error. Update Google Play services and retry."
        else -> e.message ?: "Google sign-in failed (code ${e.statusCode})"
    }
}
