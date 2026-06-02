package com.techmomentum.wc2026

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Validates seed JSON structure: 48 teams, 720 players, 720 stickers.
 * TODO: Verify final 2026 squads before tournament.
 */
class SeedValidationTest {
    private val seedDir = File("src/main/assets/seed")
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun teamsCountIs48() {
        assertEquals(48, readArray("teams_seed.json").size)
    }

    @Test
    fun playersCountIs720() {
        assertEquals(720, readArray("players_seed.json").size)
    }

    @Test
    fun stickersCountIs720() {
        assertEquals(720, readArray("stickers_seed.json").size)
    }

    @Test
    fun stickerIdsAreUnique() {
        val ids = readArray("stickers_seed.json").map { it.jsonObject["stickerId"]!!.jsonPrimitive.content }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun eachTeamHas15Players() {
        val byTeam = readArray("players_seed.json")
            .groupingBy { it.jsonObject["teamId"]!!.jsonPrimitive.content }
            .eachCount()
        assertEquals(48, byTeam.size)
        byTeam.values.forEach { assertEquals(15, it) }
    }

    private fun readArray(name: String) =
        json.parseToJsonElement(readAsset(name)).jsonArray

    private fun readAsset(name: String): String {
        val file = File(seedDir, name)
        assertTrue("Missing $name at ${file.absolutePath}", file.exists())
        return file.readText()
    }
}
