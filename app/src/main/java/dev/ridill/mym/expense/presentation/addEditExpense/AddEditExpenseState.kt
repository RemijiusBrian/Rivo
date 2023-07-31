package dev.ridill.mym.expense.presentation.addEditExpense

data class AddEditExpenseState(
    val amountRecommendations: List<Long> = emptyList(),
    val tagsList: List<String> = emptyList(),
    val selectedTagId: String? = null,
    val expenseDate: String = "",
    val showDeleteConfirmation: Boolean = false
)