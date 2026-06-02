package com.techmomentum.wc2026.data.session

import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.data.model.UserSticker
import com.techmomentum.wc2026.utils.DateUtils
import com.techmomentum.wc2026.utils.GameConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** In-memory session for guest/demo mode (no Firebase required). */
@Singleton
class AppSession @Inject constructor() {
    private val _isGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    private val _guestProfile = MutableStateFlow(defaultGuestProfile())
    val guestProfile: StateFlow<UserProfile> = _guestProfile.asStateFlow()

    private val _guestStickers = MutableStateFlow<Map<String, UserSticker>>(emptyMap())
    val guestStickers: StateFlow<Map<String, UserSticker>> = _guestStickers.asStateFlow()

    fun enterGuestMode() {
        _isGuest.value = true
        _guestProfile.value = defaultGuestProfile()
        _guestStickers.value = emptyMap()
    }

    fun exitGuestMode() {
        _isGuest.value = false
        _guestProfile.value = defaultGuestProfile()
        _guestStickers.value = emptyMap()
    }

    fun isActive(): Boolean = _isGuest.value

    fun updateGuestProfile(transform: (UserProfile) -> UserProfile) {
        _guestProfile.value = transform(_guestProfile.value)
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
    }

    private fun defaultGuestProfile() = UserProfile(
        uid = "guest",
        email = "guest@demo.local",
        displayName = "Guest Collector",
        unopenedPacks = GameConstants.SIGNUP_FREE_PACKS,
        albumUniqueCount = 0,
        totalStickerCount = 0,
        slotSpinsRemaining = GameConstants.DAILY_FREE_SLOT_SPINS,
        slotSpinsDate = DateUtils.todayUtc(),
        slotRewardDate = DateUtils.todayUtc(),
    )
}
