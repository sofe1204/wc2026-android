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
 * Validates seed JSON structure: 48 teams, 720 players, 768 stickers (incl. crests).
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
    fun stickersCountIs768() {
        assertEquals(768, readArray("stickers_seed.json").size)
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
    fun eachTeamHas15Players() {
        val byTeam = readArray("players_seed.json")
            .groupingBy { it.jsonObject["teamId"]!!.jsonPrimitive.content }
            .eachCount()
        assertEquals(48, byTeam.size)
        byTeam.values.forEach { assertEquals(15, it) }
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
