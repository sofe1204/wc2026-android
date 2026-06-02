package com.techmomentum.wc2026.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.techmomentum.wc2026.data.dataconnect.SqlConnectCatalogDataSource
import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.data.remote.toPlayer
import com.techmomentum.wc2026.data.remote.toSticker
import com.techmomentum.wc2026.data.remote.toTeam
import com.techmomentum.wc2026.data.seed.SeedJsonParser
import com.techmomentum.wc2026.data.session.AppSession
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val seedJsonParser: SeedJsonParser,
    private val appSession: AppSession,
    private val sqlConnectCatalog: SqlConnectCatalogDataSource,
) {
    private var teamsCache: List<Team>? = null
    private var playersCache: List<Player>? = null
    private var stickersCache: List<Sticker>? = null

    suspend fun getTeams(): List<Team> {
        teamsCache?.let { return it }
        val teams = resolveCatalog(
            sqlLoad = { sqlConnectCatalog.loadTeams() },
            firestoreLoad = { loadFromFirestoreTeams() },
            seedLoad = { seedJsonParser.loadTeams() },
        )
        teamsCache = teams
        return teams
    }

    suspend fun getPlayers(): List<Player> {
        playersCache?.let { return it }
        val players = resolveCatalog(
            sqlLoad = { sqlConnectCatalog.loadPlayers() },
            firestoreLoad = { loadFromFirestorePlayers() },
            seedLoad = { seedJsonParser.loadPlayers() },
        )
        playersCache = players
        return players
    }

    suspend fun getStickers(): List<Sticker> {
        stickersCache?.let { return it }
        val stickers = resolveCatalog(
            sqlLoad = { sqlConnectCatalog.loadStickers() },
            firestoreLoad = { loadFromFirestoreStickers() },
            seedLoad = { seedJsonParser.loadStickers() },
        )
        stickersCache = stickers
        return stickers
    }

    fun clearCache() {
        teamsCache = null
        playersCache = null
        stickersCache = null
    }

    suspend fun getTeam(teamId: String): Team? = getTeams().firstOrNull { it.teamId == teamId }

    suspend fun getSticker(stickerId: String): Sticker? = getStickers().firstOrNull { it.stickerId == stickerId }

    suspend fun getPlayer(playerId: String): Player? = getPlayers().firstOrNull { it.playerId == playerId }

    /**
     * Priority: SQL Connect (Kotlin SDK) → Firestore → local seed JSON (guest/offline).
     */
    private suspend fun <T> resolveCatalog(
        sqlLoad: suspend () -> List<T>,
        firestoreLoad: suspend () -> Result<List<T>>,
        seedLoad: suspend () -> List<T>,
    ): List<T> {
        if (appSession.isActive()) return seedLoad()
        if (sqlConnectCatalog.isAvailable()) {
            runCatching { sqlLoad() }.getOrNull()?.takeIf { it.isNotEmpty() }?.let { return it }
        }
        return firestoreLoad().getOrElse { seedLoad() }
    }

    private suspend fun loadFromFirestoreTeams(): Result<List<Team>> = runCatching {
        firestore.collection("teams")
            .whereEqualTo("isActive", true)
            .get()
            .await()
            .documents
            .map { it.toTeam() }
            .sortedWith(compareBy({ it.group }, { it.countryName }))
    }

    private suspend fun loadFromFirestorePlayers(): Result<List<Player>> = runCatching {
        firestore.collection("players")
            .whereEqualTo("isActive", true)
            .get()
            .await()
            .documents
            .map { it.toPlayer() }
    }

    private suspend fun loadFromFirestoreStickers(): Result<List<Sticker>> = runCatching {
        firestore.collection("stickers")
            .whereEqualTo("isActive", true)
            .get()
            .await()
            .documents
            .map { it.toSticker() }
    }
}
