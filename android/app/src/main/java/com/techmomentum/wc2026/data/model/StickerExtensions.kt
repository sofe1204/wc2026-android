package com.techmomentum.wc2026.data.model

/** Sticker number reserved for the collectible team crest / emblem. */
const val TEAM_EMBLEM_STICKER_NUMBER = 0

fun Sticker.isTeamEmblem(): Boolean =
    stickerNumber == TEAM_EMBLEM_STICKER_NUMBER && playerId.isBlank()

fun List<Sticker>.emblemStickerForTeam(teamId: String): Sticker? =
    firstOrNull { it.teamId == teamId && it.isTeamEmblem() }
