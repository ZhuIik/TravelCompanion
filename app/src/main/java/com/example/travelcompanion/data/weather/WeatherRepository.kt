package com.example.travelcompanion.data.weather

import android.util.Log
import retrofit2.HttpException
import java.io.IOException

data class WeatherInfo(val temperature: Double, val description: String)

private const val TAG = "WeatherRepository"

class WeatherRepository(
    private val api: WeatherApi,
    private val apiKey: String
) {
    suspend fun getCurrentWeather(city: String): Result<WeatherInfo> {
        return try {
            val response = api.getCurrentWeather(city = city, apiKey = apiKey)
            val description = response.weather.firstOrNull()?.description ?: ""
            Result.success(WeatherInfo(temperature = response.main.temp, description = description))
        } catch (e: HttpException) {
            Log.e(TAG, "Weather request failed with HTTP code ${e.code()} for city=$city", e)
            Result.failure(e)
        } catch (e: IOException) {
            Log.e(TAG, "Weather request failed due to network error for city=$city", e)
            Result.failure(e)
        }
    }
}
