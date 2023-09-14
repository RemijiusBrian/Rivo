package dev.ridill.mym.expense.presentation.allExpenses

import androidx.compose.ui.state.ToggleableState
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.expense.domain.model.ExpenseListItem
import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.domain.model.TagWithExpenditure
import java.time.LocalDate

data class AllExpensesState(
    val selectedDate: LocalDate = DateUtil.now().toLocalDate(),
    val yearsList: List<Int> = emptyList(),
    val totalExpenditure: Double = Double.Zero,
    val tagsWithExpenditures: List<TagWithExpenditure> = emptyList(),
    val selectedTag: ExpenseTag? = null,
    val expenseList: List<ExpenseListItem> = emptyList(),
    val selectedExpenseIds: List<Long> = emptyList(),
    val expenseSelectionState: ToggleableState = ToggleableState.Off,
    val expenseMultiSelectionModeActive: Boolean = false,
    val showDeleteExpenseConfirmation: Boolean = false,
    val showDeleteTagConfirmation: Boolean = false,
    val showNewTagInput: Boolean = false,
    val newTagError: UiText? = null,
    val showExcludedExpenses: Boolean = false
)