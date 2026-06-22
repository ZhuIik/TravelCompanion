package com.example.travelcompanion.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelcompanion.data.weather.WeatherInfo
import com.example.travelcompanion.data.weather.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherInfo) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun loadWeather(city: String) {
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            val result = repository.getCurrentWeather(city)
            _uiState.value = result.fold(
                onSuccess = { WeatherUiState.Success(it) },
                onFailure = { WeatherUiState.Error("Не удалось загрузить погоду") }
            )
        }
    }
}

class WeatherViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            WeatherViewModel::class.java -> WeatherViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
