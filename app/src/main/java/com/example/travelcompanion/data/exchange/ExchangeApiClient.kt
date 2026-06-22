package com.example.travelcompanion.data.exchange

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ExchangeApiClient {
    private const val BASE_URL = "https://api.exchangerate-api.com/"

    val api: ExchangeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeApi::class.java)
    }
}
