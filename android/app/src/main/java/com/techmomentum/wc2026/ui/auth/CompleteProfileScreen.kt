package com.techmomentum.wc2026.ui.auth

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.ui.album.AlbumOverviewBackground
import com.techmomentum.wc2026.ui.album.AlbumPageFrame
import com.techmomentum.wc2026.ui.components.CountryPickerField
import com.techmomentum.wc2026.ui.components.PixarOutlinedTextField
import com.techmomentum.wc2026.ui.components.PixarPrimaryButton
import com.techmomentum.wc2026.ui.navigation.Routes
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

@Composable
fun CompleteProfileScreen(
    onContinue: (String) -> Unit,
    viewModel: CompleteProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showWelcome by remember { mutableStateOf(false) }

    LaunchedEffect(state.destinationRoute) {
        when (state.destinationRoute) {
            Routes.HOME -> showWelcome = true
            else -> state.destinationRoute?.let(onContinue)
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
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Complete your profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = AlbumPageStyle.headerAccent,
                    )
                    Text(
                        text = "Choose a leaderboard username, your full name, and country.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlbumPageStyle.bottomNavUnselectedIcon,
                    )
                    PixarOutlinedTextField(
                        value = state.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = "Username (leaderboard)",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PixarOutlinedTextField(
                        value = state.firstName,
                        onValueChange = viewModel::onFirstNameChange,
                        label = "First name",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PixarOutlinedTextField(
                        value = state.lastName,
                        onValueChange = viewModel::onLastNameChange,
                        label = "Surname",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    CountryPickerField(
                        countries = state.countries,
                        selected = state.selectedCountry,
                        onSelected = viewModel::onCountrySelected,
                        label = "Country",
                    )
                    state.error?.let { AuthErrorPill(message = it) }
                    PixarPrimaryButton(
                        text = "Save and continue",
                        onClick = viewModel::submit,
                        enabled = !state.loading,
                        loading = state.loading,
                    )
                }
            }

            if (showWelcome) {
                LoginWelcomeOverlay(
                    title = "You're all set!",
                    subtitle = "Time to fill your World Cup 2026 album.",
                    onFinished = { onContinue(Routes.HOME) },
                )
            }
        }
    }
}
