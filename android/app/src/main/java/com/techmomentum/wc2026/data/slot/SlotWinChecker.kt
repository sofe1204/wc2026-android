package com.techmomentum.wc2026.data.slot

import com.techmomentum.wc2026.data.model.SlotGridPosition

/** Mirrors server checkSlotWin in functions/src/helpers.ts (3 rows + 2 diagonals). */
object SlotWinChecker {
    private val lines = listOf(
        listOf(0 to 0, 0 to 1, 0 to 2),
        listOf(1 to 0, 1 to 1, 1 to 2),
        listOf(2 to 0, 2 to 1, 2 to 2),
        listOf(0 to 0, 1 to 1, 2 to 2),
        listOf(0 to 2, 1 to 1, 2 to 0),
    )

    fun winningCells(grid: List<List<String>>): Set<SlotGridPosition> {
        if (grid.size != 3 || grid.any { it.size != 3 }) return emptySet()

        return lines
            .filter { line ->
                val values = line.map { (row, col) -> grid[row][col].trim() }
                values.all { it.isNotBlank() && it != "unknown" } && values.distinct().size == 1
            }
            .flatten()
            .map { (row, col) -> SlotGridPosition(row, col) }
            .toSet()
    }

    fun isWin(grid: List<List<String>>): Boolean = winningCells(grid).isNotEmpty()
}
