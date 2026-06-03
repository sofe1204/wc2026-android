package com.techmomentum.wc2026.domain.usecase

import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.data.model.UserSticker
import com.techmomentum.wc2026.data.repository.CatalogRepository
import com.techmomentum.wc2026.data.repository.UserStickersRepository
import com.techmomentum.wc2026.utils.GameConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class TeamAlbumProgress(
    val team: Team,
    val ownedCount: Int = 0,
    val total: Int = GameConstants.STICKERS_PER_TEAM,
    val percent: Float = 0f,
)

data class StickerSlot(
    val sticker: Sticker,
    val player: Player?,
    val owned: UserSticker?,
)

data class AlbumState(
    val groups: Map<String, List<TeamAlbumProgress>> = emptyMap(),
    val allTeams: List<Team> = emptyList(),
    val stickers: List<Sticker> = emptyList(),
    val players: List<Player> = emptyList(),
    val owned: Map<String, UserSticker> = emptyMap(),
)

class GetAlbumUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val userStickersRepository: UserStickersRepository,
) {
    fun observe(): Flow<AlbumState> = combine(
        userStickersRepository.observeUserStickers(),
    ) { ownedArray ->
        ownedArray[0]
    }.let { ownedFlow ->
        flow {
            val teams = catalogRepository.getTeams()
            val stickers = catalogRepository.getStickers()
            val players = catalogRepository.getPlayers()
            ownedFlow.collect { owned ->
                val progress = teams.map { team ->
                    val teamStickerIds = stickers
                        .filter { it.teamId == team.teamId }
                        .map { it.stickerId }
                        .toSet()
                    val ownedCount = owned.keys.count { it in teamStickerIds }
                    TeamAlbumProgress(
                        team = team,
                        ownedCount = ownedCount,
                        percent = ownedCount.toFloat() / GameConstants.STICKERS_PER_TEAM * 100f,
                    )
                }
                val byGroup = progress.groupBy { it.team.group }.toSortedMap()
                emit(
                    AlbumState(
                        groups = byGroup,
                        allTeams = teams,
                        stickers = stickers,
                        players = players,
                        owned = owned,
                    ),
                )
            }
        }
    }

    suspend fun getTeamAlbum(teamId: String): Pair<Team?, TeamAlbumSlots> {
        val team = catalogRepository.getTeam(teamId)
        val stickers = catalogRepository.getStickers().filter { it.teamId == teamId }
        val players = catalogRepository.getPlayers()
        val owned = userStickersRepository.observeUserStickers().first()
        val slots = stickers.sortedBy { it.stickerNumber }.map { sticker ->
            StickerSlot(
                sticker = sticker,
                player = players.firstOrNull { it.playerId == sticker.playerId },
                owned = owned[sticker.stickerId],
            )
        }
        return team to slots.partitionByEmblem()
    }
}
