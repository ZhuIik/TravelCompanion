package com.example.travelcompanion.data.geo

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class NominatimPlace(
    @SerializedName("display_name") val displayName: String?
)

interface NominatimApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("accept-language") acceptLanguage: String = "en",
        @Query("limit") limit: Int = 5
    ): List<NominatimPlace>
}
