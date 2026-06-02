package com.techmomentum.wc2026.data.dataconnect

import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.Rarity
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.dataconnect.DataConnectRuntime
import com.techmomentum.wc2026.dataconnect.generated.DefaultConnector
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Catalog reads via Firebase SQL Connect **generated Kotlin SDK**.
 */
@Singleton
class SqlConnectCatalogDataSource @Inject constructor() {

    private val connector get() = DefaultConnector.instance

    fun isAvailable(): Boolean = DataConnectRuntime.isEnabled()

    suspend fun loadTeams(): List<Team> {
        val data = connector.listTeams.execute()
        return data.teams.map { row ->
            Team(
                teamId = row.teamId,
                countryName = row.countryName,
                group = row.group,
                teamCode = row.teamCode,
                flagEmoji = row.flagEmoji,
                customEmblemUrl = row.customEmblemUrl,
                primaryColor = row.primaryColor,
                secondaryColor = row.secondaryColor,
                isActive = true,
            )
        }
    }

    suspend fun loadPlayers(): List<Player> {
        val teams = loadTeams()
        return teams.flatMap { team -> loadPlayersForTeam(team.teamId, team.countryName, team.group) }
    }

    suspend fun loadPlayersForTeam(teamId: String, countryName: String, group: String): List<Player> {
        val data = connector.listPlayersByTeam.execute(teamId)
        return data.players.map { row ->
            Player(
                playerId = row.playerId,
                teamId = row.teamId.ifBlank { teamId },
                countryName = countryName,
                group = group,
                shirtNumber = row.shirtNumber,
                playerName = row.playerName,
                position = row.position,
                rarity = Rarity.from(row.rarity),
                imageUrl = row.imageUrl,
                isActive = true,
            )
        }
    }

    suspend fun loadStickers(): List<Sticker> {
        val teams = loadTeams()
        return teams.flatMap { team -> loadStickersForTeam(team.teamId, team.countryName, team.group) }
    }

    suspend fun loadStickersForTeam(teamId: String, countryName: String, group: String): List<Sticker> {
        val data = connector.listStickersByTeam.execute(teamId)
        return data.stickers.map { row ->
            Sticker(
                stickerId = row.stickerId,
                stickerNumber = row.stickerNumber,
                playerId = row.playerId,
                teamId = row.teamId.ifBlank { teamId },
                countryName = row.countryName.ifBlank { countryName },
                group = row.group.ifBlank { group },
                rarity = Rarity.from(row.rarity),
                imageUrl = row.imageUrl,
                isActive = true,
            )
        }
    }
}
