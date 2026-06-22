package com.example.travelcompanion.ui.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelcompanion.data.events.EventInfo
import com.example.travelcompanion.ui.common.EmptyState
import com.example.travelcompanion.ui.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    city: String,
    factory: EventsViewModelFactory,
    onBack: () -> Unit,
    onEventClick: (String) -> Unit,
    viewModel: EventsViewModel = viewModel(factory = factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(city) {
        viewModel.loadEvents(city)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("События · $city") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is EventsUiState.Loading -> LoadingState(modifier = Modifier.padding(innerPadding))
            is EventsUiState.Empty -> EmptyState(
                icon = Icons.Filled.Place,
                title = "Событий в этом городе не найдено",
                modifier = Modifier.padding(innerPadding)
            )
            is EventsUiState.Error -> EmptyState(
                icon = Icons.Filled.Info,
                title = state.message,
                modifier = Modifier.padding(innerPadding)
            )
            is EventsUiState.Success -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                items(state.events, key = { it.id }) { event ->
                    EventCard(event = event, onClick = { onEventClick(event.id) })
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: EventInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium)
            if (event.startDate.isNotEmpty()) {
                Text(
                    text = event.startDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            event.venueName?.let { venue ->
                Text(
                    text = venue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            event.organizerName?.let { organizer ->
                Text(
                    text = organizer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
