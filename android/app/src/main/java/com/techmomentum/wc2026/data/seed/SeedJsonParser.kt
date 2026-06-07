package com.techmomentum.wc2026.data.seed

import android.content.Context
import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.PlayerRatings
import com.techmomentum.wc2026.data.model.Rarity
import com.techmomentum.wc2026.data.model.SlotSymbol
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.data.model.Team
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class TeamSeedDto(
    @SerialName("teamId") val teamId: String,
    @SerialName("countryName") val countryName: String,
    @SerialName("group") val group: String,
    @SerialName("teamCode") val teamCode: String,
    @SerialName("flagEmoji") val flagEmoji: String = "",
    @SerialName("customEmblemUrl") val customEmblemUrl: String = "",
    @SerialName("primaryColor") val primaryColor: String = "",
    @SerialName("secondaryColor") val secondaryColor: String = "",
    @SerialName("isActive") val isActive: Boolean = true,
)

@Serializable
private data class PlayerRatingsSeedDto(
    @SerialName("overall") val overall: Int = 0,
    @SerialName("pace") val pace: Int = 0,
    @SerialName("shooting") val shooting: Int = 0,
    @SerialName("passing") val passing: Int = 0,
    @SerialName("dribbling") val dribbling: Int = 0,
    @SerialName("defending") val defending: Int = 0,
    @SerialName("physical") val physical: Int = 0,
    @SerialName("diving") val diving: Int = 0,
    @SerialName("handling") val handling: Int = 0,
    @SerialName("kicking") val kicking: Int = 0,
    @SerialName("reflexes") val reflexes: Int = 0,
    @SerialName("speed") val speed: Int = 0,
    @SerialName("positioning") val positioning: Int = 0,
)

@Serializable
private data class PlayerSeedDto(
    @SerialName("playerId") val playerId: String,
    @SerialName("teamId") val teamId: String,
    @SerialName("countryName") val countryName: String,
    @SerialName("group") val group: String,
    @SerialName("shirtNumber") val shirtNumber: Int,
    @SerialName("playerName") val playerName: String,
    @SerialName("position") val position: String,
    @SerialName("rarity") val rarity: String,
    @SerialName("animeStickerPrompt") val animeStickerPrompt: String = "",
    @SerialName("imageUrl") val imageUrl: String = "",
    @SerialName("clubName") val clubName: String = "",
    @SerialName("clubLeague") val clubLeague: String = "",
    @SerialName("clubLogoUrl") val clubLogoUrl: String = "",
    @SerialName("ratings") val ratings: PlayerRatingsSeedDto = PlayerRatingsSeedDto(),
    @SerialName("ratingsComplete") val ratingsComplete: Boolean = false,
    @SerialName("isActive") val isActive: Boolean = true,
)

@Serializable
private data class SlotSymbolSeedDto(
    @SerialName("symbolId") val symbolId: String,
    @SerialName("playerId") val playerId: String = "",
    @SerialName("label") val label: String = "",
    @SerialName("type") val type: String = "player",
    @SerialName("imageUrl") val imageUrl: String = "",
    @SerialName("isActive") val isActive: Boolean = true,
)

@Serializable
private data class StickerSeedDto(
    @SerialName("stickerId") val stickerId: String,
    @SerialName("stickerNumber") val stickerNumber: Int,
    @SerialName("playerId") val playerId: String,
    @SerialName("teamId") val teamId: String,
    @SerialName("countryName") val countryName: String = "",
    @SerialName("group") val group: String = "",
    @SerialName("rarity") val rarity: String,
    @SerialName("imageUrl") val imageUrl: String = "",
    @SerialName("isActive") val isActive: Boolean = true,
)

@Singleton
class SeedJsonParser @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadTeams(): List<Team> {
        val dtos = parseAsset<List<TeamSeedDto>>("seed/teams_seed.json")
        return dtos.map { dto ->
            Team(
                teamId = dto.teamId,
                countryName = dto.countryName,
                group = dto.group,
                teamCode = dto.teamCode,
                flagEmoji = dto.flagEmoji,
                customEmblemUrl = dto.customEmblemUrl,
                primaryColor = dto.primaryColor,
                secondaryColor = dto.secondaryColor,
                isActive = dto.isActive,
            )
        }
    }

    fun loadPlayers(): List<Player> {
        val dtos = parseAsset<List<PlayerSeedDto>>("seed/players_seed.json")
        return dtos.map { dto ->
            Player(
                playerId = dto.playerId,
                teamId = dto.teamId,
                countryName = dto.countryName,
                group = dto.group,
                shirtNumber = dto.shirtNumber,
                playerName = dto.playerName,
                position = dto.position,
                rarity = Rarity.from(dto.rarity),
                animeStickerPrompt = dto.animeStickerPrompt,
                imageUrl = dto.imageUrl,
                clubName = dto.clubName,
                clubLeague = dto.clubLeague,
                clubLogoUrl = dto.clubLogoUrl,
                ratings = PlayerRatings(
                    overall = dto.ratings.overall,
                    pace = dto.ratings.pace,
                    shooting = dto.ratings.shooting,
                    passing = dto.ratings.passing,
                    dribbling = dto.ratings.dribbling,
                    defending = dto.ratings.defending,
                    physical = dto.ratings.physical,
                    diving = dto.ratings.diving,
                    handling = dto.ratings.handling,
                    kicking = dto.ratings.kicking,
                    reflexes = dto.ratings.reflexes,
                    speed = dto.ratings.speed,
                    positioning = dto.ratings.positioning,
                ),
                ratingsComplete = dto.ratingsComplete,
                isActive = dto.isActive,
            )
        }
    }

    fun loadSlotSymbols(): List<SlotSymbol> {
        val dtos = parseAsset<List<SlotSymbolSeedDto>>("seed/slot_symbols_seed.json")
        return dtos.map { dto ->
            SlotSymbol(
                symbolId = dto.symbolId,
                playerId = dto.playerId,
                label = dto.label,
                type = dto.type,
                imageUrl = dto.imageUrl,
                isActive = dto.isActive,
            )
        }
    }

    fun loadStickers(): List<Sticker> {
        val dtos = parseAsset<List<StickerSeedDto>>("seed/stickers_seed.json")
        return dtos.map { dto ->
            Sticker(
                stickerId = dto.stickerId,
                stickerNumber = dto.stickerNumber,
                playerId = dto.playerId,
                teamId = dto.teamId,
                countryName = dto.countryName,
                group = dto.group,
                rarity = Rarity.from(dto.rarity),
                imageUrl = dto.imageUrl,
                isActive = dto.isActive,
            )
        }
    }

    private inline fun <reified T> parseAsset(path: String): T {
        val text = context.assets.open(path).bufferedReader().use { reader -> reader.readText() }
        return json.decodeFromString(text)
    }
}
