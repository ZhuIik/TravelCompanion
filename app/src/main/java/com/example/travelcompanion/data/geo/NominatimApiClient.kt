package com.example.travelcompanion.data.geo

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NominatimApiClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    // Nominatim требует осмысленный User-Agent, иначе блокирует запросы.
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "TravelCompanionApp/1.0")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: NominatimApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApi::class.java)
    }
}
