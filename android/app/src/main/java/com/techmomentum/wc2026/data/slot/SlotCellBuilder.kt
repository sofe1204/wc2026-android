package com.techmomentum.wc2026.data.slot

import com.techmomentum.wc2026.data.model.SlotCell
import com.techmomentum.wc2026.data.model.SlotSymbol
import kotlin.random.Random

object SlotCellBuilder {
    fun build(spinIds: List<List<String>>, catalog: List<SlotSymbol>): List<List<SlotCell>> {
        val lookup = catalog.associateBy { it.symbolId }
        return spinIds.mapIndexed { row, rowIds ->
            rowIds.mapIndexed { col, rawId ->
                val spinId = rawId.trim()
                SlotCell(
                    row = row,
                    col = col,
                    spinId = spinId,
                    symbol = lookup[spinId] ?: SlotSymbol.placeholder(spinId),
                )
            }
        }
    }

    fun randomPreview(catalog: List<SlotSymbol>): List<List<SlotCell>> {
        if (catalog.isEmpty()) return emptyList()
        return List(3) { row ->
            List(3) { col ->
                val symbol = catalog[Random.nextInt(catalog.size)]
                SlotCell(
                    row = row,
                    col = col,
                    spinId = symbol.symbolId,
                    symbol = symbol,
                )
            }
        }
    }
}
