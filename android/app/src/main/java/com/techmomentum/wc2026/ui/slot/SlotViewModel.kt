package com.techmomentum.wc2026.ui.slot

import android.app.Activity
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techmomentum.wc2026.data.local.AppPreferences
import com.techmomentum.wc2026.data.model.SlotResult
import com.techmomentum.wc2026.data.model.SlotSymbol
import com.techmomentum.wc2026.data.remote.RewardedAdManager
import com.techmomentum.wc2026.data.repository.CatalogRepository
import com.techmomentum.wc2026.data.slot.SlotSoundPlayer
import com.techmomentum.wc2026.data.slot.SlotSymbolsWarmup
import com.techmomentum.wc2026.data.repository.RewardsRepository
import com.techmomentum.wc2026.data.repository.UserRepository
import com.techmomentum.wc2026.utils.RewardEligibility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

enum class SlotSpinPhase {
    Idle,
    Spinning,
    Settling,
}

data class SlotUiState(
    val grid: List<List<SlotSymbol?>> = List(3) { List(3) { null } },
    val targetGrid: List<List<SlotSymbol?>> = List(3) { List(3) { null } },
    val spinPhase: SlotSpinPhase = SlotSpinPhase.Idle,
    val settledColumnCount: Int = 0,
    val spinGeneration: Int = 0,
    val symbolPool: List<SlotSymbol> = emptyList(),
    val symbolsReady: Boolean = false,
    val spinsRemaining: Int = 0,
    val packsWonToday: Int = 0,
    val isWin: Boolean = false,
    val winningCells: Set<Pair<Int, Int>> = emptySet(),
    val hapticsEnabled: Boolean = true,
    val message: String? = null,
    val slotSpinAdAvailable: Boolean = true,
    val slotSpinAdCooldownMinutes: Int = 0,
    /** Bumped when pinned reel images finish loading so cells recompose. */
    val imageRefreshGeneration: Int = 0,
) {
    val isAnimating: Boolean get() = spinPhase != SlotSpinPhase.Idle
}

