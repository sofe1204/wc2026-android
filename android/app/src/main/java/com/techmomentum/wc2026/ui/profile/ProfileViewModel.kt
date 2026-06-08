package com.techmomentum.wc2026.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.repository.AuthRepository
import com.techmomentum.wc2026.data.repository.CatalogRepository
import com.techmomentum.wc2026.data.repository.ProfileAvatarRepository
import com.techmomentum.wc2026.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileAvatarUiState(
    val uploading: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val catalogRepository: CatalogRepository,
    private val profileAvatarRepository: ProfileAvatarRepository,
    userRepository: UserRepository,
) : ViewModel() {
    val email: String
        get() = authRepository.currentUser?.email.orEmpty()

    val displayName: String
        get() = authRepository.currentUser?.displayName.orEmpty()

    val profile = userRepository.observeUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _totalCollectibleStickers = MutableStateFlow(0)
    val totalCollectibleStickers: StateFlow<Int> = _totalCollectibleStickers.asStateFlow()

    private val _avatarUiState = MutableStateFlow(ProfileAvatarUiState())
    val avatarUiState: StateFlow<ProfileAvatarUiState> = _avatarUiState.asStateFlow()

    init {
        viewModelScope.launch {
            _totalCollectibleStickers.value = catalogRepository.getCollectibleStickerCount()
        }
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            _avatarUiState.value = ProfileAvatarUiState(uploading = true, message = null)
            profileAvatarRepository.uploadAvatar(uri).fold(
                onSuccess = {
                    _avatarUiState.value = ProfileAvatarUiState(
                        uploading = false,
                        message = "Profile photo updated.",
                    )
                },
                onFailure = { error ->
                    _avatarUiState.value = ProfileAvatarUiState(
                        uploading = false,
                        message = error.message ?: "Could not upload photo.",
                    )
                },
            )
        }
    }

    fun clearAvatarMessage() {
        _avatarUiState.value = _avatarUiState.value.copy(message = null)
    }

    fun signOut() = authRepository.signOut()
}
