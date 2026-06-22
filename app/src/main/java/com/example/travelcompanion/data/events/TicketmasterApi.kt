package com.example.travelcompanion.data.events

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TicketmasterApi {
    @GET("discovery/v2/events.json")
    suspend fun searchEvents(
        @Query("city") city: String,
        @Query("apikey") apiKey: String,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "date,asc"
    ): TicketmasterEventsResponse

    @GET("discovery/v2/events/{id}.json")
    suspend fun getEvent(
        @Path("id") id: String,
        @Query("apikey") apiKey: String
    ): TicketmasterEvent
}
