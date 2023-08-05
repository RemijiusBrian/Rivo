package dev.ridill.mym.expense.presentation.allExpenses

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
import dev.ridill.mym.expense.domain.ExpenseBulkOperation
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import dev.ridill.mym.expense.domain.repository.TagsRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
        showDeleteTagConfirmation
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
                showDeleteTagConfirmation
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
            showDeleteTagConfirmation = showDeleteTagConfirmation
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
        // TODO: New Tag Input
    }

    override fun onExpenseLongClick(id: Long) {
        if (expenseMultiSelectionModeActive.value) dismissMultiSelectionMode()
        else {
            savedStateHandle[SELECTED_TAG] = null
            enableMultiSelectionModeWithId(id)
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

            ToggleableState.Off -> {
                savedStateHandle[SELECTED_EXPENSE_IDS] = listOf(expenseList.value.first().id)
            }

            ToggleableState.Indeterminate -> {
                savedStateHandle[SELECTED_EXPENSE_IDS] = expenseList.value.map { it.id }
            }
        }
    }

    override fun onDismissMultiSelectionMode() {
        dismissMultiSelectionMode()
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

    private suspend fun deleteExpenses(ids: List<Long>) {
        // TODO: Delete Expenses
    }

    override fun onDeleteDismiss() {
        savedStateHandle[SHOW_DELETE_EXPENSE_CONFIRMATION] = false
    }

    override fun onDeleteConfirm() {
        viewModelScope.launch {
            val selectedIds = selectedExpenseIds.value
            deleteExpenses(selectedIds)
            savedStateHandle[SHOW_DELETE_EXPENSE_CONFIRMATION] = false
        }
    }

    sealed class AllExpenseEvent {
        data class ShowUiMessage(val uiText: UiText) : AllExpenseEvent()
    }
}

private const val SELECTED_DATE = "SELECTED_DATE"
private const val SELECTED_TAG = "SELECTED_TAG_NAME"
private const val SELECTED_EXPENSE_IDS = "SELECTED_EXPENSE_IDS"
private const val EXPENSE_MULTI_SELECTION_MODE_ACTIVE = "EXPENSE_MULTI_SELECTION_MODE_ACTIVE"
private const val SHOW_DELETE_EXPENSE_CONFIRMATION = "SHOW_DELETE_EXPENSE_CONFIRMATION"
private const val SHOW_DELETE_TAG_CONFIRMATION = "SHOW_DELETE_TAG_CONFIRMATION"