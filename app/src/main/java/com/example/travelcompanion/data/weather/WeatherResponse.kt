package com.example.travelcompanion.data.weather

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("main") val main: MainInfo,
    @SerializedName("weather") val weather: List<WeatherDescription>
)

data class MainInfo(
    @SerializedName("temp") val temp: Double
)

data class WeatherDescription(
    @SerializedName("description") val description: String
)
