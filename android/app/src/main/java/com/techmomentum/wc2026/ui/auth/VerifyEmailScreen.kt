package com.techmomentum.wc2026.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.ui.components.PixarCelebrationChip
import com.techmomentum.wc2026.ui.components.PixarPrimaryButton
import com.techmomentum.wc2026.ui.components.dismissKeyboardOnTap
import com.techmomentum.wc2026.ui.components.PixarSecondaryButton
import com.techmomentum.wc2026.ui.layout.AlbumPageScreen
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

@Composable
fun VerifyEmailScreen(
    onContinue: (String) -> Unit,
    viewModel: VerifyEmailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.destinationRoute) {
        state.destinationRoute?.let(onContinue)
    }

    AlbumPageScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .dismissKeyboardOnTap()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Verify your email",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = AlbumPageStyle.headerAccent,
            )
            Text(
                text = "We sent a verification link to ${state.email}. Confirm your email before continuing.",
                style = MaterialTheme.typography.bodyMedium,
                color = AlbumPageStyle.bottomNavUnselectedIcon,
                textAlign = TextAlign.Start,
            )
            state.message?.let { PixarCelebrationChip(message = it) }
            PixarPrimaryButton(
                text = "I've verified my email",
                onClick = viewModel::checkVerified,
                enabled = !state.loading,
                loading = state.loading,
            )
            PixarSecondaryButton(
                text = "Resend verification email",
                onClick = viewModel::resendVerification,
                enabled = !state.loading,
            )
        }
    }
}
