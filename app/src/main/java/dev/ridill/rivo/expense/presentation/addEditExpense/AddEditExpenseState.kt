package dev.ridill.rivo.expense.presentation.addEditExpense

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.CurrencyUtil
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.expense.domain.model.ExpenseTag
import java.time.LocalDateTime

data class AddEditExpenseState(
    val currency: Currency = CurrencyUtil.default,
    val amountRecommendations: List<Long> = emptyList(),
    val tagsList: List<ExpenseTag> = emptyList(),
    val selectedTagId: Long? = null,
    val expenseTimestamp: LocalDateTime = DateUtil.now(),
    val isExpenseExcluded: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showNewTagInput: Boolean = false,
    val newTagError: UiText? = null,
    val showDateTimePicker: Boolean = false
) {
    val expenseDateFormatted: String
        get() = expenseTimestamp.format(DateUtil.Formatters.localizedDateMedium)
}