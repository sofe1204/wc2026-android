package com.techmomentum.wc2026.data.demo

import com.techmomentum.wc2026.data.model.CallableResult
import com.techmomentum.wc2026.data.model.PackOpenResult
import com.techmomentum.wc2026.data.model.Rarity
import com.techmomentum.wc2026.data.model.SlotResult
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.data.seed.SeedJsonParser
import com.techmomentum.wc2026.data.session.AppSession
import com.techmomentum.wc2026.utils.DateUtils
import com.techmomentum.wc2026.utils.GameConstants
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DemoRewardsEngine @Inject constructor(
    private val appSession: AppSession,
    private val seedJsonParser: SeedJsonParser,
) {
    private var stickersCache: List<Sticker>? = null
    private var slotSymbolIdsCache: List<String>? = null

    suspend fun ensureUserProfile(): CallableResult {
        appSession.enterGuestMode()
        return CallableResult(success = true, message = "Guest profile ready (offline demo).")
    }

    suspend fun openStickerPack(): PackOpenResult {
        val profile = appSession.guestProfile.value
        if (profile.unopenedPacks <= 0) {
            return PackOpenResult(message = "No unopened packs available.")
        }
        val stickers = loadStickers()
        val picked = (1..GameConstants.STICKERS_PER_PACK).mapNotNull {
            pickByRarity(stickers)?.stickerId
        }
        val meta = stickers.associate { it.stickerId to (it.playerId to it.teamId) }
        appSession.addGuestStickers(picked, meta)
        appSession.updateGuestProfile {
            it.copy(unopenedPacks = it.unopenedPacks - 1)
        }
        return PackOpenResult(
            stickerIds = picked,
            unopenedPacks = appSession.guestProfile.value.unopenedPacks,
            message = "Opened ${picked.size} stickers (demo mode).",
        )
    }

    suspend fun claimDailyPacks(): CallableResult {
        val today = DateUtils.todayUtc()
        val profile = appSession.guestProfile.value
        if (profile.lastDailyPackClaimDate == today) {
            return CallableResult(success = false, message = "Daily packs already claimed today.")
        }
        appSession.updateGuestProfile {
            it.copy(
                unopenedPacks = it.unopenedPacks + GameConstants.DAILY_FREE_PACKS,
                lastDailyPackClaimDate = today,
            )
        }
        return CallableResult(
            success = true,
            message = "Added ${GameConstants.DAILY_FREE_PACKS} daily packs (demo).",
            unopenedPacks = appSession.guestProfile.value.unopenedPacks,
        )
    }

    suspend fun claimRewardedAdPack(): CallableResult {
        val today = DateUtils.todayUtc()
        val profile = appSession.guestProfile.value
        if (profile.rewardedAdPackClaimDate == today) {
            return CallableResult(success = false, message = "Rewarded ad pack already claimed today.")
        }
        appSession.updateGuestProfile {
            it.copy(
                unopenedPacks = it.unopenedPacks + 1,
                rewardedAdPackClaimDate = today,
            )
        }
        return CallableResult(
            success = true,
            message = "Added 1 pack (demo ad reward).",
            unopenedPacks = appSession.guestProfile.value.unopenedPacks,
        )
    }

    suspend fun spinSlotMachine(): SlotResult {
        resetDailyIfNeeded()
        val profile = appSession.guestProfile.value
        if (profile.slotSpinsRemaining <= 0) {
            return SlotResult(message = "No slot spins remaining.")
        }
        val symbolIds = loadSlotSymbolIds()
        val grid = List(3) { List(3) { symbolIds.random() } }
        val isWin = checkWin(grid)
        var rewardGranted = false
        var packsWon = profile.slotRewardPacksWonToday
        var packs = profile.unopenedPacks
        var message = if (isWin) "" else "No match — try again!"
        if (isWin) {
            if (packsWon < GameConstants.DAILY_SLOT_PACK_REWARD_CAP) {
                rewardGranted = true
                packsWon += 1
                packs += 1
                message = "You won a sticker pack!"
            } else {
                message = "Daily slot reward limit reached."
            }
        }
        appSession.updateGuestProfile {
            it.copy(
                slotSpinsRemaining = it.slotSpinsRemaining - 1,
                slotRewardPacksWonToday = packsWon,
                unopenedPacks = packs,
            )
        }
        return SlotResult(
            grid = grid,
            isWin = isWin,
            rewardGranted = rewardGranted,
            spinsRemaining = appSession.guestProfile.value.slotSpinsRemaining,
            packsWonToday = packsWon,
            message = message,
        )
    }

    fun claimRewardedSlotSpins(): CallableResult {
        appSession.updateGuestProfile {
            it.copy(slotSpinsRemaining = it.slotSpinsRemaining + GameConstants.REWARDED_SLOT_SPINS)
        }
        return CallableResult(
            success = true,
            message = "Added ${GameConstants.REWARDED_SLOT_SPINS} spins (demo).",
        )
    }

    private fun resetDailyIfNeeded() {
        val today = DateUtils.todayUtc()
        val p = appSession.guestProfile.value
        if (p.slotSpinsDate != today || p.slotRewardDate != today) {
            appSession.updateGuestProfile {
                it.copy(
                    slotSpinsRemaining = if (it.slotSpinsDate != today) GameConstants.DAILY_FREE_SLOT_SPINS else it.slotSpinsRemaining,
                    slotSpinsDate = today,
                    slotRewardPacksWonToday = if (it.slotRewardDate != today) 0 else it.slotRewardPacksWonToday,
                    slotRewardDate = today,
                )
            }
        }
    }

    private suspend fun loadStickers(): List<Sticker> {
        stickersCache?.let { return it }
        return seedJsonParser.loadStickers().also { stickersCache = it }
    }

    private suspend fun loadSlotSymbolIds(): List<String> {
        slotSymbolIdsCache?.let { return it }
        val ids = seedJsonParser.loadSlotSymbols()
            .filter { it.isActive }
            .map { it.symbolId }
        if (ids.isNotEmpty()) return ids.also { slotSymbolIdsCache = it }
        return seedJsonParser.loadPlayers().map { it.playerId }.also { slotSymbolIdsCache = it }
    }

    private fun pickByRarity(stickers: List<Sticker>): Sticker? {
        val rarity = rollRarity()
        val pool = stickers.filter { it.rarity == rarity && it.isActive }
        val chosen = pool.randomOrNull()
            ?: stickers.filter { it.isActive }.randomOrNull()
        return chosen
    }

    private fun rollRarity(): Rarity {
        val roll = Random.nextInt(100)
        return when {
            roll < GameConstants.RARITY_LEGENDARY_WEIGHT -> Rarity.LEGENDARY
            roll < GameConstants.RARITY_LEGENDARY_WEIGHT + GameConstants.RARITY_EPIC_WEIGHT -> Rarity.EPIC
            roll < GameConstants.RARITY_LEGENDARY_WEIGHT + GameConstants.RARITY_EPIC_WEIGHT + GameConstants.RARITY_RARE_WEIGHT -> Rarity.RARE
            else -> Rarity.COMMON
        }
    }

    private fun checkWin(grid: List<List<String>>): Boolean {
        val lines = listOf(
            listOf(grid[0][0], grid[0][1], grid[0][2]),
            listOf(grid[1][0], grid[1][1], grid[1][2]),
            listOf(grid[2][0], grid[2][1], grid[2][2]),
            listOf(grid[0][0], grid[1][1], grid[2][2]),
            listOf(grid[0][2], grid[1][1], grid[2][0]),
        )
        return lines.any { lineWins(it) }
    }

    private fun lineWins(line: List<String>): Boolean {
        if (line.any { it.isBlank() }) return false
        val trophy = GameConstants.TROPHY_SYMBOL_ID
        val nonWild = line.filter { it != trophy }
        if (nonWild.isEmpty()) return true
        val target = nonWild.first()
        return nonWild.all { it == target } && line.all { it == target || it == trophy }
    }
}
