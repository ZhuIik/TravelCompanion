package com.example.travelcompanion.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelcompanion.data.events.EventInfo
import com.example.travelcompanion.data.events.EventsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class EventsUiState {
    object Loading : EventsUiState()
    data class Success(val events: List<EventInfo>) : EventsUiState()
    object Empty : EventsUiState()
    data class Error(val message: String) : EventsUiState()
}

class EventsViewModel(private val repository: EventsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    fun loadEvents(city: String) {
        _uiState.value = EventsUiState.Loading
        viewModelScope.launch {
            val result = repository.getEventsForCity(city)
            _uiState.value = result.fold(
                onSuccess = { events ->
                    if (events.isEmpty()) EventsUiState.Empty else EventsUiState.Success(events)
                },
                onFailure = { error ->
                    val message = if (error is HttpException && (error.code() == 401 || error.code() == 403)) {
                        "Ошибка авторизации: проверьте API-ключ Eventbrite"
                    } else {
                        "Не удалось загрузить события"
                    }
                    EventsUiState.Error(message)
                }
            )
        }
    }
}

class EventsViewModelFactory(private val repository: EventsRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            EventsViewModel::class.java -> EventsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
