package com.techmomentum.wc2026

import com.techmomentum.wc2026.utils.ProfileValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileValidationTest {
    @Test
    fun validUsernames_accepted() {
        assertNull(ProfileValidation.validateUsername("aleksandar07"))
        assertNull(ProfileValidation.validateUsername("user_name"))
        assertNull(ProfileValidation.validateUsername("abc"))
    }

    @Test
    fun dotInUsername_rejected() {
        assertEquals(
            "Username must be 3–20 characters (letters, numbers, underscore).",
            ProfileValidation.validateUsername("aleksandar07.06"),
        )
    }

    @Test
    fun tooShortOrLong_rejected() {
        assertEquals(
            "Username must be 3–20 characters (letters, numbers, underscore).",
            ProfileValidation.validateUsername("ab"),
        )
        assertEquals(
            "Username must be 3–20 characters (letters, numbers, underscore).",
            ProfileValidation.validateUsername("a".repeat(21)),
        )
    }
}
