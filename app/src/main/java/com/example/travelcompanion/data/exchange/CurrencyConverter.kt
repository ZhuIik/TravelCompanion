package com.example.travelcompanion.data.exchange

/**
 * Пересчёт суммы в рубли по курсам с базой RUB.
 * rates[currency] = сколько единиц валюты приходится на 1 рубль,
 * поэтому 1 единица валюты = 1 / rates[currency] рублей.
 */
object CurrencyConverter {
    fun toRub(amount: Double, currency: String, rates: Map<String, Double>): Double {
        if (currency == "RUB") return amount
        val rate = rates[currency] ?: return amount
        if (rate == 0.0) return amount
        return amount / rate
    }
}
