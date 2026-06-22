package com.example.travelcompanion.ui.nav

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.travelcompanion.data.ExpenseRepository
import com.example.travelcompanion.data.SavedEventRepository
import com.example.travelcompanion.data.TripRepository
import com.example.travelcompanion.data.events.EventsRepository
import com.example.travelcompanion.data.exchange.ExchangeRatesRepository
import com.example.travelcompanion.data.geo.GeoRepository
import com.example.travelcompanion.data.weather.WeatherRepository
import com.example.travelcompanion.ui.auth.AuthViewModelFactory
import com.example.travelcompanion.ui.auth.LoginScreen
import com.example.travelcompanion.ui.auth.RegisterScreen
import com.example.travelcompanion.ui.events.EventDetailScreen
import com.example.travelcompanion.ui.events.EventDetailViewModelFactory
import com.example.travelcompanion.ui.events.EventsScreen
import com.example.travelcompanion.ui.events.EventsViewModelFactory
import com.example.travelcompanion.ui.expense.ExpenseScreen
import com.example.travelcompanion.ui.expense.ExpenseViewModelFactory
import com.example.travelcompanion.ui.trip.CreateTripScreen
import com.example.travelcompanion.ui.trip.TripDetailScreen
import com.example.travelcompanion.ui.trip.TripDetailViewModelFactory
import com.example.travelcompanion.ui.trip.TripListScreen
import com.example.travelcompanion.ui.trip.TripViewModelFactory
import com.example.travelcompanion.ui.weather.WeatherViewModelFactory

object TravelDestinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TRIP_LIST = "trips/{userId}"
    const val CREATE_TRIP = "trips/{userId}/create"
    const val TRIP_DETAIL = "trip/{tripId}"
    const val EXPENSES = "trip/{tripId}/expenses"
    const val EVENTS = "trip/{tripId}/events/{city}"
    const val EVENT_DETAIL = "trip/{tripId}/event/{eventId}"

    fun tripList(userId: Long) = "trips/$userId"
    fun createTrip(userId: Long) = "trips/$userId/create"
    fun tripDetail(tripId: Long) = "trip/$tripId"
    fun expenses(tripId: Long) = "trip/$tripId/expenses"
    fun events(tripId: Long, city: String) = "trip/$tripId/events/${Uri.encode(city)}"
    fun eventDetail(tripId: Long, eventId: String) = "trip/$tripId/event/${Uri.encode(eventId)}"
}

@Composable
fun TravelNavGraph(
    authFactory: AuthViewModelFactory,
    tripRepository: TripRepository,
    weatherRepository: WeatherRepository,
    eventsRepository: EventsRepository,
    expenseRepository: ExpenseRepository,
    exchangeRatesRepository: ExchangeRatesRepository,
    savedEventRepository: SavedEventRepository,
    geoRepository: GeoRepository,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = TravelDestinations.LOGIN) {
        composable(TravelDestinations.LOGIN) {
            LoginScreen(
                factory = authFactory,
                onLoginSuccess = { userId ->
                    navController.navigate(TravelDestinations.tripList(userId)) {
                        popUpTo(TravelDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(TravelDestinations.REGISTER) }
            )
        }
        composable(TravelDestinations.REGISTER) {
            RegisterScreen(
                factory = authFactory,
                onRegisterSuccess = { userId ->
                    navController.navigate(TravelDestinations.tripList(userId)) {
                        popUpTo(TravelDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(
            TravelDestinations.TRIP_LIST,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            TripListScreen(
                factory = TripViewModelFactory(tripRepository, geoRepository, userId),
                onCreateTripClick = { navController.navigate(TravelDestinations.createTrip(userId)) },
                onTripClick = { tripId -> navController.navigate(TravelDestinations.tripDetail(tripId)) }
            )
        }
        composable(
            TravelDestinations.CREATE_TRIP,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            CreateTripScreen(
                factory = TripViewModelFactory(tripRepository, geoRepository, userId),
                onTripSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            TravelDestinations.TRIP_DETAIL,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
            TripDetailScreen(
                tripDetailFactory = TripDetailViewModelFactory(
                    tripRepository, expenseRepository, exchangeRatesRepository,
                    savedEventRepository, tripId
                ),
                weatherFactory = WeatherViewModelFactory(weatherRepository),
                onExpensesClick = { id -> navController.navigate(TravelDestinations.expenses(id)) },
                onEventsClick = { city -> navController.navigate(TravelDestinations.events(tripId, city)) },
                onBack = { navController.popBackStack() },
                onTripDeleted = { navController.popBackStack() }
            )
        }
        composable(
            TravelDestinations.EXPENSES,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
            ExpenseScreen(
                factory = ExpenseViewModelFactory(expenseRepository, exchangeRatesRepository, tripId),
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            TravelDestinations.EVENTS,
            arguments = listOf(
                navArgument("tripId") { type = NavType.LongType },
                navArgument("city") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
            val city = backStackEntry.arguments?.getString("city") ?: ""
            EventsScreen(
                city = city,
                factory = EventsViewModelFactory(eventsRepository),
                onBack = { navController.popBackStack() },
                onEventClick = { eventId ->
                    navController.navigate(TravelDestinations.eventDetail(tripId, eventId))
                }
            )
        }
        composable(
            TravelDestinations.EVENT_DETAIL,
            arguments = listOf(
                navArgument("tripId") { type = NavType.LongType },
                navArgument("eventId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(
                factory = EventDetailViewModelFactory(
                    eventsRepository, savedEventRepository, tripId, eventId
                ),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
