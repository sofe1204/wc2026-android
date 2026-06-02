package com.techmomentum.wc2026.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.Rarity
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.data.model.UserSticker

fun DocumentSnapshot.toTeam(): Team = Team(
    teamId = getString("teamId") ?: id,
    countryName = getString("countryName") ?: "",
    group = getString("group") ?: "",
    teamCode = getString("teamCode") ?: "",
    flagEmoji = getString("flagEmoji") ?: "",
    customEmblemUrl = getString("customEmblemUrl") ?: "",
    primaryColor = getString("primaryColor") ?: "",
    secondaryColor = getString("secondaryColor") ?: "",
    isActive = getBoolean("isActive") ?: true,
)

fun DocumentSnapshot.toPlayer(): Player = Player(
    playerId = getString("playerId") ?: id,
    teamId = getString("teamId") ?: "",
    countryName = getString("countryName") ?: "",
    group = getString("group") ?: "",
    shirtNumber = getLong("shirtNumber")?.toInt() ?: 0,
    playerName = getString("playerName") ?: "",
    position = getString("position") ?: "",
    rarity = Rarity.from(getString("rarity")),
    animeStickerPrompt = getString("animeStickerPrompt") ?: "",
    imageUrl = getString("imageUrl") ?: "",
    isActive = getBoolean("isActive") ?: true,
)

fun DocumentSnapshot.toSticker(): Sticker = Sticker(
    stickerId = getString("stickerId") ?: id,
    stickerNumber = getLong("stickerNumber")?.toInt() ?: 0,
    playerId = getString("playerId") ?: "",
    teamId = getString("teamId") ?: "",
    countryName = getString("countryName") ?: "",
    group = getString("group") ?: "",
    rarity = Rarity.from(getString("rarity")),
    imageUrl = getString("imageUrl") ?: "",
    isActive = getBoolean("isActive") ?: true,
)

fun DocumentSnapshot.toUserProfile(): UserProfile = UserProfile(
    uid = getString("uid") ?: id,
    email = getString("email") ?: "",
    displayName = getString("displayName") ?: "",
    unopenedPacks = getLong("unopenedPacks")?.toInt() ?: 0,
    albumUniqueCount = getLong("albumUniqueCount")?.toInt() ?: 0,
    totalStickerCount = getLong("totalStickerCount")?.toInt() ?: 0,
    lastDailyPackClaimDate = getString("lastDailyPackClaimDate") ?: "",
    rewardedAdPackClaimDate = getString("rewardedAdPackClaimDate") ?: "",
    slotSpinsRemaining = getLong("slotSpinsRemaining")?.toInt() ?: 0,
    slotSpinsDate = getString("slotSpinsDate") ?: "",
    slotRewardDate = getString("slotRewardDate") ?: "",
    slotRewardPacksWonToday = getLong("slotRewardPacksWonToday")?.toInt() ?: 0,
)

fun DocumentSnapshot.toUserSticker(): UserSticker = UserSticker(
    stickerId = getString("stickerId") ?: id,
    playerId = getString("playerId") ?: "",
    teamId = getString("teamId") ?: "",
    count = getLong("count")?.toInt() ?: 0,
)
