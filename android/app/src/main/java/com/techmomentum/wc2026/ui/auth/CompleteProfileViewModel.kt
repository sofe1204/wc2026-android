package com.techmomentum.wc2026.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.model.CountryOption
import com.techmomentum.wc2026.data.model.ProfileUpdateRequest
import com.techmomentum.wc2026.data.repository.ProfileRepository
import com.techmomentum.wc2026.ui.navigation.Routes
import com.techmomentum.wc2026.utils.WorldCountries
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CompleteProfileUiState(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val selectedCountry: CountryOption? = null,
    val countries: List<CountryOption> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val destinationRoute: String? = null,
)

@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    worldCountries: WorldCountries,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        CompleteProfileUiState(countries = worldCountries.all()),
    )
    val uiState: StateFlow<CompleteProfileUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value, error = null) }
    fun onFirstNameChange(value: String) = _uiState.update { it.copy(firstName = value, error = null) }
    fun onLastNameChange(value: String) = _uiState.update { it.copy(lastName = value, error = null) }
    fun onCountrySelected(country: CountryOption) =
        _uiState.update { it.copy(selectedCountry = country, error = null) }

    fun submit() {
        val state = _uiState.value
        val country = state.selectedCountry
        if (state.username.isBlank() || state.firstName.isBlank() || state.lastName.isBlank() || country == null) {
            _uiState.update { it.copy(error = "Fill in username, full name, surname, and country.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = profileRepository.updateUserProfile(
                ProfileUpdateRequest(
                    username = state.username.trim(),
                    firstName = state.firstName.trim(),
                    lastName = state.lastName.trim(),
                    countryCode = country.code,
                    countryName = country.name,
                ),
            )
            if (result.success) {
                _uiState.update {
                    it.copy(loading = false, destinationRoute = Routes.HOME, error = null)
                }
            } else {
                _uiState.update {
                    it.copy(loading = false, error = result.message.ifBlank { "Could not save profile." })
                }
            }
        }
    }
}
