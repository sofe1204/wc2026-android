package com.techmomentum.wc2026.utils

import android.content.Context
import com.techmomentum.wc2026.data.model.CountryOption
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class CountryRow(val code: String, val name: String)

@Singleton
class WorldCountries @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val countries: List<CountryOption> by lazy {
        val raw = context.assets.open("countries.json").bufferedReader().use { it.readText() }
        json.decodeFromString<List<CountryRow>>(raw)
            .map { CountryOption(code = it.code, name = it.name) }
            .sortedBy { it.name }
    }

    fun all(): List<CountryOption> = countries

    fun findByCode(code: String): CountryOption? =
        countries.find { it.code.equals(code, ignoreCase = true) }
}
