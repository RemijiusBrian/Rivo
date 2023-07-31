package dev.ridill.mym.expense.presentation.addEditExpense

interface AddEditExpenseActions {
    fun onAmountChange(value: String)
    fun onNoteChange(value: String)
    fun onRecommendedAmountClick(amount: Long)
    fun onTagClick(tagId: String)
    fun onSave()
    fun onDeleteClick()
    fun onDeleteDismiss()
    fun onDeleteConfirm()
}