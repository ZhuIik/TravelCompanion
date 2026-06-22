package com.example.travelcompanion.ui.events

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.travelcompanion.data.events.EventDetail
import com.example.travelcompanion.ui.common.EmptyState
import com.example.travelcompanion.ui.common.LoadingState

private const val TAG = "EventDetailScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    factory: EventDetailViewModelFactory,
    onBack: () -> Unit,
    viewModel: EventDetailViewModel = viewModel(factory = factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Событие") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    val detail = uiState.detail
                    if (detail != null) {
                        IconButton(onClick = { shareEvent(context, detail) }) {
                            Icon(Icons.Filled.Share, contentDescription = "Поделиться")
                        }
                        IconButton(onClick = { viewModel.toggleSaved() }) {
                            Icon(
                                imageVector = if (uiState.isSaved) Icons.Filled.Bookmark
                                else Icons.Filled.BookmarkBorder,
                                contentDescription = if (uiState.isSaved) "Убрать из поездки"
                                else "Добавить в поездку",
                                tint = if (uiState.isSaved) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(innerPadding))
            uiState.detail == null -> EmptyState(
                icon = Icons.Filled.Place,
                title = uiState.error ?: "Нет данных о событии",
                modifier = Modifier.padding(innerPadding)
            )
            else -> EventDetailContent(
                detail = uiState.detail!!,
                isSaved = uiState.isSaved,
                onRegister = { openUrl(context, uiState.detail!!.url) },
                onToggleSaved = { viewModel.toggleSaved() },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun EventDetailContent(
    detail: EventDetail,
    isSaved: Boolean,
    onRegister: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // c) Обложка или плейсхолдер
        CoverImage(detail.imageUrl)

        // a) Название
        Text(text = detail.title, style = MaterialTheme.typography.headlineSmall)

        // h) Категория-чип
        if (detail.category != null) {
            AssistChip(onClick = {}, label = { Text(detail.category) })
        }

        // f) Дата и время
        InfoRow(label = "Когда", value = detail.dateTimeText)

        // g) Место
        if (detail.venueName != null || detail.venueAddress != null) {
            val place = listOfNotNull(detail.venueName, detail.venueAddress).joinToString("\n")
            InfoRow(label = "Где", value = place)
        }

        // e) Организатор
        if (detail.organizerName != null) {
            InfoRow(label = "Организатор", value = detail.organizerName)
        }

        // d) Стоимость
        InfoRow(label = "Стоимость", value = detail.price)

        // b) Полное описание
        if (!detail.description.isNullOrBlank()) {
            Text(
                text = "Описание",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(text = detail.description, style = MaterialTheme.typography.bodyMedium)
        }

        // 3) Регистрация (единственный выход в браузер)
        Button(
            onClick = onRegister,
            enabled = !detail.url.isNullOrBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Зарегистрироваться")
        }

        // 5) Добавить/убрать из поездки
        OutlinedButton(
            onClick = onToggleSaved,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = if (isSaved) "Добавлено в поездку" else "Добавить в поездку",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun CoverImage(imageUrl: String?) {
    val shape = RoundedCornerShape(12.dp)
    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(shape),
            contentAlignment = Alignment.Center
        ) {
            // Плейсхолдер
            Card(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(shape)
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun openUrl(context: android.content.Context, url: String?) {
    if (url.isNullOrBlank()) {
        Toast.makeText(context, "У события нет ссылки", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Log.e(TAG, "No app to open url=$url", e)
        Toast.makeText(context, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
    }
}

private fun shareEvent(context: android.content.Context, detail: EventDetail) {
    val text = listOfNotNull(detail.title, detail.url).joinToString("\n")
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, detail.title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Поделиться событием"))
}
