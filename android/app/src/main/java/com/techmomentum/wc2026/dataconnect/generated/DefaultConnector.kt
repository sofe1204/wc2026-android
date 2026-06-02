@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.techmomentum.wc2026.dataconnect.generated

/**
 * PLACEHOLDER Kotlin connector — replaced when you run:
 * `firebase dataconnect:sdk:generate`
 *
 * Do not edit manually after generation.
 */
object DefaultConnector {
    val instance: DefaultConnector get() = this

    val listTeams: ListTeamsQuery = ListTeamsQuery()
    val listPlayersByTeam: ListPlayersByTeamQuery = ListPlayersByTeamQuery()
    val listStickersByTeam: ListStickersByTeamQuery = ListStickersByTeamQuery()
}

class ListTeamsQuery {
    suspend fun execute(): Data = Data(emptyList())

    data class Data(val teams: List<TeamRow>)
    data class TeamRow(
        val teamId: String = "",
        val countryName: String = "",
        val group: String = "",
        val teamCode: String = "",
        val flagEmoji: String = "",
        val customEmblemUrl: String = "",
        val primaryColor: String = "",
        val secondaryColor: String = "",
    )
}

class ListPlayersByTeamQuery {
    suspend fun execute(teamId: String): Data = Data(emptyList())

    data class Data(val players: List<PlayerRow>)
    data class PlayerRow(
        val playerId: String = "",
        val teamId: String = "",
        val shirtNumber: Int = 0,
        val playerName: String = "",
        val position: String = "",
        val rarity: String = "",
        val imageUrl: String = "",
    )
}

class ListStickersByTeamQuery {
    suspend fun execute(teamId: String): Data = Data(emptyList())

    data class Data(val stickers: List<StickerRow>)
    data class StickerRow(
        val stickerId: String = "",
        val stickerNumber: Int = 0,
        val playerId: String = "",
        val teamId: String = "",
        val countryName: String = "",
        val group: String = "",
        val rarity: String = "",
        val imageUrl: String = "",
    )
}
