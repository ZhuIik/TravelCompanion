package com.example.travelcompanion.data.geo

import android.util.Log

data class CitySuggestion(
    val display: String, // полный display_name для показа в списке
    val city: String     // короткое английское название для БД и API
)

private const val TAG = "GeoRepository"

class GeoRepository(private val api: NominatimApi) {

    suspend fun suggestCities(query: String): List<CitySuggestion> {
        return try {
            api.search(query = query)
                .mapNotNull { place ->
                    val display = place.displayName?.trim().orEmpty()
                    if (display.isEmpty()) return@mapNotNull null
                    CitySuggestion(
                        display = display,
                        city = display.substringBefore(",").trim()
                    )
                }
                .distinctBy { it.city }
        } catch (e: Exception) {
            // Любая ошибка сети/парсинга — просто нет подсказок, ручной ввод продолжает работать.
            Log.w(TAG, "Nominatim suggestion failed for query=$query: ${e.message}")
            emptyList()
        }
    }
}
