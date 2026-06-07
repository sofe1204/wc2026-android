package com.techmomentum.wc2026.data.demo

import com.techmomentum.wc2026.data.model.CallableResult
import com.techmomentum.wc2026.data.model.PackOpenResult
import com.techmomentum.wc2026.data.model.Rarity
import com.techmomentum.wc2026.data.model.SlotResult
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.data.repository.CatalogRepository
import com.techmomentum.wc2026.data.session.AppSession
import com.techmomentum.wc2026.utils.DateUtils
import com.techmomentum.wc2026.utils.GameConstants
import com.techmomentum.wc2026.utils.RewardEligibility
import com.techmomentum.wc2026.utils.SwapDeckUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DemoRewardsEngine @Inject constructor(
    private val appSession: AppSession,
    private val catalogRepository: CatalogRepository,
) {
    private var stickersCache: List<Sticker>? = null
    private var slotSymbolIdsCache: List<String>? = null

    suspend fun ensureUserProfile(): CallableResult {
        val now = System.currentTimeMillis()
        var message = "Guest profile ready."
        appSession.updateGuestProfile { profile ->
            if (RewardEligibility.isLoginPackEligible(profile.lastLoginPackGrantedAtEpochMs, now)) {
                message = "Welcome back! +${GameConstants.LOGIN_REWARD_PACKS} pack " +
                    "(${GameConstants.STICKERS_PER_PACK} stickers each)."
                profile.copy(
                    unopenedPacks = profile.unopenedPacks + GameConstants.LOGIN_REWARD_PACKS,
                    lastLoginPackGrantedAtEpochMs = now,
                )
            } else {
                profile
            }
        }
        return CallableResult(
            success = true,
            message = message,
            unopenedPacks = appSession.guestProfile.value.unopenedPacks,
        )
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

    suspend fun claimRewardedAdStickers(): CallableResult {
        val profile = appSession.guestProfile.value
        val now = System.currentTimeMillis()
        if (!RewardEligibility.isAdStickerAvailable(profile.lastRewardedAdStickerAtEpochMs, now)) {
            val waitMin = RewardEligibility.adStickerCooldownMinutesRemaining(
                profile.lastRewardedAdStickerAtEpochMs,
                now,
            )
            return CallableResult(success = false, message = "Wait $waitMin min for next ad reward.")
        }
        val stickers = loadStickers()
        val picked = (1..GameConstants.REWARDED_AD_STICKERS).mapNotNull {
            pickByRarity(stickers)?.stickerId
        }
        if (picked.isEmpty()) {
            return CallableResult(success = false, message = "No stickers available.")
        }
        val meta = stickers.associate { it.stickerId to (it.playerId to it.teamId) }
        appSession.addGuestStickers(picked, meta)
        appSession.updateGuestProfile {
            it.copy(lastRewardedAdStickerAtEpochMs = now)
        }
        return CallableResult(
            success = true,
            message = "You earned ${picked.size} stickers!",
            stickerIds = picked,
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

    suspend fun redeemSwapDeck(): CallableResult {
        val stickers = appSession.guestStickers.value
        val total = SwapDeckUtils.totalSwapDuplicates(stickers)
        if (total < GameConstants.SWAP_DUPLICATES_FOR_PACK) {
            return CallableResult(
                success = false,
                message = "Need ${GameConstants.SWAP_DUPLICATES_FOR_PACK} duplicates " +
                    "in swap deck ($total/${GameConstants.SWAP_DUPLICATES_FOR_PACK}).",
            )
        }
        if (!appSession.consumeGuestDuplicates(GameConstants.SWAP_DUPLICATES_FOR_PACK)) {
            return CallableResult(success = false, message = "Could not consume duplicates.")
        }
        appSession.updateGuestProfile {
            it.copy(unopenedPacks = it.unopenedPacks + 1)
        }
        return CallableResult(
            success = true,
            message = "Swapped ${GameConstants.SWAP_DUPLICATES_FOR_PACK} duplicates for 1 sticker pack!",
            unopenedPacks = appSession.guestProfile.value.unopenedPacks,
        )
    }

    fun claimRewardedSlotSpins(): CallableResult {
        val profile = appSession.guestProfile.value
        val now = System.currentTimeMillis()
        if (!RewardEligibility.isSlotSpinAdAvailable(profile.lastRewardedSlotSpinAtEpochMs, now)) {
            val waitMin = RewardEligibility.slotSpinAdCooldownMinutesRemaining(
                profile.lastRewardedSlotSpinAtEpochMs,
                now,
            )
            return CallableResult(success = false, message = "Wait $waitMin min for next spin ad reward.")
        }
        appSession.updateGuestProfile {
            it.copy(
                slotSpinsRemaining = it.slotSpinsRemaining + GameConstants.REWARDED_SLOT_SPINS,
                lastRewardedSlotSpinAtEpochMs = now,
            )
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
        return catalogRepository.getStickers().also { stickersCache = it }
    }

    private suspend fun loadSlotSymbolIds(): List<String> {
        slotSymbolIdsCache?.let { return it }
        val ids = catalogRepository.getSlotSymbols()
            .filter { it.isActive }
            .map { it.symbolId }
        if (ids.isNotEmpty()) return ids.also { slotSymbolIdsCache = it }
        return catalogRepository.getPlayers().map { it.playerId }.also { slotSymbolIdsCache = it }
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
        val diagonals = listOf(
            listOf(grid[0][0], grid[1][1], grid[2][2]),
            listOf(grid[0][2], grid[1][1], grid[2][0]),
        )
        return diagonals.any { lineWins(it) }
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
