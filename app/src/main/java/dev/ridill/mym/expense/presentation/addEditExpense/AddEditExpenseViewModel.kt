package dev.ridill.mym.expense.presentation.addEditExpense

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notkamui.keval.keval
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.R
import dev.ridill.mym.core.data.db.MYMDatabase
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.domain.util.asStateFlow
import dev.ridill.mym.core.domain.util.tryOrNull
import dev.ridill.mym.core.ui.navigation.destinations.AddEditExpenseDestination
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.expense.domain.model.Expense
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import dev.ridill.mym.expense.domain.repository.TagsRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val expenseRepo: ExpenseRepository,
    private val tagsRepo: TagsRepository,
    private val eventBus: EventBus<AddEditExpenseEvent>
) : ViewModel(), AddEditExpenseActions {

    private val expenseIdArg = AddEditExpenseDestination
        .getExpenseIdFromSavedStateHandle(savedStateHandle)
    private val isEditMode = AddEditExpenseDestination.isEditMode(expenseIdArg)
    private val currentExpenseId: Long
        get() = expenseIdArg.takeIf { it >= MYMDatabase.DEFAULT_ID_LONG }
            ?: MYMDatabase.DEFAULT_ID_LONG

    val amountInput = savedStateHandle.getStateFlow(AMOUNT_INPUT, "")
    val noteInput = savedStateHandle.getStateFlow(NOTE_INPUT, "")

    private val tagsList = tagsRepo.getAllTags()
    private val selectedTagId = savedStateHandle.getStateFlow<String?>(SELECTED_TAG_ID, null)

    private val expenseDateTime = savedStateHandle.getStateFlow(EXPENSE_DATE_TIME, DateUtil.now())

    private val showDeleteConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_CONFIRMATION, false)

    private val amountRecommendations = expenseRepo.getAmountRecommendations()

    private val showNewTagInput = savedStateHandle
        .getStateFlow(SHOW_NEW_TAG_INPUT, false)

    val state = combineTuple(
        amountRecommendations,
        tagsList,
        selectedTagId,
        expenseDateTime,
        showDeleteConfirmation,
        showNewTagInput
    ).map { (
                amountRecommendations,
                tagsList,
                selectedTagId,
                expenseDateTime,
                showDeleteConfirmation,
                showNewTagInput
            ) ->
        AddEditExpenseState(
            amountRecommendations = amountRecommendations,
            tagsList = tagsList,
            selectedTagId = selectedTagId,
            expenseDateTime = expenseDateTime,
            showDeleteConfirmation = showDeleteConfirmation,
            showNewTagInput = showNewTagInput
        )
    }.asStateFlow(viewModelScope, AddEditExpenseState())

    val events = eventBus.eventFlow

    init {
        onInit()
    }

    private fun onInit() = viewModelScope.launch {
        val expense = expenseRepo.getExpenseById(expenseIdArg)
            ?: Expense.DEFAULT
        savedStateHandle[AMOUNT_INPUT] = expense.amount
        savedStateHandle[NOTE_INPUT] = expense.note
        savedStateHandle[EXPENSE_DATE_TIME] = expense.dateTime
    }

    override fun onAmountChange(value: String) {
        savedStateHandle[AMOUNT_INPUT] = value
    }

    override fun onNoteInputFocused() {
        val amountInput = amountInput.value
        savedStateHandle[AMOUNT_INPUT] = evalAmountExpression(amountInput).toString()
    }

    override fun onNoteChange(value: String) {
        savedStateHandle[NOTE_INPUT] = value
    }

    override fun onRecommendedAmountClick(amount: Long) {
        savedStateHandle[AMOUNT_INPUT] = amount.toString()
    }

    override fun onTagClick(tagId: String) {
        savedStateHandle[SELECTED_TAG_ID] = tagId
            .takeIf { selectedTagId.value != it }
    }

    override fun onSave() {
        viewModelScope.launch {
            val amount = evalAmountExpression(amountInput.value)
            if (amount <= -1.0) {
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
            val note = noteInput.value.trim()
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
            val tagId = selectedTagId.value
            expenseRepo.cacheExpense(
                id = currentExpenseId,
                amount = amount,
                note = note,
                tagId = tagId,
                dateTime = expenseDateTime.value
            )
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
            expenseRepo.deleteExpense(currentExpenseId)
            savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
            eventBus.send(AddEditExpenseEvent.ExpenseDeleted)
        }
    }

    private fun evalAmountExpression(exp: String): Double = tryOrNull {
        exp.keval()
    } ?: Double.Zero

    override fun onNewTagClick() {
        savedStateHandle[SHOW_NEW_TAG_INPUT] = true
    }

    override fun onNewTagNameChange(value: String) {
    }

    override fun onNewTagColorSelected(color: Color) {
    }

    override fun onNewTagSave() {
    }

    sealed class AddEditExpenseEvent {
        object ExpenseAdded : AddEditExpenseEvent()
        object ExpenseUpdated : AddEditExpenseEvent()
        object ExpenseDeleted : AddEditExpenseEvent()
        data class ShowUiMessage(val uiText: UiText) : AddEditExpenseEvent()
    }
}

private const val AMOUNT_INPUT = "AMOUNT_INPUT"
private const val NOTE_INPUT = "NOTE_INPUT"
private const val SELECTED_TAG_ID = "SELECTED_TAG_ID"
private const val EXPENSE_DATE_TIME = "EXPENSE_DATE_TIME"
private const val SHOW_DELETE_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"
private const val SHOW_NEW_TAG_INPUT = "SHOW_NEW_TAG_INPUT"

const val RESULT_EXPENSE_ADDED = "RESULT_EXPENSE_ADDED"
const val RESULT_EXPENSE_UPDATED = "RESULT_EXPENSE_UPDATED"
const val RESULT_EXPENSE_DELETED = "RESULT_EXPENSE_DELETED"