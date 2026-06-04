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
 * Validates seed JSON: 48 teams, official FIFA squads (25–26 players each), 1296 stickers (crest + players).
 */
class SeedValidationTest {
    private val seedDir = File("src/main/assets/seed")
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun teamsCountIs48() {
        assertEquals(48, readArray("teams_seed.json").size)
    }

    @Test
    fun playersCountIs1248() {
        assertEquals(1248, readArray("players_seed.json").size)
    }

    @Test
    fun stickersCountIs1296() {
        assertEquals(1296, readArray("stickers_seed.json").size)
    }

    @Test
    fun eachTeamHasOneEmblemSticker() {
        val emblems = readArray("stickers_seed.json").filter {
            val obj = it.jsonObject
            obj["stickerNumber"]!!.jsonPrimitive.content == "0" &&
                obj["playerId"]!!.jsonPrimitive.content.isEmpty()
        }
        assertEquals(48, emblems.size)
    }

    @Test
    fun stickerIdsAreUnique() {
        val ids = readArray("stickers_seed.json").map { it.jsonObject["stickerId"]!!.jsonPrimitive.content }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun eachTeamHas25Or26Players() {
        val byTeam = readArray("players_seed.json")
            .groupingBy { it.jsonObject["teamId"]!!.jsonPrimitive.content }
            .eachCount()
        assertEquals(48, byTeam.size)
        byTeam.values.forEach { count ->
            assertTrue("Expected 25–26 players per team, got $count", count in 25..26)
        }
    }

    @Test
    fun playersHaveRatingsObject() {
        readArray("players_seed.json").forEach { element ->
            val obj = element.jsonObject
            assertTrue(
                "Missing ratings for ${obj["playerId"]!!.jsonPrimitive.content}",
                obj.containsKey("ratings"),
            )
            val ratings = obj["ratings"]!!.jsonObject
            assertTrue(ratings.containsKey("overall"))
            assertTrue(obj.containsKey("ratingsComplete"))
            assertTrue(obj.containsKey("clubName"))
            assertTrue(obj.containsKey("clubLeague"))
            assertTrue(obj.containsKey("clubLogoUrl"))
        }
    }

    private fun readArray(name: String) =
        json.parseToJsonElement(readAsset(name)).jsonArray

    private fun readAsset(name: String): String {
        val file = File(seedDir, name)
        assertTrue("Missing $name at ${file.absolutePath}", file.exists())
        return file.readText()
    }
}
