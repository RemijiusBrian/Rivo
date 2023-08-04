package dev.ridill.mym.expense.presentation.addEditExpense

import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.expense.domain.model.ExpenseTag
import java.time.LocalDateTime

data class AddEditExpenseState(
    val amountRecommendations: List<Long> = emptyList(),
    val tagsList: List<ExpenseTag> = emptyList(),
    val selectedTagId: String? = null,
    val expenseDateTime: LocalDateTime = DateUtil.now(),
    val showDeleteConfirmation: Boolean = false,
    val showNewTagInput: Boolean = false
) {
    val expenseTimeFormatted: String
        get() = expenseDateTime.format(DateUtil.Formatters.localizedTimeShort)

    val expenseDateFormatted: String
        get() = expenseDateTime.format(DateUtil.Formatters.ddth_MMM_spaceSep)
}