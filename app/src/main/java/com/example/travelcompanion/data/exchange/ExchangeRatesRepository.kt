package com.example.travelcompanion.data.exchange

import android.util.Log
import retrofit2.HttpException
import java.io.IOException

private const val TAG = "ExchangeRatesRepository"

class ExchangeRatesRepository(private val api: ExchangeApi) {

    // Возвращает Map: код валюты -> сколько её единиц в 1 рубле (база RUB).
    suspend fun getRatesForRub(): Result<Map<String, Double>> {
        return try {
            val response = api.getRatesForRub()
            Result.success(response.rates)
        } catch (e: HttpException) {
            Log.e(TAG, "Exchange rates request failed with HTTP code ${e.code()}", e)
            Result.failure(e)
        } catch (e: IOException) {
            Log.e(TAG, "Exchange rates request failed due to network error", e)
            Result.failure(e)
        }
    }
}
