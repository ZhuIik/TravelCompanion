package com.example.travelcompanion.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelcompanion.data.ExpenseRepository
import com.example.travelcompanion.data.SavedEvent
import com.example.travelcompanion.data.SavedEventRepository
import com.example.travelcompanion.data.Trip
import com.example.travelcompanion.data.TripRepository
import com.example.travelcompanion.data.exchange.CurrencyConverter
import com.example.travelcompanion.data.exchange.ExchangeRatesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripDetailUiState(
    val trip: Trip? = null,
    val expenseCount: Int = 0,
    val totalInRub: Double = 0.0,
    val ratesAvailable: Boolean = true,
    val savedEvents: List<SavedEvent> = emptyList(),
    val isDeleted: Boolean = false
)

class TripDetailViewModel(
    private val tripRepository: TripRepository,
    private val expenseRepository: ExpenseRepository,
    private val exchangeRatesRepository: ExchangeRatesRepository,
    private val savedEventRepository: SavedEventRepository,
    private val tripId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripDetailUiState())
    val uiState: StateFlow<TripDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Локал до присваивания: иначе _uiState.value (получатель) читается ДО
            // suspend-вызова и более поздние корутины затрут это значение.
            val trip = tripRepository.getTripById(tripId)
            _uiState.value = _uiState.value.copy(trip = trip)
        }
        refresh()
    }

    // Вызывается при каждом возврате на экран: и расходы, и сохранённые события.
    fun refresh() {
        loadExpensesSummary()
        loadSavedEvents()
    }

    fun loadExpensesSummary() {
        viewModelScope.launch {
            val expenses = expenseRepository.getExpensesForTrip(tripId)
            val ratesResult = exchangeRatesRepository.getRatesForRub()
            val rates = ratesResult.getOrNull() ?: emptyMap()
            val ratesAvailable = ratesResult.isSuccess && rates.isNotEmpty()
            val totalInRub = if (ratesAvailable) {
                expenses.sumOf { CurrencyConverter.toRub(it.amount, it.currency, rates) }
            } else {
                0.0
            }
            _uiState.value = _uiState.value.copy(
                expenseCount = expenses.size,
                totalInRub = totalInRub,
                ratesAvailable = ratesAvailable
            )
        }
    }

    fun loadSavedEvents() {
        viewModelScope.launch {
            val events = savedEventRepository.getForTrip(tripId)
            _uiState.value = _uiState.value.copy(savedEvents = events)
        }
    }

    fun removeSavedEvent(savedEvent: SavedEvent) {
        viewModelScope.launch {
            savedEventRepository.delete(savedEvent)
            loadSavedEvents()
        }
    }

    fun deleteTrip() {
        val trip = _uiState.value.trip ?: return
        viewModelScope.launch {
            tripRepository.deleteTrip(trip)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }
}

class TripDetailViewModelFactory(
    private val tripRepository: TripRepository,
    private val expenseRepository: ExpenseRepository,
    private val exchangeRatesRepository: ExchangeRatesRepository,
    private val savedEventRepository: SavedEventRepository,
    private val tripId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            TripDetailViewModel::class.java ->
                TripDetailViewModel(
                    tripRepository, expenseRepository, exchangeRatesRepository,
                    savedEventRepository, tripId
                ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
