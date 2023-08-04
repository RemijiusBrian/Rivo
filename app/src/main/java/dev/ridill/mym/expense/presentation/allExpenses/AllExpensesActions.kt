package dev.ridill.mym.expense.presentation.allExpenses

import java.time.Month

interface AllExpensesActions {
    fun onMonthSelect(month: Month)
    fun onYearSelect(year: String)
}