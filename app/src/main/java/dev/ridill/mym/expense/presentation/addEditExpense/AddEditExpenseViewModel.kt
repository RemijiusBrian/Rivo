package dev.ridill.mym.expense.presentation.addEditExpense

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.R
import dev.ridill.mym.core.data.db.MYMDatabase
import dev.ridill.mym.core.domain.service.ExpEvalService
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.core.domain.util.TextFormat
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.domain.util.asStateFlow
import dev.ridill.mym.core.domain.util.orZero
import dev.ridill.mym.core.ui.navigation.destinations.AddEditExpenseDestinationSpec
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.expense.domain.model.Expense
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import dev.ridill.mym.expense.domain.repository.TagsRepository
import dev.ridill.mym.expense.presentation.components.TagColors
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val expenseRepo: ExpenseRepository,
    private val tagsRepo: TagsRepository,
    private val eventBus: EventBus<AddEditExpenseEvent>,
    private val evalService: ExpEvalService
) : ViewModel(), AddEditExpenseActions {

    private val expenseIdArg = AddEditExpenseDestinationSpec
        .getExpenseIdFromSavedStateHandle(savedStateHandle)
    private val isEditMode = AddEditExpenseDestinationSpec.isEditMode(expenseIdArg)
    private val currentExpenseId: Long
        get() = expenseIdArg.takeIf { it >= MYMDatabase.DEFAULT_ID_LONG }
            ?: MYMDatabase.DEFAULT_ID_LONG

    val amountInput = savedStateHandle.getStateFlow(AMOUNT_INPUT, "")
    val noteInput = savedStateHandle.getStateFlow(NOTE_INPUT, "")

    private val tagsList = tagsRepo.getAllTags()
    private val selectedTagId = savedStateHandle.getStateFlow<String?>(SELECTED_TAG_ID, null)

    private val expenseTimestamp = savedStateHandle.getStateFlow(EXPENSE_TIMESTAMP, DateUtil.now())

    private val showDeleteConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_CONFIRMATION, false)

    private val amountRecommendations = expenseRepo.getAmountRecommendations()

    private val showNewTagInput = savedStateHandle
        .getStateFlow(SHOW_NEW_TAG_INPUT, false)
    val tagNameInput = savedStateHandle
        .getStateFlow(TAG_NAME_INPUT, "")
    val tagColorInput = savedStateHandle
        .getStateFlow<Int?>(TAG_COLOR_INPUT, null)
    private val newTagError = savedStateHandle.getStateFlow<UiText?>(NEW_TAG_ERROR, null)

    private val showDateTimePicker = savedStateHandle.getStateFlow(SHOW_DATE_TIME_PICKER, false)

    val state = combineTuple(
        amountRecommendations,
        tagsList,
        selectedTagId,
        expenseTimestamp,
        showDeleteConfirmation,
        showNewTagInput,
        newTagError,
        showDateTimePicker
    ).map { (
                amountRecommendations,
                tagsList,
                selectedTagId,
                expenseTimestamp,
                showDeleteConfirmation,
                showNewTagInput,
                newTagError,
                showDateTimePicker
            ) ->
        AddEditExpenseState(
            amountRecommendations = amountRecommendations,
            tagsList = tagsList,
            selectedTagId = selectedTagId,
            expenseTimestamp = expenseTimestamp,
            showDeleteConfirmation = showDeleteConfirmation,
            showNewTagInput = showNewTagInput,
            newTagError = newTagError,
            showDateTimePicker = showDateTimePicker
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
        savedStateHandle[EXPENSE_TIMESTAMP] = expense.createdTimestamp
        savedStateHandle[SELECTED_TAG_ID] = expense.tagId
    }

    override fun onAmountChange(value: String) {
        savedStateHandle[AMOUNT_INPUT] = value
    }

    override fun onNoteInputFocused() {
        val amountInput = amountInput.value
            .trim()
            .ifEmpty { return }

        val isExpression = evalService.isExpression(amountInput)
        val result = if (isExpression) evalService.evalOrNull(amountInput)
        else TextFormat.parseNumber(amountInput)
        savedStateHandle[AMOUNT_INPUT] = TextFormat.number(
            value = result.orZero(),
            isGroupingUsed = false
        )
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

    override fun onExpenseTimestampClick() {
        savedStateHandle[SHOW_DATE_TIME_PICKER] = true
    }

    override fun onExpenseTimestampSelectionDismiss() {
        savedStateHandle[SHOW_DATE_TIME_PICKER] = false
    }

    override fun onExpenseTimestampSelectionConfirm(dateTime: LocalDateTime) {
        savedStateHandle[EXPENSE_TIMESTAMP] = dateTime
        savedStateHandle[SHOW_DATE_TIME_PICKER] = false
    }

    override fun onSaveClick() {
        viewModelScope.launch {
            val amountInput = amountInput.value.trim()
            val isExp = evalService.isExpression(amountInput)
            val amount = (if (isExp) evalService.evalOrNull(amountInput)
            else TextFormat.parseNumber(amountInput)) ?: -1.0
            if (amount < Double.Zero) {
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
                dateTime = expenseTimestamp.value
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

    override fun onNewTagClick() {
        savedStateHandle[TAG_COLOR_INPUT] = TagColors.first().toArgb()
        savedStateHandle[SHOW_NEW_TAG_INPUT] = true
    }

    override fun onNewTagNameChange(value: String) {
        savedStateHandle[TAG_NAME_INPUT] = value
        savedStateHandle[NEW_TAG_ERROR] = null

    }

    override fun onNewTagColorSelect(color: Color) {
        savedStateHandle[TAG_COLOR_INPUT] = color.toArgb()
    }

    override fun onNewTagInputDismiss() {
        clearAndHideTagInput()
    }

    override fun onNewTagInputConfirm() {
        viewModelScope.launch {
            val name = tagNameInput.value.trim()
            val color = tagColorInput.value?.let { Color(it) }

            if (name.isEmpty()) {
                savedStateHandle[NEW_TAG_ERROR] = UiText.StringResource(
                    R.string.error_invalid_tag_name,
                    true
                )
                return@launch
            }

            if (color == null) {
                savedStateHandle[NEW_TAG_ERROR] = UiText.StringResource(
                    R.string.error_invalid_tag_color,
                    true
                )
                return@launch
            }

            tagsRepo.saveTag(
                name = name,
                color = color
            )

            clearAndHideTagInput()
            savedStateHandle[SELECTED_TAG_ID] = name
            eventBus.send(
                AddEditExpenseEvent.ShowUiMessage(UiText.StringResource(R.string.new_tag_created))
            )
        }
    }

    private fun clearAndHideTagInput() {
        savedStateHandle[SHOW_NEW_TAG_INPUT] = false
        savedStateHandle[TAG_NAME_INPUT] = ""
        savedStateHandle[TAG_COLOR_INPUT] = null
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
private const val EXPENSE_TIMESTAMP = "EXPENSE_TIMESTAMP"
private const val SHOW_DELETE_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"
private const val SHOW_NEW_TAG_INPUT = "SHOW_NEW_TAG_INPUT"
private const val TAG_NAME_INPUT = "TAG_NAME_INPUT"
private const val TAG_COLOR_INPUT = "TAG_COLOR_INPUT"
private const val SHOW_DATE_TIME_PICKER = "SHOW_DATE_TIME_PICKER"
private const val NEW_TAG_ERROR = "NEW_TAG_ERROR"

const val RESULT_EXPENSE_ADDED = "RESULT_EXPENSE_ADDED"
const val RESULT_EXPENSE_UPDATED = "RESULT_EXPENSE_UPDATED"
const val RESULT_EXPENSE_DELETED = "RESULT_EXPENSE_DELETED"