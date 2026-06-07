package com.techmomentum.wc2026.utils

/** Game balance — keep in sync with [project.config.json] (run `./scripts/sync_project.sh`). */
object GameConstants {
    const val STICKERS_PER_PACK = 5
    const val SIGNUP_FREE_PACKS = 2
    const val LOGIN_REWARD_PACKS = 1
    const val LOGIN_REWARD_INTERVAL_HOURS = 24
    const val LOGIN_REWARD_INTERVAL_MS = LOGIN_REWARD_INTERVAL_HOURS * 60L * 60L * 1000L
    const val REWARDED_AD_STICKERS = 2
    const val REWARDED_AD_COOLDOWN_MINUTES = 60
    const val REWARDED_AD_COOLDOWN_MS = REWARDED_AD_COOLDOWN_MINUTES * 60L * 1000L
    const val DAILY_FREE_PACKS = 0
    const val DAILY_REWARDED_PACK_LIMIT = 0
    const val DAILY_FREE_SLOT_SPINS = 10
    const val REWARDED_SLOT_SPINS = 5
    const val REWARDED_SLOT_SPIN_COOLDOWN_MINUTES = 60
    const val REWARDED_SLOT_SPIN_COOLDOWN_MS = REWARDED_SLOT_SPIN_COOLDOWN_MINUTES * 60L * 1000L
    const val DAILY_SLOT_PACK_REWARD_CAP = 5
    const val SWAP_DUPLICATES_FOR_PACK = 5
    const val TOTAL_STICKERS = 1296
    const val PLAYERS_PER_TEAM = 26
    /** Players plus one collectible team crest sticker per squad. */
    const val STICKERS_PER_TEAM = PLAYERS_PER_TEAM + 1
    const val TOTAL_TEAMS = 48
    const val SLOT_SYMBOL_COUNT = 7
    const val TROPHY_SYMBOL_ID = "trophy"

    const val RARITY_COMMON_WEIGHT = 70
    const val RARITY_RARE_WEIGHT = 20
    const val RARITY_EPIC_WEIGHT = 8
    const val RARITY_LEGENDARY_WEIGHT = 2
}
