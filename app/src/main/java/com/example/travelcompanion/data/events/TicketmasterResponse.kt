package com.example.travelcompanion.data.events

import com.google.gson.annotations.SerializedName

data class TicketmasterEventsResponse(
    @SerializedName("_embedded") val embedded: EmbeddedEvents?
)

data class EmbeddedEvents(
    @SerializedName("events") val events: List<TicketmasterEvent> = emptyList()
)

data class TicketmasterEvent(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("info") val info: String?,
    @SerializedName("pleaseNote") val pleaseNote: String?,
    @SerializedName("images") val images: List<EventImage>?,
    @SerializedName("dates") val dates: EventDates?,
    @SerializedName("priceRanges") val priceRanges: List<PriceRange>?,
    @SerializedName("classifications") val classifications: List<Classification>?,
    @SerializedName("promoter") val promoter: Promoter?,
    @SerializedName("_embedded") val embedded: EventEmbedded?
)

data class EventImage(
    @SerializedName("url") val url: String?,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?,
    @SerializedName("ratio") val ratio: String?
)

data class EventDates(
    @SerializedName("start") val start: EventStart?,
    @SerializedName("end") val end: EventEnd?
)

data class EventStart(
    @SerializedName("localDate") val localDate: String?,
    @SerializedName("localTime") val localTime: String?
)

data class EventEnd(
    @SerializedName("localDate") val localDate: String?,
    @SerializedName("localTime") val localTime: String?
)

data class PriceRange(
    @SerializedName("currency") val currency: String?,
    @SerializedName("min") val min: Double?,
    @SerializedName("max") val max: Double?
)

data class Classification(
    @SerializedName("segment") val segment: NamedRef?,
    @SerializedName("genre") val genre: NamedRef?
)

data class NamedRef(
    @SerializedName("name") val name: String?
)

data class Promoter(
    @SerializedName("name") val name: String?
)

data class EventEmbedded(
    @SerializedName("venues") val venues: List<Venue>?,
    @SerializedName("attractions") val attractions: List<Attraction>?
)

data class Venue(
    @SerializedName("name") val name: String?,
    @SerializedName("address") val address: VenueAddress?,
    @SerializedName("city") val city: NamedRef?
)

data class VenueAddress(
    @SerializedName("line1") val line1: String?
)

data class Attraction(
    @SerializedName("name") val name: String?
)
