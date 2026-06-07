package com.techmomentum.wc2026.data.session

import android.content.Context
import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.data.model.UserSticker
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestSessionStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasSavedSession(): Boolean = prefs.getBoolean(KEY_HAS_SESSION, false)

    fun loadProfile(): UserProfile? {
        if (!hasSavedSession()) return null
        return UserProfile(
            uid = prefs.getString(KEY_UID, "guest") ?: "guest",
            email = prefs.getString(KEY_EMAIL, "guest@demo.local") ?: "guest@demo.local",
            displayName = prefs.getString(KEY_DISPLAY_NAME, "Guest Collector") ?: "Guest Collector",
            unopenedPacks = prefs.getInt(KEY_UNOPENED_PACKS, 0),
            albumUniqueCount = prefs.getInt(KEY_ALBUM_UNIQUE, 0),
            totalStickerCount = prefs.getInt(KEY_TOTAL_STICKERS, 0),
            lastDailyPackClaimDate = prefs.getString(KEY_LAST_DAILY, "") ?: "",
            rewardedAdPackClaimDate = prefs.getString(KEY_REWARDED_AD_PACK, "") ?: "",
            lastLoginPackGrantedAtEpochMs = prefs.getLong(KEY_LAST_LOGIN_PACK, 0L),
            lastRewardedAdStickerAtEpochMs = prefs.getLong(KEY_LAST_AD_STICKER, 0L),
            slotSpinsRemaining = prefs.getInt(KEY_SLOT_SPINS, 0),
            slotSpinsDate = prefs.getString(KEY_SLOT_SPINS_DATE, "") ?: "",
            slotRewardDate = prefs.getString(KEY_SLOT_REWARD_DATE, "") ?: "",
            slotRewardPacksWonToday = prefs.getInt(KEY_SLOT_PACKS_WON, 0),
            lastRewardedSlotSpinAtEpochMs = prefs.getLong(KEY_LAST_SLOT_SPIN_AD, 0L),
        )
    }

    fun loadStickers(): Map<String, UserSticker> {
        val raw = prefs.getString(KEY_STICKERS_JSON, null) ?: return emptyMap()
        return runCatching {
            val arr = JSONArray(raw)
            buildMap {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.getString("stickerId")
                    put(
                        id,
                        UserSticker(
                            stickerId = id,
                            playerId = obj.optString("playerId", ""),
                            teamId = obj.optString("teamId", ""),
                            count = obj.optInt("count", 1),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyMap())
    }

    fun save(profile: UserProfile, stickers: Map<String, UserSticker>) {
        val stickerJson = JSONArray().apply {
            stickers.values.forEach { sticker ->
                put(
                    JSONObject().apply {
                        put("stickerId", sticker.stickerId)
                        put("playerId", sticker.playerId)
                        put("teamId", sticker.teamId)
                        put("count", sticker.count)
                    },
                )
            }
        }
        prefs.edit()
            .putBoolean(KEY_HAS_SESSION, true)
            .putString(KEY_UID, profile.uid)
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_DISPLAY_NAME, profile.displayName)
            .putInt(KEY_UNOPENED_PACKS, profile.unopenedPacks)
            .putInt(KEY_ALBUM_UNIQUE, profile.albumUniqueCount)
            .putInt(KEY_TOTAL_STICKERS, profile.totalStickerCount)
            .putString(KEY_LAST_DAILY, profile.lastDailyPackClaimDate)
            .putString(KEY_REWARDED_AD_PACK, profile.rewardedAdPackClaimDate)
            .putLong(KEY_LAST_LOGIN_PACK, profile.lastLoginPackGrantedAtEpochMs)
            .putLong(KEY_LAST_AD_STICKER, profile.lastRewardedAdStickerAtEpochMs)
            .putInt(KEY_SLOT_SPINS, profile.slotSpinsRemaining)
            .putString(KEY_SLOT_SPINS_DATE, profile.slotSpinsDate)
            .putString(KEY_SLOT_REWARD_DATE, profile.slotRewardDate)
            .putInt(KEY_SLOT_PACKS_WON, profile.slotRewardPacksWonToday)
            .putLong(KEY_LAST_SLOT_SPIN_AD, profile.lastRewardedSlotSpinAtEpochMs)
            .putString(KEY_STICKERS_JSON, stickerJson.toString())
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "guest_session"
        private const val KEY_HAS_SESSION = "has_session"
        private const val KEY_UID = "uid"
        private const val KEY_EMAIL = "email"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_UNOPENED_PACKS = "unopened_packs"
        private const val KEY_ALBUM_UNIQUE = "album_unique"
        private const val KEY_TOTAL_STICKERS = "total_stickers"
        private const val KEY_LAST_DAILY = "last_daily"
        private const val KEY_REWARDED_AD_PACK = "rewarded_ad_pack"
        private const val KEY_LAST_LOGIN_PACK = "last_login_pack"
        private const val KEY_LAST_AD_STICKER = "last_ad_sticker"
        private const val KEY_SLOT_SPINS = "slot_spins"
        private const val KEY_SLOT_SPINS_DATE = "slot_spins_date"
        private const val KEY_SLOT_REWARD_DATE = "slot_reward_date"
        private const val KEY_SLOT_PACKS_WON = "slot_packs_won"
        private const val KEY_LAST_SLOT_SPIN_AD = "last_slot_spin_ad"
        private const val KEY_STICKERS_JSON = "stickers_json"
    }
}
