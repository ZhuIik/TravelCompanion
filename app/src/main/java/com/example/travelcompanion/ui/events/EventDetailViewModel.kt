package com.example.travelcompanion.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelcompanion.data.SavedEvent
import com.example.travelcompanion.data.SavedEventRepository
import com.example.travelcompanion.data.events.EventDetail
import com.example.travelcompanion.data.events.EventsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val isLoading: Boolean = true,
    val detail: EventDetail? = null,
    val error: String? = null,
    val isSaved: Boolean = false
)

class EventDetailViewModel(
    private val eventsRepository: EventsRepository,
    private val savedEventRepository: SavedEventRepository,
    private val tripId: Long,
    private val eventId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val saved = savedEventRepository.isSaved(tripId, eventId)
            val result = eventsRepository.getEventById(eventId)
            _uiState.value = result.fold(
                onSuccess = { detail ->
                    EventDetailUiState(isLoading = false, detail = detail, isSaved = saved)
                },
                onFailure = {
                    EventDetailUiState(
                        isLoading = false,
                        error = "Не удалось загрузить информацию о событии",
                        isSaved = saved
                    )
                }
            )
        }
    }

    fun toggleSaved() {
        val detail = _uiState.value.detail ?: return
        viewModelScope.launch {
            if (_uiState.value.isSaved) {
                savedEventRepository.removeByEvent(tripId, detail.id)
                _uiState.value = _uiState.value.copy(isSaved = false)
            } else {
                savedEventRepository.save(
                    SavedEvent(
                        tripId = tripId,
                        eventId = detail.id,
                        name = detail.title,
                        startDate = detail.startDate,
                        imageUrl = detail.imageUrl,
                        eventUrl = detail.url ?: ""
                    )
                )
                _uiState.value = _uiState.value.copy(isSaved = true)
            }
        }
    }
}

class EventDetailViewModelFactory(
    private val eventsRepository: EventsRepository,
    private val savedEventRepository: SavedEventRepository,
    private val tripId: Long,
    private val eventId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            EventDetailViewModel::class.java ->
                EventDetailViewModel(eventsRepository, savedEventRepository, tripId, eventId) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
