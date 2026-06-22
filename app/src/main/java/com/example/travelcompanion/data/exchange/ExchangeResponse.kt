package com.example.travelcompanion.data.exchange

import com.google.gson.annotations.SerializedName

data class ExchangeRatesResponse(
    @SerializedName("base") val base: String,
    @SerializedName("rates") val rates: Map<String, Double>
)
