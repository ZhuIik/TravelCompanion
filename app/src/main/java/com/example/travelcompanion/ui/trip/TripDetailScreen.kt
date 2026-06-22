package com.example.travelcompanion.ui.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelcompanion.data.SavedEvent
import com.example.travelcompanion.ui.common.LoadingState
import com.example.travelcompanion.ui.weather.WeatherUiState
import com.example.travelcompanion.ui.weather.WeatherViewModel
import com.example.travelcompanion.ui.weather.WeatherViewModelFactory
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripDetailFactory: TripDetailViewModelFactory,
    weatherFactory: WeatherViewModelFactory,
    onExpensesClick: (Long) -> Unit,
    onEventsClick: (String) -> Unit,
    onBack: () -> Unit,
    onTripDeleted: () -> Unit,
    tripDetailViewModel: TripDetailViewModel = viewModel(factory = tripDetailFactory),
    weatherViewModel: WeatherViewModel = viewModel(factory = weatherFactory)
) {
    val detailState by tripDetailViewModel.uiState.collectAsState()
    val weatherState by weatherViewModel.uiState.collectAsState()
    val trip = detailState.trip
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(trip?.city) {
        trip?.city?.let { weatherViewModel.loadWeather(it) }
    }

    LaunchedEffect(detailState.isDeleted) {
        if (detailState.isDeleted) onTripDeleted()
    }

    // Обновляем расходы и сохранённые события при каждом возврате на экран.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        tripDetailViewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip?.city ?: "Поездка") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (trip == null) {
            LoadingState(modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "${trip.startDate} — ${trip.endDate}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Погода
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Погода",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    when (val state = weatherState) {
                        is WeatherUiState.Loading -> CircularProgressIndicator(
                            modifier = Modifier.padding(top = 12.dp).size(28.dp)
                        )
                        is WeatherUiState.Success -> {
                            Text(
                                text = "${state.weather.temperature.roundToInt()}°C",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = state.weather.description.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is WeatherUiState.Error -> Text(
                            text = "Не удалось загрузить погоду",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Расходы
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Расходы", style = MaterialTheme.typography.titleMedium)
                    if (detailState.ratesAvailable) {
                        Text(
                            text = "${detailState.totalInRub.roundToInt()} ₽",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Text(
                            text = "Курс валют недоступен",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Text(
                        text = "Записей: ${detailState.expenseCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Button(
                onClick = { onExpensesClick(trip.id) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Расходы") }

            OutlinedButton(
                onClick = { onEventsClick(trip.city) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("События") }

            // Запланированные события
            Text(
                text = "Запланированные события",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (detailState.savedEvents.isEmpty()) {
                Text(
                    text = "Пока нет сохранённых событий. Откройте «События» и добавьте интересное в поездку.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                detailState.savedEvents.forEach { saved ->
                    SavedEventCard(
                        saved = saved,
                        onRemove = { tripDetailViewModel.removeSavedEvent(saved) }
                    )
                }
            }

            Button(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(text = "Удалить поездку", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить поездку?") },
            text = {
                Text(
                    "Поездка «${trip?.city}», все её расходы и сохранённые события " +
                        "будут удалены без возможности восстановления."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    tripDetailViewModel.deleteTrip()
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun SavedEventCard(saved: SavedEvent, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = saved.name, style = MaterialTheme.typography.titleSmall)
                if (saved.startDate.isNotBlank()) {
                    Text(
                        text = saved.startDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Убрать из поездки",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
