package com.techmomentum.wc2026.data.firebase

import com.google.firebase.auth.FirebaseAuthException
import com.techmomentum.wc2026.BuildConfig

fun Throwable.toAuthUserMessage(): String {
    findFirebaseAuthException(this)?.let { auth ->
        return mapFirebaseAuthException(auth)
    }
    val msg = message.orEmpty()
    if (msg.contains("network error", ignoreCase = true) ||
        msg.contains("unreachable host", ignoreCase = true) ||
        msg.contains("timeout", ignoreCase = true)
    ) {
        return networkHelpMessage()
    }
    return msg.ifBlank { "Authentication failed" }
}

private fun findFirebaseAuthException(throwable: Throwable): FirebaseAuthException? {
    var current: Throwable? = throwable
    while (current != null) {
        if (current is FirebaseAuthException) return current
        current = current.cause
    }
    return null
}

private fun mapFirebaseAuthException(auth: FirebaseAuthException): String = when (auth.errorCode) {
    "ERROR_OPERATION_NOT_ALLOWED" ->
        "Email/Password sign-in is disabled. In Firebase Console → Authentication → Sign-in method, enable Email/Password."
    "ERROR_EMAIL_ALREADY_IN_USE" ->
        "This email is already registered. Try Sign in instead."
    "ERROR_INVALID_EMAIL" ->
        "Invalid email address."
    "ERROR_WEAK_PASSWORD" ->
        "Password is too weak. Use at least 6 characters."
    "ERROR_USER_NOT_FOUND",
    "ERROR_INVALID_LOGIN_CREDENTIALS",
    "ERROR_INVALID_CREDENTIAL",
    "ERROR_WRONG_PASSWORD",
    ->
        INCORRECT_EMAIL_OR_PASSWORD
    "ERROR_TOO_MANY_REQUESTS" ->
        "Too many attempts. Wait a few minutes and try again."
    "ERROR_NETWORK_REQUEST_FAILED" ->
        networkHelpMessage()
    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
        "An account already exists with this email using a different sign-in method."
    else -> if (looksLikeInvalidEmailOrPassword(auth)) {
        INCORRECT_EMAIL_OR_PASSWORD
    } else {
        auth.message ?: "Authentication failed"
    }
}

private const val INCORRECT_EMAIL_OR_PASSWORD = "Incorrect email or password."

private fun looksLikeInvalidEmailOrPassword(auth: FirebaseAuthException): Boolean {
    val message = auth.message.orEmpty()
    return message.contains("supplied auth credential", ignoreCase = true) &&
        message.contains("incorrect", ignoreCase = true)
}

private fun networkHelpMessage(): String = buildString {
    append("Can't reach Firebase Authentication servers. ")
    append("Your emulator shows Wi‑Fi, but the app still needs a path to Google. Try: ")
    append("(1) Cold boot the emulator (AVD Manager → dropdown → Cold Boot Now), ")
    append("(2) Use an AVD with the Google Play icon (not 'Google APIs' only), ")
    append("(3) Try a physical phone on Wi‑Fi, ")
    append("(4) Disable VPN/firewall on your Mac. ")
    if (BuildConfig.USE_FIREBASE_EMULATORS) {
        append("firebase.emulators=true is ON — run 'firebase emulators:start' or set firebase.emulators=false in android/local.properties and rebuild. ")
    }
}
