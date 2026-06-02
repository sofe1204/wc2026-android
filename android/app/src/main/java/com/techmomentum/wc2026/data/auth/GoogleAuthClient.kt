package com.techmomentum.wc2026.data.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.techmomentum.wc2026.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val isConfigured: Boolean
        get() = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    val publicGcpProjectId: String
        get() = BuildConfig.GCP_PUBLIC_PROJECT_ID

    fun getSignInIntent(): Intent? {
        if (!isConfigured) return null
        return googleSignInClient().signInIntent
    }

    fun idTokenFromResult(data: Intent?): Result<String> {
        return try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)
            tokenFromAccount(account)
        } catch (e: ApiException) {
            Result.failure(Exception(mapGoogleApiException(e), e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOutGoogle() {
        if (!isConfigured) return
        runCatching { googleSignInClient().signOut() }
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
        10 -> "Google sign-in misconfigured. Enable Google in Firebase Authentication, add your debug SHA-1, then re-download google-services.json."
        else -> e.message ?: "Google sign-in failed (code ${e.statusCode})"
    }
}
