package com.example.travelcompanion.data.exchange

import retrofit2.http.GET

interface ExchangeApi {
    // База RUB: rates[X] = сколько единиц валюты X приходится на 1 рубль.
    @GET("v4/latest/RUB")
    suspend fun getRatesForRub(): ExchangeRatesResponse
}
