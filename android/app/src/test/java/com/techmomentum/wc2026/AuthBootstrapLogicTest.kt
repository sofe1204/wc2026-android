package com.techmomentum.wc2026

import com.techmomentum.wc2026.data.firebase.FunctionsNotDeployedException
import com.techmomentum.wc2026.data.firebase.FunctionsUnauthenticatedException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Documents when Firestore bootstrap should run (mirrors RewardsRepository logic).
 */
class AuthBootstrapLogicTest {
    @Test
    fun functionsNotDeployed_triggersBootstrap() {
        assertTrue(shouldBootstrap(FunctionsNotDeployedException()))
    }

    @Test
    fun notFoundMessage_triggersBootstrap() {
        assertTrue(shouldBootstrap(Exception("NOT_FOUND")))
        assertTrue(shouldBootstrap(Exception("Cloud Functions not deployed (us-central1)")))
    }

    @Test
    fun unauthenticated_triggersBootstrap() {
        assertTrue(shouldBootstrap(FunctionsUnauthenticatedException()))
        assertTrue(shouldBootstrap(Exception("UNAUTHENTICATED")))
    }

    @Test
    fun weakPassword_doesNotTriggerBootstrap() {
        assertFalse(shouldBootstrap(Exception("Password is too weak")))
    }

    private fun shouldBootstrap(e: Exception): Boolean {
        if (e is FunctionsNotDeployedException) return true
        if (e.cause is FunctionsNotDeployedException) return true
        if (e is FunctionsUnauthenticatedException) return true
        if (e.cause is FunctionsUnauthenticatedException) return true
        val msg = (e.message ?: "") + (e.cause?.message ?: "")
        return msg.contains("NOT_FOUND", ignoreCase = true) ||
            msg.contains("Cloud Functions not deployed", ignoreCase = true) ||
            msg.contains("UNAUTHENTICATED", ignoreCase = true)
    }
}
