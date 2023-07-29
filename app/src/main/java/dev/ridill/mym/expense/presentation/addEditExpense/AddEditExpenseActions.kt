package dev.ridill.mym.expense.presentation.addEditExpense

interface AddEditExpenseActions {
    fun onAmountChange(value: String)
    fun onNoteChange(value: String)
    fun onSave()
}