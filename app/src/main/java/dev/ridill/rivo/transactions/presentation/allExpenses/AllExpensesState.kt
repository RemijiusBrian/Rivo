package dev.ridill.rivo.transactions.presentation.allExpenses

import androidx.compose.ui.state.ToggleableState
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.transactions.domain.model.ExpenseListItem
import dev.ridill.rivo.transactions.domain.model.ExpenseTag
import dev.ridill.rivo.transactions.domain.model.TagWithExpenditure
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
    val showTagInput: Boolean = false,
    val newTagError: UiText? = null,
    val showExcludedExpenses: Boolean = false
)