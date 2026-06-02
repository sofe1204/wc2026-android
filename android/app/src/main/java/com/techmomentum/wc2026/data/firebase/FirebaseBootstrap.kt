package com.techmomentum.wc2026.data.firebase

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.techmomentum.wc2026.BuildConfig
import com.techmomentum.wc2026.config.ProjectConfig

/**
 * Central Firebase wiring: correct Functions region, optional emulators, Firestore settings.
 * Call once from [com.techmomentum.wc2026.WorldCupApplication].
 */
object FirebaseBootstrap {
    private const val TAG = "FirebaseBootstrap"
    private const val EMULATOR_HOST = "10.0.2.2"

    fun create(application: Application): FirebaseServices {
        val app = FirebaseApp.initializeApp(application) ?: FirebaseApp.getInstance()
        Log.i(TAG, "Firebase app: ${app.options.projectId} (expected ${ProjectConfig.FIREBASE_PROJECT_ID})")

        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val functions = FirebaseFunctions.getInstance(ProjectConfig.FUNCTIONS_REGION)
        val storage = FirebaseStorage.getInstance()

        if (BuildConfig.USE_FIREBASE_EMULATORS) {
            Log.w(TAG, "Firebase EMULATOR mode — start: firebase emulators:start")
            auth.useEmulator(EMULATOR_HOST, ProjectConfig.EMULATOR_AUTH_PORT)
            firestore.useEmulator(EMULATOR_HOST, ProjectConfig.EMULATOR_FIRESTORE_PORT)
            functions.useEmulator(EMULATOR_HOST, ProjectConfig.EMULATOR_FUNCTIONS_PORT)
            storage.useEmulator(EMULATOR_HOST, ProjectConfig.EMULATOR_STORAGE_PORT)
        } else {
            Log.i(TAG, "Firebase production (project ${ProjectConfig.FIREBASE_PROJECT_ID})")
        }

        return FirebaseServices(auth, firestore, functions, storage)
    }
}
