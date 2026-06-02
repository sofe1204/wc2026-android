package com.techmomentum.wc2026.data.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.techmomentum.wc2026.config.ProjectConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class FirebaseConfigState {
    /** google-services.json still has placeholder values — replace from Firebase Console. */
    Placeholder,
    /** Project ID does not match [BuildConfig.FIREBASE_PROJECT_ID]. */
    ProjectMismatch,
    /** SDK configured for production or emulators. */
    Ready,
}

data class FirebaseConnectionStatus(
    val configState: FirebaseConfigState,
    val projectId: String,
    val functionsRegion: String,
    val useEmulators: Boolean,
    val isSignedIn: Boolean,
    val uid: String?,
) {
    val isCloudReady: Boolean
        get() = configState == FirebaseConfigState.Ready && (isSignedIn || useEmulators)

    val summary: String
        get() = when (configState) {
            FirebaseConfigState.Placeholder ->
                "Replace android/app/google-services.json from Firebase Console (project ${ProjectConfig.FIREBASE_PROJECT_ID})."
            FirebaseConfigState.ProjectMismatch ->
                "google-services.json project is \"$projectId\"; expected \"${ProjectConfig.FIREBASE_PROJECT_ID}\"."
            FirebaseConfigState.Ready -> when {
                useEmulators -> "Connected to Firebase emulators ($functionsRegion)."
                isSignedIn -> "Connected to Firebase ($projectId)."
                else -> "Firebase ready — sign in for cloud saves and rewards."
            }
        }
}

@Singleton
class FirebaseConnectionRepository @Inject constructor(
    private val auth: FirebaseAuth,
) {
    private val _configState = MutableStateFlow(detectConfigState())
    val configState: Flow<FirebaseConfigState> = _configState.asStateFlow()

    fun connectionStatus(): Flow<FirebaseConnectionStatus> =
        configState.map { state ->
            FirebaseConnectionStatus(
                configState = state,
                projectId = currentProjectId(),
                functionsRegion = ProjectConfig.FUNCTIONS_REGION,
                useEmulators = com.techmomentum.wc2026.BuildConfig.USE_FIREBASE_EMULATORS,
                isSignedIn = auth.currentUser != null,
                uid = auth.currentUser?.uid,
            )
        }

    fun refreshConfigState() {
        _configState.value = detectConfigState()
    }

    private fun currentProjectId(): String =
        FirebaseApp.getInstance().options.projectId.orEmpty()

    private fun detectConfigState(): FirebaseConfigState {
        val options = FirebaseApp.getInstance().options
        val projectId = options.projectId.orEmpty()
        val apiKey = options.apiKey.orEmpty()
        return when {
            projectId.contains("placeholder", ignoreCase = true) ||
                projectId.isBlank() ||
                apiKey.contains("REPLACE", ignoreCase = true) ||
                apiKey.contains("placeholder", ignoreCase = true) -> FirebaseConfigState.Placeholder
            projectId != ProjectConfig.FIREBASE_PROJECT_ID -> FirebaseConfigState.ProjectMismatch
            else -> FirebaseConfigState.Ready
        }
    }
}
