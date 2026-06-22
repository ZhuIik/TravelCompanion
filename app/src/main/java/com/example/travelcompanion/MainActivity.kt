package com.example.travelcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.travelcompanion.data.AppDatabase
import com.example.travelcompanion.data.ExpenseRepository
import com.example.travelcompanion.data.SavedEventRepository
import com.example.travelcompanion.data.TripRepository
import com.example.travelcompanion.data.UserRepository
import com.example.travelcompanion.data.events.EventsRepository
import com.example.travelcompanion.data.events.TicketmasterApiClient
import com.example.travelcompanion.data.exchange.ExchangeApiClient
import com.example.travelcompanion.data.exchange.ExchangeRatesRepository
import com.example.travelcompanion.data.geo.GeoRepository
import com.example.travelcompanion.data.geo.NominatimApiClient
import com.example.travelcompanion.data.weather.WeatherApiClient
import com.example.travelcompanion.data.weather.WeatherRepository
import com.example.travelcompanion.ui.auth.AuthViewModelFactory
import com.example.travelcompanion.ui.nav.TravelNavGraph
import com.example.travelcompanion.ui.theme.TravelCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.getInstance(applicationContext)
        val authFactory = AuthViewModelFactory(UserRepository(database.userDao()))
        val tripRepository = TripRepository(database.tripDao())
        val expenseRepository = ExpenseRepository(database.expenseDao())
        val weatherRepository = WeatherRepository(WeatherApiClient.api, BuildConfig.WEATHER_API_KEY)
        val eventsRepository = EventsRepository(TicketmasterApiClient.api, BuildConfig.TICKETMASTER_API_KEY)
        val exchangeRatesRepository = ExchangeRatesRepository(ExchangeApiClient.api)
        val savedEventRepository = SavedEventRepository(database.savedEventDao())
        val geoRepository = GeoRepository(NominatimApiClient.api)
        setContent {
            TravelCompanionTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TravelNavGraph(
                        authFactory = authFactory,
                        tripRepository = tripRepository,
                        weatherRepository = weatherRepository,
                        eventsRepository = eventsRepository,
                        expenseRepository = expenseRepository,
                        exchangeRatesRepository = exchangeRatesRepository,
                        savedEventRepository = savedEventRepository,
                        geoRepository = geoRepository
                    )
                }
            }
        }
    }
}
