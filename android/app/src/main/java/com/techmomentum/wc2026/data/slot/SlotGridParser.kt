package com.techmomentum.wc2026.data.slot

/**
 * Parses slot spin grids from Cloud Function responses.
 * Prefer flat [symbolIds] — nested [grid] arrays deserialize inconsistently on Android.
 */
object SlotGridParser {
    fun parseSpinIds(data: Map<String, Any?>): List<List<String>> = toGrid(parseFlatIds(data))

    /** @deprecated Use [parseSpinIds]; kept for tests. */
    fun parse(data: Map<String, Any?>): List<List<String>> = parseSpinIds(data)

    fun isValid(grid: List<List<String>>): Boolean =
        grid.size == 3 && grid.all { row ->
            row.size == 3 && row.all { id -> id.isNotBlank() && id != "unknown" }
        }

    private fun parseFlatIds(data: Map<String, Any?>): List<String>? {
        flattenRaw(data["symbolIds"])?.let { return it }
        flattenRaw(data["grid"])?.let { flat ->
            if (flat.size >= 9) return flat.take(9)
        }
        val nested = data["grid"] as? List<*>
        if (nested != null) {
            val rows = nested.mapNotNull { row ->
                (row as? List<*>)?.map { cell -> cell?.toString()?.trim().orEmpty() }
            }
            if (rows.size >= 3) {
                return rows.take(3).flatMap { row ->
                    List(3) { col -> row.getOrNull(col).orEmpty() }
                }
            }
        }
        return null
    }

    private fun flattenRaw(raw: Any?): List<String>? = when (raw) {
        is List<*> -> raw.map { it?.toString()?.trim().orEmpty() }
        is Map<*, *> -> {
            val indexed = (0 until 9).mapNotNull { index ->
                val value = raw[index] ?: raw[index.toString()]
                value?.toString()?.trim()?.takeIf { it.isNotEmpty() }
            }
            indexed.takeIf { it.size >= 9 }
        }
        else -> null
    }

    private fun toGrid(flat: List<String>?): List<List<String>> {
        if (flat == null || flat.size < 9) return emptyList()
        return listOf(
            flat.subList(0, 3).toList(),
            flat.subList(3, 6).toList(),
            flat.subList(6, 9).toList(),
        )
    }
}
