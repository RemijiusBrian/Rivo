package dev.ridill.mym.expense.presentation.addEditExpense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.core.ui.navigation.destinations.AddEditExpenseDestination
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.expense.domain.model.Expense
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val expenseRepo: ExpenseRepository,
    private val eventBus: EventBus<AddEditExpenseEvent>
) : ViewModel(), AddEditExpenseActions {

    private val expenseIdArg = AddEditExpenseDestination
        .getExpenseIdFromSavedStateHandle(savedStateHandle)
    private val isEditMode = AddEditExpenseDestination.isEditMode(expenseIdArg)

    private val expense = savedStateHandle.getStateFlow(KEY_EXPENSE, Expense.DEFAULT)

    val amount = expense.map { it.amount }
        .distinctUntilChanged()
    val note = expense.map { it.note }
        .distinctUntilChanged()

    val showDeleteConfirmation = savedStateHandle.getStateFlow(SHOW_DELETE_CONFIRMATION, false)

    val amountRecommendations = expenseRepo.getAmountRecommendations()

    val events = eventBus.eventFlow

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

    override fun onRecommendedAmountClick(amount: Long) {
        savedStateHandle[KEY_EXPENSE] = expense.value
            .copy(amount = amount.toString())
    }

    override fun onSave() {
        viewModelScope.launch {
            val expense = expense.value
            if (expense.evalAmount <= -1.0) {
                eventBus.send(
                    AddEditExpenseEvent.ShowUiMessage(
                        UiText.StringResource(
                            R.string.error_invalid_amount,
                            true
                        )
                    )
                )
                return@launch
            }
            val note = expense.note.trim()
            if (note.isEmpty()) {
                eventBus.send(
                    AddEditExpenseEvent.ShowUiMessage(
                        UiText.StringResource(
                            R.string.error_invalid_expense_note,
                            true
                        )
                    )
                )
                return@launch
            }
            expenseRepo.cacheExpense(expense.copy(note = note))
            val event = if (isEditMode) AddEditExpenseEvent.ExpenseUpdated
            else AddEditExpenseEvent.ExpenseAdded
            eventBus.send(event)
        }
    }

    override fun onDeleteClick() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = true
    }

    override fun onDeleteDismiss() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
    }

    override fun onDeleteConfirm() {
        viewModelScope.launch {
            expenseRepo.deleteExpense(expense.value)
            savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
            eventBus.send(AddEditExpenseEvent.ExpenseDeleted)
        }
    }

    sealed class AddEditExpenseEvent {
        object ExpenseAdded : AddEditExpenseEvent()
        object ExpenseUpdated : AddEditExpenseEvent()
        object ExpenseDeleted : AddEditExpenseEvent()
        data class ShowUiMessage(val uiText: UiText) : AddEditExpenseEvent()
    }
}

private const val KEY_EXPENSE = "KEY_EXPENSE"
private const val SHOW_DELETE_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"

const val RESULT_EXPENSE_ADDED = "RESULT_EXPENSE_ADDED"
const val RESULT_EXPENSE_UPDATED = "RESULT_EXPENSE_UPDATED"
const val RESULT_EXPENSE_DELETED = "RESULT_EXPENSE_DELETED"