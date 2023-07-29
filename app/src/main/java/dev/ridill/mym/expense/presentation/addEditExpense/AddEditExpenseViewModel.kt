package dev.ridill.mym.expense.presentation.addEditExpense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.core.ui.navigation.destinations.AddEditExpenseDestination
import dev.ridill.mym.expense.domain.model.Expense
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val expenseRepo: ExpenseRepository
) : ViewModel(), AddEditExpenseActions {

    private val expenseIdArg = AddEditExpenseDestination
        .getExpenseIdFromSavedStateHandle(savedStateHandle)
    private val isEditMode = AddEditExpenseDestination.isEditMode(expenseIdArg)

    private val expense = savedStateHandle.getStateFlow(KEY_EXPENSE, Expense.DEFAULT)

    val amount = expense.map { it.amount }
        .distinctUntilChanged()
    val note = expense.map { it.note }
        .distinctUntilChanged()

    private val eventsChannel = Channel<AddEditExpenseEvent>()
    val events get() = eventsChannel.receiveAsFlow()

    init {
        onInit()
    }

    private fun onInit() = viewModelScope.launch {
        savedStateHandle[KEY_EXPENSE] = expenseRepo.getExpenseById(expenseIdArg)
            ?: Expense.DEFAULT
    }

    override fun onAmountChange(value: String) {
        savedStateHandle[KEY_EXPENSE] = expense.value
            .copy(amount = value)
    }

    override fun onNoteChange(value: String) {
        savedStateHandle[KEY_EXPENSE] = expense.value
            .copy(note = value)
    }

    override fun onSave() {
        viewModelScope.launch {
            val expense = expense.value
            expenseRepo.cacheExpense(expense)
            val event = if (isEditMode) AddEditExpenseEvent.ExpenseUpdated
            else AddEditExpenseEvent.ExpenseAdded
            eventsChannel.send(event)
        }
    }

    sealed class AddEditExpenseEvent {
        object ExpenseAdded : AddEditExpenseEvent()
        object ExpenseUpdated : AddEditExpenseEvent()
        object ExpenseDeleted : AddEditExpenseEvent()
    }
}

private const val KEY_EXPENSE = "KEY_EXPENSE"

const val RESULT_EXPENSE_ADDED = "RESULT_EXPENSE_ADDED"
const val RESULT_EXPENSE_UPDATED = "RESULT_EXPENSE_UPDATED"
const val RESULT_EXPENSE_DELETED = "RESULT_EXPENSE_DELETED"