package com.techmomentum.wc2026

import com.techmomentum.wc2026.data.model.SlotGridPosition
import com.techmomentum.wc2026.data.slot.SlotGridParser
import com.techmomentum.wc2026.data.slot.SlotWinChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SlotGridParserTest {
    @Test
    fun parse_prefersFlatSymbolIds() {
        val ids = listOf(
            "a", "b", "c",
            "d", "e", "f",
            "g", "h", "i",
        )
        val grid = SlotGridParser.parseSpinIds(mapOf("symbolIds" to ids))
        assertEquals(
            listOf(
                listOf("a", "b", "c"),
                listOf("d", "e", "f"),
                listOf("g", "h", "i"),
            ),
            grid,
        )
        assertTrue(SlotGridParser.isValid(grid))
    }

    @Test
    fun parse_handlesSymbolIdsAsMap() {
        val map = mapOf(
            0 to "salah",
            1 to "messi",
            2 to "kane",
            3 to "trophy",
            4 to "salah",
            5 to "ronaldo",
            6 to "mbappe",
            7 to "neymar",
            8 to "salah",
        )
        val grid = SlotGridParser.parseSpinIds(mapOf("symbolIds" to map))
        assertEquals("salah", grid[0][0])
        assertEquals("kane", grid[0][2])
        assertEquals("salah", grid[1][1])
        assertTrue(SlotGridParser.isValid(grid))
    }

    @Test
    fun parse_fallsBackToNestedGrid() {
        val grid = SlotGridParser.parseSpinIds(
            mapOf(
                "grid" to listOf(
                    listOf("trophy", "messi", "kane"),
                    listOf("salah", "trophy", "ronaldo"),
                    listOf("trophy", "mbappe", "neymar"),
                ),
            ),
        )
        assertTrue(SlotGridParser.isValid(grid))
    }

    @Test
    fun isValid_rejectsIncompleteGrid() {
        assertFalse(SlotGridParser.isValid(emptyList()))
        assertFalse(
            SlotGridParser.isValid(
                listOf(
                    listOf("a", "b"),
                    listOf("c", "d", "e"),
                    listOf("f", "g", "h"),
                ),
            ),
        )
    }

    @Test
    fun winningCells_detectsMiddleRowWin() {
        val grid = listOf(
            listOf("ronaldo", "neymar", "trophy"),
            listOf("salah", "salah", "salah"),
            listOf("kane", "mbappe", "neymar"),
        )
        val cells = SlotWinChecker.winningCells(grid)
        assertEquals(
            setOf(
                SlotGridPosition(1, 0),
                SlotGridPosition(1, 1),
                SlotGridPosition(1, 2),
            ),
            cells,
        )
    }

    @Test
    fun winningCells_detectsAntiDiagonal() {
        val grid = listOf(
            listOf("ronaldo", "neymar", "trophy"),
            listOf("salah", "trophy", "messi"),
            listOf("trophy", "mbappe", "neymar"),
        )
        val cells = SlotWinChecker.winningCells(grid)
        assertEquals(
            setOf(
                SlotGridPosition(0, 2),
                SlotGridPosition(1, 1),
                SlotGridPosition(2, 0),
            ),
            cells,
        )
    }
}
