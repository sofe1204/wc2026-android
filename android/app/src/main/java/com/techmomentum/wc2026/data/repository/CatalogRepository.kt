package com.techmomentum.wc2026.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.techmomentum.wc2026.data.dataconnect.SqlConnectCatalogDataSource
import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.SlotSymbol
import com.techmomentum.wc2026.data.model.Sticker
import com.techmomentum.wc2026.data.model.Team
import com.techmomentum.wc2026.data.remote.toPlayer
import com.techmomentum.wc2026.data.remote.toSlotSymbol
import com.techmomentum.wc2026.data.remote.toSticker
import com.techmomentum.wc2026.data.remote.toTeam
import com.techmomentum.wc2026.data.seed.SeedJsonParser
import com.techmomentum.wc2026.data.session.AppSession
import com.techmomentum.wc2026.utils.GameConstants
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
    private var slotSymbolsCache: List<SlotSymbol>? = null

    suspend fun getTeams(): List<Team> {
        teamsCache?.let { return it }
        val teams = resolveTeams(
            sqlLoad = { sqlConnectCatalog.loadTeams() },
            firestoreLoad = { loadFromFirestoreTeams() },
            seedLoad = { seedJsonParser.loadTeams() },
        )
        teamsCache = teams
        return teams
    }

    suspend fun getPlayers(): List<Player> {
        playersCache?.let { return it }
        val players = resolvePlayers(
            sqlLoad = { sqlConnectCatalog.loadPlayers() },
            firestoreLoad = { loadFromFirestorePlayers() },
            seedLoad = { seedJsonParser.loadPlayers() },
        )
        playersCache = players
        return players
    }

    suspend fun getStickers(): List<Sticker> {
        stickersCache?.let { return it }
        val stickers = resolveStickers(
            sqlLoad = { sqlConnectCatalog.loadStickers() },
            firestoreLoad = { loadFromFirestoreStickers() },
            seedLoad = { seedJsonParser.loadStickers() },
        )
        stickersCache = stickers
        return stickers
    }

    suspend fun getSlotSymbols(): List<SlotSymbol> {
        slotSymbolsCache?.let { return it }
        val symbols = if (appSession.isActive()) {
            seedJsonParser.loadSlotSymbols()
        } else {
            loadFromFirestoreSlotSymbols().getOrNull()
                ?.takeIf { it.size >= GameConstants.SLOT_SYMBOL_COUNT }
                ?: seedJsonParser.loadSlotSymbols()
        }
        slotSymbolsCache = symbols
        return symbols
    }

    fun clearCache() {
        teamsCache = null
        playersCache = null
        stickersCache = null
        slotSymbolsCache = null
    }

    suspend fun getTeam(teamId: String): Team? = getTeams().firstOrNull { it.teamId == teamId }

    suspend fun getSticker(stickerId: String): Sticker? = getStickers().firstOrNull { it.stickerId == stickerId }

    suspend fun getPlayer(playerId: String): Player? = getPlayers().firstOrNull { it.playerId == playerId }

    /**
     * Priority: SQL Connect (Kotlin SDK) → Firestore → bundled seed JSON.
     * Guest mode always uses bundled seed. Signed-in users fall back to seed when
     * Firestore is empty or still has a partial/old catalog (packs still need a full
     * Firestore seed via [scripts/seed_firestore.mjs]).
     */
    private suspend fun resolveTeams(
        sqlLoad: suspend () -> List<Team>,
        firestoreLoad: suspend () -> Result<List<Team>>,
        seedLoad: suspend () -> List<Team>,
    ): List<Team> = resolveCatalog(
        minCount = GameConstants.TOTAL_TEAMS,
        sqlLoad = sqlLoad,
        firestoreLoad = firestoreLoad,
        seedLoad = seedLoad,
    )

    private suspend fun resolvePlayers(
        sqlLoad: suspend () -> List<Player>,
        firestoreLoad: suspend () -> Result<List<Player>>,
        seedLoad: suspend () -> List<Player>,
    ): List<Player> = resolveCatalog(
        minCount = GameConstants.TOTAL_TEAMS * GameConstants.PLAYERS_PER_TEAM,
        sqlLoad = sqlLoad,
        firestoreLoad = firestoreLoad,
        seedLoad = seedLoad,
    )

    private suspend fun resolveStickers(
        sqlLoad: suspend () -> List<Sticker>,
        firestoreLoad: suspend () -> Result<List<Sticker>>,
        seedLoad: suspend () -> List<Sticker>,
    ): List<Sticker> = resolveCatalog(
        minCount = GameConstants.TOTAL_STICKERS,
        sqlLoad = sqlLoad,
        firestoreLoad = firestoreLoad,
        seedLoad = seedLoad,
    )

    private suspend fun <T> resolveCatalog(
        minCount: Int,
        sqlLoad: suspend () -> List<T>,
        firestoreLoad: suspend () -> Result<List<T>>,
        seedLoad: suspend () -> List<T>,
    ): List<T> {
        if (appSession.isActive()) return seedLoad()
        if (sqlConnectCatalog.isAvailable()) {
            runCatching { sqlLoad() }.getOrNull()?.takeIf { it.size >= minCount }?.let { return it }
        }
        val fromFirestore = firestoreLoad().getOrNull()?.takeIf { it.size >= minCount }
        if (fromFirestore != null) return fromFirestore
        return seedLoad()
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

    private suspend fun loadFromFirestoreSlotSymbols(): Result<List<SlotSymbol>> = runCatching {
        firestore.collection("slot_symbols")
            .whereEqualTo("isActive", true)
            .get()
            .await()
            .documents
            .map { it.toSlotSymbol() }
    }
}
