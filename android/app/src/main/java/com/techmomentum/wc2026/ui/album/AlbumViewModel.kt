package com.techmomentum.wc2026.ui.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.domain.usecase.AlbumState
import com.techmomentum.wc2026.domain.usecase.GetAlbumUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AlbumFilter(
    val group: String? = null,
    val ownedOnly: Boolean? = null,
    val missingOnly: Boolean? = null,
)

@HiltViewModel
class AlbumViewModel @Inject constructor(
    getAlbumUseCase: GetAlbumUseCase,
) : ViewModel() {
    val albumState: StateFlow<AlbumState> = getAlbumUseCase.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlbumState())

    private val _filter = MutableStateFlow(AlbumFilter())
    val filter: StateFlow<AlbumFilter> = _filter.asStateFlow()

    fun setGroupFilter(group: String?) = _filter.update { it.copy(group = group) }
    fun setOwnedFilter(owned: Boolean?) = _filter.update { it.copy(ownedOnly = owned, missingOnly = null) }
    fun setMissingFilter() = _filter.update { it.copy(missingOnly = true, ownedOnly = null) }
}
