package com.techmomentum.wc2026.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.ui.album.AlbumOverviewBackground
import com.techmomentum.wc2026.ui.album.AlbumPageFrame
import com.techmomentum.wc2026.ui.components.PixarSecondaryButton
import com.techmomentum.wc2026.ui.navigation.Routes
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

@Composable
fun AuthScreen(
    onAuthenticated: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showWelcome by remember { mutableStateOf(false) }
    var pendingRoute by remember { mutableStateOf<String?>(null) }
    var welcomeSignUp by remember { mutableStateOf(false) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result -> viewModel.onGoogleSignInResult(result.data) }

    LaunchedEffect(state.success, state.destinationRoute) {
        if (state.success && !showWelcome) {
            pendingRoute = state.destinationRoute ?: Routes.HOME
            welcomeSignUp = state.isSignUp
            showWelcome = true
        }
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(AlbumPageStyle.backgroundGradient)),
        ) {
            AlbumOverviewBackground()

            AlbumPageFrame(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AuthHeroHeader()

                    AuthEmailCard(
                        isSignUp = state.isSignUp,
                        displayName = state.displayName,
                        email = state.email,
                        confirmEmail = state.confirmEmail,
                        password = state.password,
                        confirmPassword = state.confirmPassword,
                        error = state.error,
                        loading = state.loading,
                        onDisplayNameChange = viewModel::onDisplayNameChange,
                        onEmailChange = viewModel::onEmailChange,
                        onConfirmEmailChange = viewModel::onConfirmEmailChange,
                        onPasswordChange = viewModel::onPasswordChange,
                        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                        onSubmit = viewModel::submit,
                        onToggleMode = viewModel::toggleMode,
                    )

                    if (state.googleSignInAvailable) {
                        AuthOrDivider(label = "or")
                        PixarSecondaryButton(
                            text = "Continue with Google",
                            onClick = {
                                viewModel.getGoogleSignInIntent()?.let { googleLauncher.launch(it) }
                            },
                            enabled = !state.loading,
                        )
                        AuthHintText(
                            "New or returning — Google creates or signs in to your account.",
                        )
                    } else {
                        state.googleSetupHint?.let { hint ->
                            AuthOrDivider(label = "or")
                            AuthHintText(hint)
                        }
                    }

                    state.firebaseHint?.let { hint ->
                        AuthHintText(hint, modifier = Modifier.padding(top = 4.dp))
                    }
                    AuthHintText(
                        "Sign in with email or Google to save your album progress in the cloud.",
                    )
                }
            }

            if (showWelcome) {
                LoginWelcomeOverlay(
                    title = if (welcomeSignUp) "Welcome, collector!" else "Welcome back!",
                    subtitle = if (welcomeSignUp) {
                        "Verify your email, then start collecting."
                    } else {
                        "Your sticker album is ready."
                    },
                    onFinished = {
                        showWelcome = false
                        pendingRoute?.let(onAuthenticated)
                    },
                )
            }
        }
    }
}

@Composable
private fun AuthHintText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = AlbumPageStyle.bottomNavUnselectedIcon,
        textAlign = TextAlign.Center,
    )
}
