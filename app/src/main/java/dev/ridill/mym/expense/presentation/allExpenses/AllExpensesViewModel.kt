package dev.ridill.mym.expense.presentation.allExpenses

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.state.ToggleableState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.core.domain.util.asStateFlow
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.expense.domain.model.ExpenseBulkOperation
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import dev.ridill.mym.expense.domain.repository.TagsRepository
import dev.ridill.mym.expense.presentation.components.TagColors
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Month
import javax.inject.Inject

@HiltViewModel
class AllExpensesViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val tagsRepo: TagsRepository,
    private val savedStateHandle: SavedStateHandle,
    private val eventBus: EventBus<AllExpenseEvent>
) : ViewModel(), AllExpensesActions {

    private val selectedDate = savedStateHandle
        .getStateFlow(SELECTED_DATE, DateUtil.now().toLocalDate())

    private val totalExpenditure = selectedDate.flatMapLatest { date ->
        expenseRepo.getTotalExpenditureForDate(date)
    }.distinctUntilChanged()

    private val tagsWithExpenditures = combineTuple(
        selectedDate,
        totalExpenditure
    ).flatMapLatest { (date, expenditure) ->
        tagsRepo.getTagsWithExpenditures(
            date = date,
            totalExpenditure = expenditure
        )
    }

    private val selectedTag = savedStateHandle.getStateFlow<String?>(SELECTED_TAG, null)

    private val expenseList = combineTuple(
        selectedDate,
        selectedTag
    ).flatMapLatest { (date, tag) ->
        expenseRepo.getExpenseForDateByTag(date, tag)
    }.onEach { list ->
        val ids = list.map { it.id }
        savedStateHandle[SELECTED_EXPENSE_IDS] = selectedExpenseIds.value
            .filter { it in ids }
    }.asStateFlow(viewModelScope, emptyList())

    private val selectedExpenseIds = savedStateHandle
        .getStateFlow<List<Long>>(SELECTED_EXPENSE_IDS, emptyList())

    private val expenseSelectionState = combineTuple(
        expenseList,
        selectedExpenseIds
    ).map { (expenses, selectedIds) ->
        when {
            selectedIds.isEmpty() -> ToggleableState.Off
            expenses.all { it.id in selectedIds } -> ToggleableState.On
            else -> ToggleableState.Indeterminate
        }
    }.distinctUntilChanged()
        .asStateFlow(viewModelScope, ToggleableState.Off)

    private val expenseMultiSelectionModeActive = savedStateHandle
        .getStateFlow(EXPENSE_MULTI_SELECTION_MODE_ACTIVE, false)

    private val showDeleteExpenseConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_EXPENSE_CONFIRMATION, false)

    private val showDeleteTagConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_TAG_CONFIRMATION, false)

    private val showNewTagInput = savedStateHandle
        .getStateFlow(SHOW_NEW_TAG_INPUT, false)
    val tagNameInput = savedStateHandle
        .getStateFlow(TAG_NAME_INPUT, "")
    val tagColorInput = savedStateHandle
        .getStateFlow<Int?>(TAG_COLOR_INPUT, null)

    val state = combineTuple(
        selectedDate,
        totalExpenditure,
        tagsWithExpenditures,
        selectedTag,
        expenseList,
        selectedExpenseIds,
        expenseSelectionState,
        expenseMultiSelectionModeActive,
        showDeleteExpenseConfirmation,
        showDeleteTagConfirmation,
        showNewTagInput
    ).map { (
                selectedDate,
                totalExpenditure,
                tagsWithExpenditures,
                selectedTag,
                expenseList,
                selectedExpenseIds,
                expenseSelectionState,
                expenseMultiSelectionModeActive,
                showDeleteExpenseConfirmation,
                showDeleteTagConfirmation,
                showNewTagInput
            ) ->
        AllExpensesState(
            selectedDate = selectedDate,
            yearsList = listOf(2023),
            totalExpenditure = totalExpenditure,
            tagsWithExpenditures = tagsWithExpenditures,
            selectedTag = selectedTag,
            expenseList = expenseList,
            selectedExpenseIds = selectedExpenseIds,
            expenseSelectionState = expenseSelectionState,
            expenseMultiSelectionModeActive = expenseMultiSelectionModeActive,
            showDeleteExpenseConfirmation = showDeleteExpenseConfirmation,
            showDeleteTagConfirmation = showDeleteTagConfirmation,
            showNewTagInput = showNewTagInput
        )
    }.asStateFlow(viewModelScope, AllExpensesState())

    val events = eventBus.eventFlow

    override fun onMonthSelect(month: Month) {
        dismissMultiSelectionMode()
        savedStateHandle[SELECTED_DATE] = selectedDate.value.withMonth(month.value)
    }

    override fun onYearSelect(year: Int) {
        dismissMultiSelectionMode()
        savedStateHandle[SELECTED_DATE] = selectedDate.value.withYear(year)
    }

    override fun onTagClick(tag: String) {
        if (expenseMultiSelectionModeActive.value) viewModelScope.launch {
            val selectedIds = selectedExpenseIds.value
            tagsRepo.assignTagToExpenses(tag, selectedIds)
            dismissMultiSelectionMode()
            savedStateHandle[SELECTED_TAG] = tag
            eventBus.send(AllExpenseEvent.ShowUiMessage(UiText.StringResource(R.string.tag_assigned_to_expenses)))
        } else {
            savedStateHandle[SELECTED_TAG] = tag
                .takeIf { it != selectedTag.value }
        }
    }

    override fun onNewTagClick() {
        savedStateHandle[SHOW_NEW_TAG_INPUT] = true
    }

    override fun onNewTagNameChange(value: String) {
        savedStateHandle[TAG_NAME_INPUT] = value
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
                ?: TagColors.first()

            if (name.isEmpty()) {
                clearAndHideTagInput()
                eventBus.send(
                    AllExpenseEvent.ShowUiMessage(
                        UiText.StringResource(
                            R.string.error_invalid_tag_name,
                            true
                        )
                    )
                )
                return@launch
            }

            tagsRepo.saveTag(
                name = name,
                color = color
            )

            clearAndHideTagInput()
            eventBus.send(
                AllExpenseEvent.ShowUiMessage(UiText.StringResource(R.string.new_tag_created))
            )
        }
    }

    private fun clearAndHideTagInput() {
        savedStateHandle[SHOW_NEW_TAG_INPUT] = false
        savedStateHandle[TAG_NAME_INPUT] = ""
        savedStateHandle[TAG_COLOR_INPUT] = null
    }

    override fun onExpenseLongClick(id: Long) {
        viewModelScope.launch {
            if (expenseMultiSelectionModeActive.value) dismissMultiSelectionMode()
            else {
                savedStateHandle[SELECTED_TAG] = null
                enableMultiSelectionModeWithId(id)
            }
            eventBus.send(AllExpenseEvent.ProvideHapticFeedback(HapticFeedbackType.LongPress))
        }
    }

    override fun onExpenseClick(id: Long) {
        if (!expenseMultiSelectionModeActive.value) return
        val selectedIds = selectedExpenseIds.value
        if (id in selectedIds) {
            savedStateHandle[SELECTED_EXPENSE_IDS] = selectedIds - id
            if (selectedExpenseIds.value.isEmpty()) dismissMultiSelectionMode()
        } else {
            savedStateHandle[SELECTED_EXPENSE_IDS] = selectedIds + id
        }
    }

    override fun onSelectionStateChange() {
        when (expenseSelectionState.value) {
            ToggleableState.On -> {
                savedStateHandle[SELECTED_EXPENSE_IDS] = emptyList<Long>()
            }

            else -> {
                savedStateHandle[SELECTED_EXPENSE_IDS] = expenseList.value.map { it.id }
            }
        }
    }

    override fun onDismissMultiSelectionMode() {
        viewModelScope.launch {
            dismissMultiSelectionMode()
            eventBus.send(AllExpenseEvent.ProvideHapticFeedback(HapticFeedbackType.LongPress))
        }
    }

    private fun dismissMultiSelectionMode() {
        savedStateHandle[EXPENSE_MULTI_SELECTION_MODE_ACTIVE] = false
        savedStateHandle[SELECTED_EXPENSE_IDS] = emptyList<Long>()
    }

    private fun enableMultiSelectionModeWithId(id: Long) {
        savedStateHandle[SELECTED_EXPENSE_IDS] = listOf(id)
        savedStateHandle[EXPENSE_MULTI_SELECTION_MODE_ACTIVE] = true
    }

    override fun onExpenseBulkOperationClick(operation: ExpenseBulkOperation) {
        viewModelScope.launch {
            when (operation) {
                ExpenseBulkOperation.UNTAG -> {
                    untagExpenses(selectedExpenseIds.value)
                }

                ExpenseBulkOperation.DELETE -> {
                    savedStateHandle[SHOW_DELETE_EXPENSE_CONFIRMATION] = true
                }
            }
        }
    }

    private suspend fun untagExpenses(ids: List<Long>) {
        tagsRepo.untagExpenses(ids)
        dismissMultiSelectionMode()
        eventBus.send(AllExpenseEvent.ShowUiMessage(UiText.StringResource(R.string.expenses_untagged)))
    }

    override fun onDeleteExpenseDismiss() {
        savedStateHandle[SHOW_DELETE_EXPENSE_CONFIRMATION] = false
    }

    override fun onDeleteExpenseConfirm() {
        viewModelScope.launch {
            val selectedIds = selectedExpenseIds.value
            deleteExpenses(selectedIds)
            savedStateHandle[SHOW_DELETE_EXPENSE_CONFIRMATION] = false
            dismissMultiSelectionMode()
            eventBus.send(AllExpenseEvent.ShowUiMessage(UiText.StringResource(R.string.expenses_deleted)))
        }
    }

    private suspend fun deleteExpenses(ids: List<Long>) {
        expenseRepo.deleteExpenses(ids)
    }

    override fun onDeleteTagClick(tagName: String) {
        savedStateHandle[DELETION_TAG_NAME] = tagName
        savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = true
    }

    override fun onDeleteTagDismiss() {
        savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = false
        savedStateHandle[DELETION_TAG_NAME] = null
    }

    override fun onDeleteTagConfirm() {
        val tagName = savedStateHandle.get<String?>(DELETION_TAG_NAME) ?: return
        viewModelScope.launch {
            tagsRepo.deleteTagByName(tagName)
            savedStateHandle[SELECTED_TAG] = null
            savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = false
            savedStateHandle[DELETION_TAG_NAME] = null
            eventBus.send(
                AllExpenseEvent.ShowUiMessage(UiText.StringResource(R.string.tag_deleted))
            )
        }
    }

    sealed class AllExpenseEvent {
        data class ShowUiMessage(val uiText: UiText) : AllExpenseEvent()
        data class ProvideHapticFeedback(val type: HapticFeedbackType) : AllExpenseEvent()
    }
}

private const val SELECTED_DATE = "SELECTED_DATE"
private const val SELECTED_TAG = "SELECTED_TAG_NAME"
private const val SELECTED_EXPENSE_IDS = "SELECTED_EXPENSE_IDS"
private const val EXPENSE_MULTI_SELECTION_MODE_ACTIVE = "EXPENSE_MULTI_SELECTION_MODE_ACTIVE"
private const val SHOW_DELETE_EXPENSE_CONFIRMATION = "SHOW_DELETE_EXPENSE_CONFIRMATION"
private const val SHOW_DELETE_TAG_CONFIRMATION = "SHOW_DELETE_TAG_CONFIRMATION"
private const val SHOW_NEW_TAG_INPUT = "SHOW_NEW_TAG_INPUT"
private const val TAG_NAME_INPUT = "TAG_NAME_INPUT"
private const val TAG_COLOR_INPUT = "TAG_COLOR_INPUT"
private const val DELETION_TAG_NAME = "DELETION_TAG_NAME"