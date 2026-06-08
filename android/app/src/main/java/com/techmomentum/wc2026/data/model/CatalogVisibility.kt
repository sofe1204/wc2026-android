package com.techmomentum.wc2026.data.model

/** Firebase Storage paths for in-app collectible art (Grok player edits + uploaded emblems). */
const val FIREBASE_PLAYER_STICKER_PATH = "stickers/players/"
const val FIREBASE_EMBLEM_PATH = "emblems/"

fun Player.isVisibleInCatalog(): Boolean =
    isActive && imageUrl.contains(FIREBASE_PLAYER_STICKER_PATH, ignoreCase = true)

fun Sticker.isVisibleInCatalog(): Boolean =
    isActive && when {
        isTeamEmblem() -> imageUrl.contains(FIREBASE_EMBLEM_PATH, ignoreCase = true)
        else -> imageUrl.contains(FIREBASE_PLAYER_STICKER_PATH, ignoreCase = true)
    }
