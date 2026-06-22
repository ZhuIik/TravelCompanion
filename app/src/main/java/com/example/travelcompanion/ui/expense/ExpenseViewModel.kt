package com.example.travelcompanion.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelcompanion.data.Expense
import com.example.travelcompanion.data.ExpenseRepository
import com.example.travelcompanion.data.exchange.CurrencyConverter
import com.example.travelcompanion.data.exchange.ExchangeRatesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true,
    // код валюты -> сколько её единиц в 1 рубле (база RUB)
    val rates: Map<String, Double> = emptyMap(),
    val ratesAvailable: Boolean = true,
    val totalInRub: Double = 0.0,
    // запасной вариант, когда курс недоступен: сумма по каждой валюте
    val totalsByCurrency: Map<String, Double> = emptyMap()
)

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val exchangeRatesRepository: ExchangeRatesRepository,
    private val tripId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val ratesResult = exchangeRatesRepository.getRatesForRub()
            val rates = ratesResult.getOrNull() ?: emptyMap()
            val ratesAvailable = ratesResult.isSuccess && rates.isNotEmpty()
            val expenses = expenseRepository.getExpensesForTrip(tripId)
            _uiState.value = recompute(expenses, rates, ratesAvailable, isLoading = false)
        }
    }

    fun addExpense(category: String, amount: Double, currency: String, date: String) {
        viewModelScope.launch {
            expenseRepository.addExpense(tripId, category, amount, currency, date)
            refreshExpenses()
        }
    }

    fun updateExpense(expense: Expense, category: String, amount: Double, currency: String, date: String) {
        viewModelScope.launch {
            expenseRepository.updateExpense(
                expense.copy(category = category, amount = amount, currency = currency, date = date)
            )
            refreshExpenses()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
            refreshExpenses()
        }
    }

    private suspend fun refreshExpenses() {
        val current = _uiState.value
        val expenses = expenseRepository.getExpensesForTrip(tripId)
        _uiState.value = recompute(expenses, current.rates, current.ratesAvailable, isLoading = false)
    }

    private fun recompute(
        expenses: List<Expense>,
        rates: Map<String, Double>,
        ratesAvailable: Boolean,
        isLoading: Boolean
    ): ExpenseUiState {
        val totalInRub = if (ratesAvailable) {
            expenses.sumOf { expense -> CurrencyConverter.toRub(expense.amount, expense.currency, rates) }
        } else {
            0.0
        }
        val totalsByCurrency = expenses
            .groupBy { it.currency }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        return ExpenseUiState(
            expenses = expenses,
            isLoading = isLoading,
            rates = rates,
            ratesAvailable = ratesAvailable,
            totalInRub = totalInRub,
            totalsByCurrency = totalsByCurrency
        )
    }
}

class ExpenseViewModelFactory(
    private val expenseRepository: ExpenseRepository,
    private val exchangeRatesRepository: ExchangeRatesRepository,
    private val tripId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            ExpenseViewModel::class.java ->
                ExpenseViewModel(expenseRepository, exchangeRatesRepository, tripId) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
