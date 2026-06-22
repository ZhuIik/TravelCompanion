package com.example.travelcompanion.data

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    suspend fun getExpensesForTrip(tripId: Long): List<Expense> = expenseDao.getByTripId(tripId)

    suspend fun addExpense(
        tripId: Long,
        category: String,
        amount: Double,
        currency: String,
        date: String
    ): Long {
        return expenseDao.insert(
            Expense(
                tripId = tripId,
                category = category,
                amount = amount,
                currency = currency,
                date = date
            )
        )
    }

    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)
}
