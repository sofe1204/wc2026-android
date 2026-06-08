package com.techmomentum.wc2026.utils

/** Mirrors server rules in functions/src/profileHelpers.ts */
object ProfileValidation {
    private val USERNAME_RE = Regex("""^[a-zA-Z0-9_]{3,20}$""")

    fun validateUsername(username: String): String? {
        val value = username.trim()
        if (!USERNAME_RE.matches(value)) {
            return "Username must be 3–20 characters (letters, numbers, underscore)."
        }
        return null
    }
}
