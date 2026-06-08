package com.techmomentum.wc2026

import com.techmomentum.wc2026.data.model.SlotSymbol
import com.techmomentum.wc2026.data.slot.SlotCellBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SlotCellBuilderTest {
    private val catalog = listOf(
        SlotSymbol(
            symbolId = "egypt_mohamed_salah",
            playerId = "egypt_mohamed_salah",
            label = "Mohamed Salah",
            imageUrl = "https://example.com/salah.png",
        ),
        SlotSymbol(
            symbolId = "brazil_neymar",
            playerId = "brazil_neymar",
            label = "Neymar",
            imageUrl = "https://example.com/neymar.png",
        ),
    )

    @Test
    fun build_usesExactSpinIdOnly() {
        val spinIds = listOf(
            listOf("egypt_mohamed_salah", "brazil_neymar", "unknown_doc"),
            listOf("brazil_neymar", "egypt_mohamed_salah", "brazil_neymar"),
            listOf("unknown_doc", "brazil_neymar", "egypt_mohamed_salah"),
        )
        val cells = SlotCellBuilder.build(spinIds, catalog)
        assertEquals("egypt_mohamed_salah", cells[0][0].spinId)
        assertEquals("Mohamed Salah", cells[0][0].symbol.label)
        assertEquals("unknown_doc", cells[0][2].spinId)
        assertNotEquals("Mohamed Salah", cells[0][2].symbol.label)
    }
}
