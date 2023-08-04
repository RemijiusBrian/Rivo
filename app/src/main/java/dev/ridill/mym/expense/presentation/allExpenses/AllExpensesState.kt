package dev.ridill.mym.expense.presentation.allExpenses

import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.expense.domain.model.TagWithExpenditure
import java.time.Month

data class AllExpensesState(
    val totalExpenditure: Double = Double.Zero,
    val tagsWithExpenditures: List<TagWithExpenditure> = emptyList(),
    val selectedTagId: String? = null,
    val selectedMonth: Month = Month.JANUARY,
    val yearsList: List<String> = emptyList(),
    val selectedYear: String = ""
)