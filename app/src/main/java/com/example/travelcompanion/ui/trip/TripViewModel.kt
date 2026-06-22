package com.example.travelcompanion.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelcompanion.data.Trip
import com.example.travelcompanion.data.TripRepository
import com.example.travelcompanion.data.geo.CitySuggestion
import com.example.travelcompanion.data.geo.GeoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripListUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = false
)

data class CreateTripUiState(
    val city: String = "",
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val citySuggestions: List<CitySuggestion> = emptyList()
)

class TripViewModel(
    private val repository: TripRepository,
    private val geoRepository: GeoRepository,
    private val userId: Long
) : ViewModel() {

    private val _listUiState = MutableStateFlow(TripListUiState())
    val listUiState: StateFlow<TripListUiState> = _listUiState.asStateFlow()

    private val _createUiState = MutableStateFlow(CreateTripUiState())
    val createUiState: StateFlow<CreateTripUiState> = _createUiState.asStateFlow()

    private var suggestJob: Job? = null

    init {
        loadTrips()
    }

    fun loadTrips() {
        _listUiState.value = _listUiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val trips = repository.getTripsForUser(userId)
            _listUiState.value = TripListUiState(trips = trips, isLoading = false)
        }
    }

    fun onCityChange(city: String) {
        _createUiState.value = _createUiState.value.copy(city = city, errorMessage = null)
        suggestJob?.cancel()
        if (city.trim().length < 2) {
            _createUiState.value = _createUiState.value.copy(citySuggestions = emptyList())
            return
        }
        // debounce ~400мс, чтобы не слать запрос на каждую букву
        suggestJob = viewModelScope.launch {
            delay(400)
            val suggestions = geoRepository.suggestCities(city.trim())
            _createUiState.value = _createUiState.value.copy(citySuggestions = suggestions)
        }
    }

    fun onCitySuggestionSelected(suggestion: CitySuggestion) {
        suggestJob?.cancel()
        _createUiState.value = _createUiState.value.copy(
            city = suggestion.city,
            citySuggestions = emptyList(),
            errorMessage = null
        )
    }

    fun dismissCitySuggestions() {
        suggestJob?.cancel()
        _createUiState.value = _createUiState.value.copy(citySuggestions = emptyList())
    }

    fun onStartDateChange(millis: Long?) {
        _createUiState.value = _createUiState.value.copy(startDateMillis = millis, errorMessage = null)
    }

    fun onEndDateChange(millis: Long?) {
        _createUiState.value = _createUiState.value.copy(endDateMillis = millis, errorMessage = null)
    }

    fun resetCreateState() {
        _createUiState.value = CreateTripUiState()
    }

    fun saveTrip() {
        val state = _createUiState.value
        val city = state.city.trim()
        val startMillis = state.startDateMillis
        val endMillis = state.endDateMillis

        if (city.isEmpty() || startMillis == null || endMillis == null) {
            _createUiState.value = state.copy(errorMessage = "Заполните город и обе даты")
            return
        }
        if (endMillis < startMillis) {
            _createUiState.value = state.copy(errorMessage = "Дата окончания раньше даты начала")
            return
        }

        _createUiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            repository.createTrip(
                userId = userId,
                city = city,
                startDate = formatDate(startMillis),
                endDate = formatDate(endMillis)
            )
            _createUiState.value = _createUiState.value.copy(isSaving = false, savedSuccessfully = true)
            loadTrips()
        }
    }
}
