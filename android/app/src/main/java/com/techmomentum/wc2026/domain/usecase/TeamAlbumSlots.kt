package com.techmomentum.wc2026.domain.usecase

import com.techmomentum.wc2026.data.model.isTeamEmblem

data class TeamAlbumSlots(
    val crestSlot: StickerSlot?,
    val playerSlots: List<StickerSlot>,
) {
    val allSlots: List<StickerSlot> = buildList {
        crestSlot?.let { add(it) }
        addAll(playerSlots)
    }
}

fun List<StickerSlot>.partitionByEmblem(): TeamAlbumSlots {
    val crest = firstOrNull { it.sticker.isTeamEmblem() }
    val players = filter { !it.sticker.isTeamEmblem() }
    return TeamAlbumSlots(crestSlot = crest, playerSlots = players)
}
