package dev.ridill.mym.expense.presentation.addEditExpense

import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.expense.domain.model.ExpenseTag
import java.time.LocalDateTime

data class AddEditExpenseState(
    val amountRecommendations: List<Long> = emptyList(),
    val tagsList: List<ExpenseTag> = emptyList(),
    val selectedTagId: String? = null,
    val expenseTimestamp: LocalDateTime = DateUtil.now(),
    val showDeleteConfirmation: Boolean = false,
    val showNewTagInput: Boolean = false,
    val newTagError: UiText? = null,
    val showDateTimePicker: Boolean = false
) {
    val expenseDateFormatted: String
        get() = expenseTimestamp.format(DateUtil.Formatters.localizedDateMedium)
}