@HiltViewModel
class SlotViewModel @Inject constructor(
    private val rewardsRepository: RewardsRepository,
    private val catalogRepository: CatalogRepository,
    private val userRepository: UserRepository,
    private val rewardedAdManager: RewardedAdManager,
    private val slotSymbolsWarmup: SlotSymbolsWarmup,
    private val slotSoundPlayer: SlotSoundPlayer,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SlotUiState())
    val uiState: StateFlow<SlotUiState> = _uiState.asStateFlow()

    private var symbolsById: Map<String, SlotSymbol> = emptyMap()
    private var symbolList: List<SlotSymbol> = emptyList()

    init {
        viewModelScope.launch {
            loadSymbols()
            showPreviewGrid()
            // Show the cabinet as soon as the symbols are known. Images are pinned in
            // memory during bootstrap, so cells paint instantly; warmIfNeeded() below is
            // a fast no-op when already warmed.
            _uiState.update { it.copy(symbolsReady = true) }
            slotSymbolsWarmup.warmIfNeeded()
            _uiState.update { it.copy(imageRefreshGeneration = it.imageRefreshGeneration + 1) }
            rewardedAdManager.load()
        }
        viewModelScope.launch {
            userRepository.observeUserProfile().collect { profile ->
                if (profile != null) {
                    val lastAd = profile.lastRewardedSlotSpinAtEpochMs
                    _uiState.update {
                        it.copy(
                            spinsRemaining = profile.slotSpinsRemaining,
                            packsWonToday = profile.slotRewardPacksWonToday,
                            slotSpinAdAvailable = RewardEligibility.isSlotSpinAdAvailable(lastAd),
                            slotSpinAdCooldownMinutes = RewardEligibility.slotSpinAdCooldownMinutesRemaining(lastAd),
                        )
                    }
                }
            }
        }
    }

    fun spin() {
        val current = _uiState.value
        if (current.spinPhase != SlotSpinPhase.Idle || current.spinsRemaining <= 0) return

        viewModelScope.launch {
            ensureSymbols()
            _uiState.update {
                it.copy(
                    spinPhase = SlotSpinPhase.Spinning,
                    settledColumnCount = 0,
                    spinGeneration = it.spinGeneration + 1,
                    message = null,
                    isWin = false,
                    winningCells = emptySet(),
                    symbolPool = symbolList,
                )
            }
            slotSoundPlayer.playSpin()

            val resultDeferred = async {
                runCatching { rewardsRepository.spinSlotMachine() }
            }
            val minSpinDeferred = async { delay(SPIN_MIN_DURATION_MS) }

            val result = resultDeferred.await()
            minSpinDeferred.await()

            result.fold(
                onSuccess = { slotResult -> playSettleAnimation(slotResult) },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            spinPhase = SlotSpinPhase.Idle,
                            message = e.message,
                        )
                    }
                },
            )
        }
    }

    fun watchAdForSpins(activity: Activity, rewardedAdManager: RewardedAdManager) {
        if (!_uiState.value.slotSpinAdAvailable) return
        rewardedAdManager.show(
            activity = activity,
            onReward = {
                viewModelScope.launch {
                    runCatching { rewardsRepository.claimRewardedSlotSpins() }
                        .onSuccess { result ->
                            _uiState.update { s ->
                                s.copy(message = result.message.ifBlank { "Done!" })
                            }
                        }
                        .onFailure { e ->
                            _uiState.update { s -> s.copy(message = e.message) }
                        }
                }
            },
            onDismiss = {},
        )
    }

    private suspend fun playSettleAnimation(result: SlotResult) {
        ensureSymbols()
        val targetGrid = result.grid.mapIndexed { row, rowIds ->
            rowIds.mapIndexed { col, id -> resolveSymbolOrFallback(id, row, col) }
        }

        _uiState.update {
            it.copy(
                spinPhase = SlotSpinPhase.Settling,
                targetGrid = targetGrid,
                settledColumnCount = 0,
            )
        }

        for (col in 0 until COLS) {
            if (col > 0) delay(COLUMN_STOP_DELAY_MS)
            _uiState.update { it.copy(settledColumnCount = col + 1) }
            slotSoundPlayer.playReelStop()
        }

        delay(SLOT_REEL_LAND_DURATION_MS.toLong())
        slotSymbolsWarmup.warmIfNeeded()

        // Derive the highlight from the SAME resolved grid that is shown on screen
        // (by symbolId), so the pulsing line always matches the pictures the user sees.
        val winning = if (result.isWin) {
            winningCells(targetGrid.map { rowSymbols -> rowSymbols.map { it.symbolId } })
        } else {
            emptySet()
        }

        _uiState.update {
            it.copy(
                grid = targetGrid,
                targetGrid = targetGrid,
                spinPhase = SlotSpinPhase.Idle,
                settledColumnCount = COLS,
                spinsRemaining = result.spinsRemaining,
                packsWonToday = result.packsWonToday,
                isWin = result.isWin,
                winningCells = winning,
                imageRefreshGeneration = it.imageRefreshGeneration + 1,
                hapticsEnabled = appPreferences.hapticsEnabled,
                message = result.message.ifBlank {
                    when {
                        result.rewardGranted -> "You won a sticker pack!"
                        result.isWin -> "Line match! Daily pack limit reached."
                        else -> "No line match — try again!"
                    }
                },
            )
        }

        if (result.isWin) {
            slotSoundPlayer.playWin()
        }
    }

    private suspend fun loadSymbols() {
        symbolList = catalogRepository.getSlotSymbols().filter { it.isActive }
        symbolsById = buildSymbolLookup(symbolList)
    }

    private suspend fun ensureSymbols() {
        if (symbolsById.isEmpty()) {
            loadSymbols()
        }
        slotSymbolsWarmup.warmIfNeeded()
    }

    private fun showPreviewGrid() {
        if (symbolList.isEmpty()) return
        val grid = List(3) {
            List(3) { symbolList[Random.nextInt(symbolList.size)] }
        }
        _uiState.update {
            it.copy(
                grid = grid,
                targetGrid = grid,
                symbolPool = symbolList,
            )
        }
    }

    fun slotBitmap(url: String): ImageBitmap? = slotSymbolsWarmup.bitmapFor(url)

    private fun resolveSymbol(id: String): SlotSymbol? = symbolsById[id.trim()]

    private fun resolveSymbolOrFallback(id: String, row: Int, col: Int): SlotSymbol {
        resolveSymbol(id)?.let { return it }
        // The spin result referenced a symbol id the client catalog does not know
        // (catalog out of sync with the spin source). Render an explicit "unknown"
        // cell instead of substituting a real player image — otherwise a losing line
        // could look like a winning one. Keep the real id so distinct ids stay distinct
        // and the win check / highlight remain faithful to what was scored.
        return SlotSymbol(
            symbolId = id.ifBlank { "unknown_${col}_$row" },
            label = "?",
        )
    }

    private fun buildSymbolLookup(symbols: List<SlotSymbol>): Map<String, SlotSymbol> =
        buildMap {
            symbols.forEach { symbol ->
                put(symbol.symbolId, symbol)
                if (symbol.playerId.isNotBlank()) {
                    put(symbol.playerId, symbol)
                }
                if (symbol.documentId.isNotBlank() && symbol.documentId != symbol.symbolId) {
                    put(symbol.documentId, symbol)
                }
            }
        }

    companion object {
        private const val SPIN_MIN_DURATION_MS = 2_000L
        private const val COLUMN_STOP_DELAY_MS = 450L
        private const val COLS = 3

        fun isColumnSpinning(phase: SlotSpinPhase, settledColumnCount: Int, column: Int): Boolean =
            when (phase) {
                SlotSpinPhase.Idle -> false
                SlotSpinPhase.Spinning -> true
                SlotSpinPhase.Settling -> column >= settledColumnCount
            }

        fun isColumnStopRequested(phase: SlotSpinPhase, settledColumnCount: Int, column: Int): Boolean =
            phase == SlotSpinPhase.Settling && settledColumnCount > column

        fun columnSymbols(
            grid: List<List<SlotSymbol?>>,
            column: Int,
        ): List<SlotSymbol?> = List(3) { row -> grid.getOrNull(row)?.getOrNull(column) }

        /** Returns the (row, col) cells that form any winning line (3 rows + 2 diagonals). */
        fun winningCells(grid: List<List<String>>): Set<Pair<Int, Int>> {
            if (grid.size < 3 || grid.any { it.size < 3 }) return emptySet()
            val lines = listOf(
                listOf(0 to 0, 0 to 1, 0 to 2),
                listOf(1 to 0, 1 to 1, 1 to 2),
                listOf(2 to 0, 2 to 1, 2 to 2),
                listOf(0 to 0, 1 to 1, 2 to 2),
                listOf(0 to 2, 1 to 1, 2 to 0),
            )
            val cells = mutableSetOf<Pair<Int, Int>>()
            for (line in lines) {
                val first = grid[line[0].first][line[0].second]
                if (first.isNotBlank() && line.all { grid[it.first][it.second] == first }) {
                    cells += line
                }
            }
            return cells
        }
    }
}
