package com.techmomentum.wc2026.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.repository.RewardsRepository
import com.techmomentum.wc2026.domain.usecase.GetSwapDeckUseCase
import com.techmomentum.wc2026.domain.usecase.SwapDeckState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SwapDeckUiState(
    val loading: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class SwapDeckViewModel @Inject constructor(
    getSwapDeckUseCase: GetSwapDeckUseCase,
    private val rewardsRepository: RewardsRepository,
) : ViewModel() {
    val swapDeckState: StateFlow<SwapDeckState> = getSwapDeckUseCase.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SwapDeckState())

    private val _uiState = MutableStateFlow(SwapDeckUiState())
    val uiState: StateFlow<SwapDeckUiState> = _uiState.asStateFlow()

    fun redeemForPack() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, message = null) }
            runCatching { rewardsRepository.redeemSwapDeck() }.fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            message = result.message.ifBlank { "Done!" },
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, message = e.message) }
                },
            )
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }
}
