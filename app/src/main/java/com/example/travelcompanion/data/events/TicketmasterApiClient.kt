package com.example.travelcompanion.data.events

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TicketmasterApiClient {
    private const val BASE_URL = "https://app.ticketmaster.com/"

    val api: TicketmasterApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TicketmasterApi::class.java)
    }
}
