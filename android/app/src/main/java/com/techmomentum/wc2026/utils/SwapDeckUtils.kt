package com.techmomentum.wc2026.utils

import com.techmomentum.wc2026.data.model.UserSticker

object SwapDeckUtils {
    /** Copies beyond the first album slot (count - 1). */
    fun duplicateCount(stickerCount: Int): Int = (stickerCount - 1).coerceAtLeast(0)

    fun totalSwapDuplicates(stickers: Map<String, UserSticker>): Int =
        stickers.values.sumOf { duplicateCount(it.count) }

    fun canRedeemForPack(stickers: Map<String, UserSticker>): Boolean =
        totalSwapDuplicates(stickers) >= GameConstants.SWAP_DUPLICATES_FOR_PACK
}
