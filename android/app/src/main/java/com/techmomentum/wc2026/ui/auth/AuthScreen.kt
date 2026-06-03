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
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

@Composable
fun AuthScreen(
    @Suppress("UNUSED_PARAMETER") isGuestMode: Boolean,
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result -> viewModel.onGoogleSignInResult(result.data) }

    LaunchedEffect(state.success) {
        if (state.success) onAuthenticated()
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(AlbumPageStyle.backgroundGradient)),
        ) {
            AlbumOverviewBackground()

            AlbumPageFrame(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
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
                        password = state.password,
                        error = state.error,
                        loading = state.loading,
                        onDisplayNameChange = viewModel::onDisplayNameChange,
                        onEmailChange = viewModel::onEmailChange,
                        onPasswordChange = viewModel::onPasswordChange,
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

                    AuthOrDivider(label = "or")

                    PixarSecondaryButton(
                        text = "Continue as Guest (offline demo)",
                        onClick = viewModel::continueAsGuest,
                        enabled = !state.loading,
                        accentBorder = true,
                    )

                    state.firebaseHint?.let { hint ->
                        AuthHintText(hint, modifier = Modifier.padding(top = 4.dp))
                    }
                    AuthHintText(
                        "Email or Google saves progress in the cloud. Guest mode is offline only.",
                    )
                }
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
