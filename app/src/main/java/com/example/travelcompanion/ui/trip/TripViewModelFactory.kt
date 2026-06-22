package com.example.travelcompanion.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.travelcompanion.data.TripRepository
import com.example.travelcompanion.data.geo.GeoRepository

class TripViewModelFactory(
    private val repository: TripRepository,
    private val geoRepository: GeoRepository,
    private val userId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            TripViewModel::class.java -> TripViewModel(repository, geoRepository, userId) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
