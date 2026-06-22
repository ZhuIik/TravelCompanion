package com.example.travelcompanion.ui.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelcompanion.data.Expense
import com.example.travelcompanion.ui.common.EmptyState
import com.example.travelcompanion.ui.common.LoadingState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    factory: ExpenseViewModelFactory,
    onBack: () -> Unit,
    viewModel: ExpenseViewModel = viewModel(factory = factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    // null = форма закрыта; Expense = редактирование; sentinel new = добавление
    var editing by remember { mutableStateOf<Expense?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расходы") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Расход") }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TotalCard(uiState)

            when {
                uiState.isLoading -> LoadingState()
                uiState.expenses.isEmpty() -> EmptyState(
                    icon = Icons.Filled.ShoppingCart,
                    title = "Расходов пока нет",
                    subtitle = "Нажмите «Расход», чтобы добавить трату"
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.expenses, key = { it.id }) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onEdit = { editing = expense },
                            onDelete = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        ExpenseFormDialog(
            initial = null,
            onDismiss = { showAdd = false },
            onConfirm = { category, amount, currency, date ->
                viewModel.addExpense(category, amount, currency, date)
                showAdd = false
            }
        )
    }

    editing?.let { expense ->
        ExpenseFormDialog(
            initial = expense,
            onDismiss = { editing = null },
            onConfirm = { category, amount, currency, date ->
                viewModel.updateExpense(expense, category, amount, currency, date)
                editing = null
            }
        )
    }
}

@Composable
private fun TotalCard(uiState: ExpenseUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Всего по поездке", style = MaterialTheme.typography.titleMedium)
            if (uiState.ratesAvailable) {
                Text(
                    text = "${uiState.totalInRub.roundToInt()} ₽",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "Курс валют недоступен",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                uiState.totalsByCurrency.forEach { (currency, sum) ->
                    Text(
                        text = "${formatAmount(sum)} $currency",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = expense.category, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${formatAmount(expense.amount)} ${expense.currency}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = expense.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Редактировать")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatAmount(value: Double): String {
    return if (value % 1.0 == 0.0) value.toLong().toString() else String.format("%.2f", value)
}
