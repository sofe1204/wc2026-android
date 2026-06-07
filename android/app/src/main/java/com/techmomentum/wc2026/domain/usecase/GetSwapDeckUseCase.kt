package com.techmomentum.wc2026.domain.usecase

import com.techmomentum.wc2026.data.model.SwapDeckItem
import com.techmomentum.wc2026.data.repository.CatalogRepository
import com.techmomentum.wc2026.data.repository.UserStickersRepository
import com.techmomentum.wc2026.utils.GameConstants
import com.techmomentum.wc2026.utils.SwapDeckUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class SwapDeckState(
    val items: List<SwapDeckItem> = emptyList(),
    val totalDuplicates: Int = 0,
    val canRedeemPack: Boolean = false,
)

class GetSwapDeckUseCase @Inject constructor(
    private val userStickersRepository: UserStickersRepository,
    private val catalogRepository: CatalogRepository,
) {
    fun observe(): Flow<SwapDeckState> = flow {
        val stickers = catalogRepository.getStickers()
        val players = catalogRepository.getPlayers()
        val stickerById = stickers.associateBy { it.stickerId }
        val playerById = players.associateBy { it.playerId }
        userStickersRepository.observeUserStickers().collect { owned ->
            val items = owned.mapNotNull { (id, userSticker) ->
                val dupes = SwapDeckUtils.duplicateCount(userSticker.count)
                if (dupes <= 0) return@mapNotNull null
                val meta = stickerById[id]
                val player = meta?.playerId?.let { playerById[it] }
                SwapDeckItem(
                    stickerId = id,
                    label = player?.playerName?.ifBlank { null }
                        ?: meta?.countryName?.ifBlank { null }
                        ?: id,
                    imageUrl = meta?.imageUrl.orEmpty(),
                    teamId = userSticker.teamId,
                    duplicateCount = dupes,
                    rarity = meta?.rarity ?: com.techmomentum.wc2026.data.model.Rarity.COMMON,
                )
            }.sortedByDescending { it.duplicateCount }
            val total = SwapDeckUtils.totalSwapDuplicates(owned)
            emit(
                SwapDeckState(
                    items = items,
                    totalDuplicates = total,
                    canRedeemPack = total >= GameConstants.SWAP_DUPLICATES_FOR_PACK,
                ),
            )
        }
    }
}
