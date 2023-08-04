package dev.ridill.mym.expense.presentation.allExpenses

import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.expense.domain.model.TagWithExpenditure
import java.time.LocalDate

data class AllExpensesState(
    val selectedDate: LocalDate = DateUtil.now().toLocalDate(),
    val yearsList: List<Int> = emptyList(),
    val totalExpenditure: Double = Double.Zero,
    val tagsWithExpenditures: List<TagWithExpenditure> = emptyList(),
    val selectedTag: String? = null
)