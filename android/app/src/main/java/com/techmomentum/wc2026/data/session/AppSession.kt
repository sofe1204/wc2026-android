package com.techmomentum.wc2026.data.session

import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.data.model.UserSticker
import com.techmomentum.wc2026.utils.DateUtils
import com.techmomentum.wc2026.utils.GameConstants
import com.techmomentum.wc2026.utils.SwapDeckUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** In-memory session for guest/demo mode (no Firebase required). */
@Singleton
class AppSession @Inject constructor(
    private val guestSessionStore: GuestSessionStore,
) {
    private val _isGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    private val _guestProfile = MutableStateFlow(defaultGuestProfile())
    val guestProfile: StateFlow<UserProfile> = _guestProfile.asStateFlow()

    private val _guestStickers = MutableStateFlow<Map<String, UserSticker>>(emptyMap())
    val guestStickers: StateFlow<Map<String, UserSticker>> = _guestStickers.asStateFlow()

    fun enterGuestMode() {
        _isGuest.value = true
        val savedProfile = guestSessionStore.loadProfile()
        val savedStickers = guestSessionStore.loadStickers()
        if (savedProfile != null) {
            _guestProfile.value = savedProfile
            _guestStickers.value = savedStickers
        } else {
            _guestProfile.value = defaultGuestProfile()
            _guestStickers.value = emptyMap()
            persistGuestState()
        }
    }

    fun exitGuestMode(clearPersistence: Boolean = false) {
        if (_isGuest.value) {
            persistGuestState()
        }
        _isGuest.value = false
        _guestProfile.value = defaultGuestProfile()
        _guestStickers.value = emptyMap()
        if (clearPersistence) {
            guestSessionStore.clear()
        }
    }

    fun isActive(): Boolean = _isGuest.value

    fun updateGuestProfile(transform: (UserProfile) -> UserProfile) {
        _guestProfile.value = transform(_guestProfile.value)
        persistGuestState()
    }

    fun consumeGuestDuplicates(amount: Int): Boolean {
        var remaining = amount
        if (remaining <= 0) return true
        val current = _guestStickers.value.toMutableMap()
        val order = current.entries.sortedByDescending { SwapDeckUtils.duplicateCount(it.value.count) }
        for ((id, sticker) in order) {
            if (remaining <= 0) break
            val dupes = SwapDeckUtils.duplicateCount(sticker.count)
            if (dupes <= 0) continue
            val take = minOf(dupes, remaining)
            val newCount = sticker.count - take
            if (newCount <= 0) {
                current.remove(id)
            } else {
                current[id] = sticker.copy(count = newCount)
            }
            remaining -= take
        }
        if (remaining > 0) return false
        _guestStickers.value = current
        val unique = current.size
        val total = current.values.sumOf { it.count }
        _guestProfile.value = _guestProfile.value.copy(
            albumUniqueCount = unique,
            totalStickerCount = total,
        )
        persistGuestState()
        return true
    }

    fun addGuestStickers(stickerIds: List<String>, stickerMeta: Map<String, Pair<String, String>>) {
        val current = _guestStickers.value.toMutableMap()
        stickerIds.forEach { id ->
            val meta = stickerMeta[id]
            val existing = current[id]
            if (existing != null) {
                current[id] = existing.copy(count = existing.count + 1)
            } else {
                current[id] = UserSticker(
                    stickerId = id,
                    playerId = meta?.first ?: "",
                    teamId = meta?.second ?: "",
                    count = 1,
                )
            }
        }
        _guestStickers.value = current
        val unique = current.size
        val total = current.values.sumOf { it.count }
        _guestProfile.value = _guestProfile.value.copy(
            albumUniqueCount = unique,
            totalStickerCount = total,
        )
        persistGuestState()
    }

    private fun persistGuestState() {
        if (_isGuest.value) {
            guestSessionStore.save(_guestProfile.value, _guestStickers.value)
        }
    }

    private fun defaultGuestProfile() = UserProfile(
        uid = "guest",
        email = "guest@demo.local",
        displayName = "Guest Collector",
        unopenedPacks = GameConstants.SIGNUP_FREE_PACKS,
        albumUniqueCount = 0,
        totalStickerCount = 0,
        lastLoginPackGrantedAtEpochMs = System.currentTimeMillis(),
        slotSpinsRemaining = GameConstants.DAILY_FREE_SLOT_SPINS,
        slotSpinsDate = DateUtils.todayUtc(),
        slotRewardDate = DateUtils.todayUtc(),
    )
}
