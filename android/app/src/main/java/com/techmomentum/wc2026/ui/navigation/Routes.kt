package com.techmomentum.wc2026.ui.navigation

object Routes {
    const val LOADING = "loading"
    const val AUTH = "auth"
    const val HOME = "home"
    const val ALBUM = "album"
    const val SLOT = "slot"
    const val PROFILE = "profile"
    const val LEADERBOARD = "leaderboard"
    const val VERIFY_EMAIL = "verify_email"
    const val COMPLETE_PROFILE = "complete_profile"
    const val SETTINGS = "settings"
    const val TEAM = "team/{teamId}"
    const val STICKER = "sticker/{stickerId}"
    const val PACK_OPEN = "pack_open"

    val bottomBarDestinations = setOf(HOME, ALBUM, LEADERBOARD, SLOT, PROFILE)

    fun team(teamId: String) = "team/$teamId"
    fun sticker(stickerId: String) = "sticker/$stickerId"
}
