package com.techmomentum.wc2026.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.techmomentum.wc2026.data.auth.GoogleAuthClient
import com.techmomentum.wc2026.data.firebase.toAuthUserMessage
import com.techmomentum.wc2026.debug.DebugAgentLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

sealed class AppAuthState {
    data object Unauthenticated : AppAuthState()
    data class SignedIn(val user: FirebaseUser) : AppAuthState()
}

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val googleAuthClient: GoogleAuthClient,
    @Named("application") private val appScope: CoroutineScope,
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isEmailVerified: Boolean
        get() = auth.currentUser?.isEmailVerified == true

    val isPasswordAccount: Boolean
        get() = auth.currentUser?.providerData?.any { it.providerId == "password" } == true

    fun authState(): Flow<AppAuthState> = firebaseAuthFlow().map { firebaseUser ->
        when {
            firebaseUser != null -> AppAuthState.SignedIn(firebaseUser)
            else -> AppAuthState.Unauthenticated
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> =
        runAuthWithRetry(block = {
            auth.signInWithEmailAndPassword(email, password).await()
        })

    suspend fun signUp(email: String, password: String, displayName: String): Result<Unit> =
        runAuthWithRetry(block = {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val profile = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            result.user?.updateProfile(profile)?.await()
        })

    suspend fun reloadAuthUser(): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("Not signed in.")
        user.reload().await()
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> = runAuthWithRetry(block = {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    })

    private suspend fun runAuthWithRetry(
        attempts: Int = 3,
        block: suspend () -> Unit,
    ): Result<Unit> {
        var lastError: Exception? = null
        repeat(attempts) { attempt ->
            try {
                block()
                // #region agent log
                DebugAgentLog.log(
                    location = "AuthRepository.kt:runAuthWithRetry",
                    message = "auth success",
                    hypothesisId = "H1",
                    data = mapOf("uid" to (auth.currentUser?.uid ?: "null")),
                )
                // #endregion
                return Result.success(Unit)
            } catch (e: Exception) {
                lastError = e as? Exception ?: Exception(e)
                if (!isRetryableNetworkError(e) || attempt == attempts - 1) {
                    return Result.failure(Exception(e.toAuthUserMessage(), e))
                }
                delay(800L * (attempt + 1))
            }
        }
        return Result.failure(Exception(lastError?.toAuthUserMessage() ?: "Authentication failed"))
    }

    private fun isRetryableNetworkError(e: Throwable): Boolean {
        val msg = (e.message ?: "") + (e.cause?.message ?: "")
        return msg.contains("network", ignoreCase = true) ||
            msg.contains("timeout", ignoreCase = true) ||
            msg.contains("unreachable", ignoreCase = true) ||
            (e is FirebaseAuthException && e.errorCode == "ERROR_NETWORK_REQUEST_FAILED")
    }

    fun signOut() {
        auth.signOut()
        appScope.launch { googleAuthClient.signOutGoogle() }
    }

    private fun firebaseAuthFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(auth.currentUser) }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
}
