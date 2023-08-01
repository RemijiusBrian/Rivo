package dev.ridill.mym.expense.presentation.addEditExpense

import dev.ridill.mym.core.domain.util.Empty

data class AddEditExpenseState(
    val amountRecommendations: List<Long> = emptyList(),
    val tagsList: List<String> = emptyList(),
    val selectedTagId: String? = null,
    val expenseDate: String = String.Empty,
    val expenseTime: String = String.Empty,
    val showDeleteConfirmation: Boolean = false
)