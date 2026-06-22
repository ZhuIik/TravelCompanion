package com.example.travelcompanion.data.events

import android.util.Log
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class EventInfo(
    val id: String,
    val title: String,
    val startDate: String,
    val venueName: String?,
    val organizerName: String?,
    val url: String?
)

data class EventDetail(
    val id: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val price: String,
    val organizerName: String?,
    val dateTimeText: String,
    val venueName: String?,
    val venueAddress: String?,
    val category: String?,
    val url: String?,
    val startDate: String
)

private const val TAG = "EventsRepository"
private val RU = Locale.forLanguageTag("ru")

class EventsRepository(
    private val api: TicketmasterApi,
    private val apiKey: String
) {
    suspend fun getEventsForCity(city: String): Result<List<EventInfo>> {
        return try {
            val response = api.searchEvents(city = city, apiKey = apiKey)
            val events = response.embedded?.events.orEmpty()
                .filter { it.id != null }
                .map { event ->
                    EventInfo(
                        id = event.id!!,
                        title = event.name ?: "Без названия",
                        startDate = shortStart(event),
                        venueName = event.embedded?.venues?.firstOrNull()?.name,
                        organizerName = event.embedded?.attractions?.firstOrNull()?.name,
                        url = event.url
                    )
                }
            Result.success(events)
        } catch (e: HttpException) {
            Log.e(TAG, "Events request failed with HTTP code ${e.code()} for city=$city", e)
            Result.failure(e)
        } catch (e: IOException) {
            Log.e(TAG, "Events request failed due to network error for city=$city", e)
            Result.failure(e)
        }
    }

    suspend fun getEventById(id: String): Result<EventDetail> {
        return try {
            val e = api.getEvent(id = id, apiKey = apiKey)
            Result.success(
                EventDetail(
                    id = e.id ?: id,
                    title = e.name ?: "Без названия",
                    description = e.info ?: e.pleaseNote,
                    imageUrl = bestImage(e),
                    price = formatPrice(e),
                    organizerName = e.promoter?.name
                        ?: e.embedded?.attractions?.firstOrNull()?.name,
                    dateTimeText = formatDateTime(e),
                    venueName = e.embedded?.venues?.firstOrNull()?.name,
                    venueAddress = formatAddress(e),
                    category = formatCategory(e),
                    url = e.url,
                    startDate = shortStart(e)
                )
            )
        } catch (e: HttpException) {
            Log.e(TAG, "Event detail request failed with HTTP code ${e.code()} for id=$id", e)
            Result.failure(e)
        } catch (e: IOException) {
            Log.e(TAG, "Event detail request failed due to network error for id=$id", e)
            Result.failure(e)
        }
    }

    private fun shortStart(event: TicketmasterEvent): String {
        val start = event.dates?.start ?: return ""
        return listOfNotNull(start.localDate, start.localTime?.take(5)).joinToString(" ")
    }

    private fun bestImage(e: TicketmasterEvent): String? {
        val images = e.images.orEmpty().filter { !it.url.isNullOrBlank() }
        if (images.isEmpty()) return null
        // Берём самое широкое изображение для обложки.
        return images.maxByOrNull { it.width ?: 0 }?.url
    }

    private fun formatPrice(e: TicketmasterEvent): String {
        val pr = e.priceRanges?.firstOrNull { it.min != null }
        return if (pr?.min != null) {
            val currency = pr.currency ?: ""
            "от ${trimAmount(pr.min)} $currency".trim()
        } else {
            "Платно, уточните на странице мероприятия"
        }
    }

    private fun trimAmount(value: Double): String =
        if (value % 1.0 == 0.0) value.toLong().toString() else String.format(Locale.US, "%.2f", value)

    private fun formatCategory(e: TicketmasterEvent): String? {
        val c = e.classifications?.firstOrNull() ?: return null
        return c.genre?.name ?: c.segment?.name
    }

    private fun formatAddress(e: TicketmasterEvent): String? {
        val venue = e.embedded?.venues?.firstOrNull() ?: return null
        val parts = listOfNotNull(venue.address?.line1, venue.city?.name)
        return parts.joinToString(", ").ifBlank { null }
    }

    // "21 июня 2026, 18:00 — 22:00" (конец показываем только если есть время окончания)
    private fun formatDateTime(e: TicketmasterEvent): String {
        val start = e.dates?.start ?: return "Дата уточняется"
        val date = start.localDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val datePart = date?.format(DateTimeFormatter.ofPattern("d MMMM yyyy", RU)) ?: (start.localDate ?: "")
        val startTime = start.localTime?.let { formatTime(it) }
        val endTime = e.dates.end?.localTime?.let { formatTime(it) }

        return buildString {
            append(datePart)
            if (startTime != null) {
                append(", ")
                append(startTime)
                if (endTime != null) {
                    append(" — ")
                    append(endTime)
                }
            }
        }.ifBlank { "Дата уточняется" }
    }

    private fun formatTime(time: String): String? {
        val parsed = runCatching { LocalTime.parse(time) }.getOrNull() ?: return time.take(5)
        return parsed.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
}
