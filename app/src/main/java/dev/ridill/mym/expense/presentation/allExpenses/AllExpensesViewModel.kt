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
import dev.ridill.mym.expense.domain.model.ExpenseOption
import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import dev.ridill.mym.expense.domain.repository.TagsRepository
import kotlinx.coroutines.flow.collectLatest
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

    private val yearsList = expenseRepo.getExpenseYearsList()

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

    private val selectedTag = savedStateHandle.getStateFlow<ExpenseTag?>(SELECTED_TAG, null)

    private val showExcludedExpenses = expenseRepo.getShowExcludedExpenses()

    private val expenseList = combineTuple(
        selectedDate,
        selectedTag,
        showExcludedExpenses
    ).flatMapLatest { (date, tag, showExcluded) ->
        expenseRepo.getExpenseForDateByTag(
            date = date,
            tagId = tag?.id,
            showExcluded = showExcluded
        )
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
        .getStateFlow(SHOW_TAG_INPUT, false)
    val tagInput = savedStateHandle.getStateFlow<ExpenseTag?>(TAG_INPUT, null)
    private val newTagError = savedStateHandle.getStateFlow<UiText?>(NEW_TAG_ERROR, null)

    val state = combineTuple(
        selectedDate,
        yearsList,
        totalExpenditure,
        tagsWithExpenditures,
        selectedTag,
        expenseList,
        selectedExpenseIds,
        expenseSelectionState,
        expenseMultiSelectionModeActive,
        showDeleteExpenseConfirmation,
        showDeleteTagConfirmation,
        showNewTagInput,
        newTagError,
        showExcludedExpenses
    ).map { (
                selectedDate,
                yearsList,
                totalExpenditure,
                tagsWithExpenditures,
                selectedTag,
                expenseList,
                selectedExpenseIds,
                expenseSelectionState,
                expenseMultiSelectionModeActive,
                showDeleteExpenseConfirmation,
                showDeleteTagConfirmation,
                showNewTagInput,
                newTagError,
                showExcludedExpenses
            ) ->
        AllExpensesState(
            selectedDate = selectedDate,
            yearsList = yearsList,
            totalExpenditure = totalExpenditure,
            tagsWithExpenditures = tagsWithExpenditures,
            selectedTag = selectedTag,
            expenseList = expenseList,
            selectedExpenseIds = selectedExpenseIds,
            expenseSelectionState = expenseSelectionState,
            expenseMultiSelectionModeActive = expenseMultiSelectionModeActive,
            showDeleteExpenseConfirmation = showDeleteExpenseConfirmation,
            showDeleteTagConfirmation = showDeleteTagConfirmation,
            showTagInput = showNewTagInput,
            newTagError = newTagError,
            showExcludedExpenses = showExcludedExpenses
        )
    }.asStateFlow(viewModelScope, AllExpensesState())

    val events = eventBus.eventFlow

    init {
        refreshSelectedIdsListOnExpenseListChange()
    }

    private fun refreshSelectedIdsListOnExpenseListChange() = viewModelScope.launch {
        expenseList.collectLatest { list ->
            val ids = list.map { it.id }
            savedStateHandle[SELECTED_EXPENSE_IDS] = selectedExpenseIds.value
                .filter { it in ids }
        }
    }

    override fun onMonthSelect(month: Month) {
        dismissMultiSelectionMode()
        savedStateHandle[SELECTED_DATE] = selectedDate.value.withMonth(month.value)
    }

    override fun onYearSelect(year: Int) {
        dismissMultiSelectionMode()
        savedStateHandle[SELECTED_DATE] = selectedDate.value.withYear(year)
    }

    override fun onTagClick(tag: ExpenseTag) {
        if (expenseMultiSelectionModeActive.value) viewModelScope.launch {
            val selectedIds = selectedExpenseIds.value
            tagsRepo.assignTagToExpenses(tag.id, selectedIds)
            dismissMultiSelectionMode()
            savedStateHandle[SELECTED_TAG] = tag
            eventBus.send(AllExpenseEvent.ShowUiMessage(UiText.StringResource(R.string.tag_assigned_to_expenses)))
        } else {
            savedStateHandle[SELECTED_TAG] = tag
                .takeIf { it != selectedTag.value }
        }
    }

    override fun onNewTagClick() {
        savedStateHandle[TAG_INPUT] = ExpenseTag.NEW
        savedStateHandle[SHOW_TAG_INPUT] = true
    }

    override fun onTagInputNameChange(value: String) {
        savedStateHandle[TAG_INPUT] = tagInput.value
            ?.copy(name = value)
        savedStateHandle[NEW_TAG_ERROR] = null
    }

    override fun onTagInputColorSelect(color: Color) {
        savedStateHandle[TAG_INPUT] = tagInput.value
            ?.copy(colorCode = color.toArgb())
    }

    override fun onTagInputExclusionChange(excluded: Boolean) {
        savedStateHandle[TAG_INPUT] = tagInput.value
            ?.copy(excluded = excluded)
    }

    override fun onTagInputDismiss() {
        hideAndClearTagInput()
    }

    override fun onTagInputConfirm() {
        val tagInput = tagInput.value ?: return
        viewModelScope.launch {
            val name = tagInput.name.trim()
            if (name.isEmpty()) {
                savedStateHandle[NEW_TAG_ERROR] = UiText.StringResource(
                    R.string.error_invalid_tag_name,
                    isErrorText = true
                )
                return@launch
            }

            val color = tagInput.color

            val insertedId = tagsRepo.saveTag(
                name = name,
                color = color,
                id = tagInput.id,
                timestamp = tagInput.createdTimestamp,
                excluded = tagInput.excluded
            )
            savedStateHandle[SELECTED_TAG] = tagInput
                .copy(id = insertedId)
            hideAndClearTagInput()
            eventBus.send(
                AllExpenseEvent.ShowUiMessage(UiText.StringResource(R.string.tag_saved))
            )
        }
    }

    private fun hideAndClearTagInput() {
        savedStateHandle[SHOW_TAG_INPUT] = false
        savedStateHandle[TAG_INPUT] = null
    }

    override fun onToggleShowExcludedExpenses(value: Boolean) {
        viewModelScope.launch {
            expenseRepo.toggleShowExcludedExpenses(value)
        }
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
        if (expenseMultiSelectionModeActive.value) {
            val selectedIds = selectedExpenseIds.value
            if (id in selectedIds) {
                savedStateHandle[SELECTED_EXPENSE_IDS] = selectedIds - id
                if (selectedExpenseIds.value.isEmpty()) dismissMultiSelectionMode()
            } else {
                savedStateHandle[SELECTED_EXPENSE_IDS] = selectedIds + id
            }
        } else viewModelScope.launch {
            eventBus.send(AllExpenseEvent.NavigateToAddEditExpenseScreen(id))
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

    override fun onExpenseOptionClick(option: ExpenseOption) {
        val selectedExpenseIds = selectedExpenseIds.value.ifEmpty { return }
        viewModelScope.launch {
            when (option) {
                ExpenseOption.DE_TAG -> {
                    deTagExpenses(selectedExpenseIds)
                }

                ExpenseOption.MARK_AS_EXCLUDED -> {
                    toggleExpenseExclusion(selectedExpenseIds, true)
                }

                ExpenseOption.MARK_AS_INCLUDED -> {
                    toggleExpenseExclusion(selectedExpenseIds, false)
                }
            }
        }
    }

    private suspend fun deTagExpenses(ids: List<Long>) {
        tagsRepo.deTagExpenses(ids)
        dismissMultiSelectionMode()
        eventBus.send(AllExpenseEvent.ShowUiMessage(UiText.StringResource(R.string.expenses_de_tagged)))
    }

    private suspend fun toggleExpenseExclusion(ids: List<Long>, excluded: Boolean) {
        expenseRepo.toggleExpenseExclusionByIds(
            ids = ids,
            excluded = excluded
        )
        dismissMultiSelectionMode()
        eventBus.send(
            AllExpenseEvent.ShowUiMessage(
                UiText.StringResource(
                    if (excluded) R.string.expenses_excluded_from_expenditure
                    else R.string.expenses_included_in_expenditure
                )
            )
        )
    }

    override fun onDeleteSelectedExpensesClick() {
        savedStateHandle[SHOW_DELETE_EXPENSE_CONFIRMATION] = true
    }

    override fun onDeleteExpenseDismiss() {
        savedStateHandle[SHOW_DELETE_EXPENSE_CONFIRMATION] = false
    }

    override fun onDeleteExpenseConfirm() {
        val selectedIds = selectedExpenseIds.value
        if (selectedIds.isEmpty()) {
            savedStateHandle[SHOW_DELETE_EXPENSE_CONFIRMATION] = false
            return
        }
        viewModelScope.launch {
            deleteExpenses(selectedIds)
            savedStateHandle[SHOW_DELETE_EXPENSE_CONFIRMATION] = false
            dismissMultiSelectionMode()
            eventBus.send(AllExpenseEvent.ShowUiMessage(UiText.StringResource(R.string.expenses_deleted)))
        }
    }

    private suspend fun deleteExpenses(ids: List<Long>) {
        expenseRepo.deleteExpenses(ids)
    }

    override fun onEditTagClick(tag: ExpenseTag) {
        savedStateHandle[TAG_INPUT] = tag
        savedStateHandle[SHOW_TAG_INPUT] = true
    }

    override fun onDeleteTagClick() {
        savedStateHandle[SHOW_TAG_INPUT] = false
        savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = true
    }

    override fun onDeleteTagDismiss() {
        savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = false
        hideAndClearTagInput()
    }

    override fun onDeleteTagConfirm() {
        val tagId = tagInput.value?.id ?: return
        viewModelScope.launch {
            tagsRepo.deleteTagById(tagId)
            savedStateHandle[SELECTED_TAG] = null
            savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = false
            hideAndClearTagInput()
            eventBus.send(
                AllExpenseEvent.ShowUiMessage(UiText.StringResource(R.string.tag_deleted))
            )
        }
    }

    override fun onDeleteTagWithExpensesClick() {
        val tagId = tagInput.value?.id ?: return
        viewModelScope.launch {
            tagsRepo.deleteTagWithExpenses(tagId)
            savedStateHandle[SELECTED_TAG] = null
            savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = false
            hideAndClearTagInput()
            eventBus.send(
                AllExpenseEvent.ShowUiMessage(UiText.StringResource(R.string.tag_deleted_with_expenses))
            )
        }
    }

    sealed class AllExpenseEvent {
        data class NavigateToAddEditExpenseScreen(val expenseId: Long) : AllExpenseEvent()
        data class ShowUiMessage(val uiText: UiText) : AllExpenseEvent()
        data class ProvideHapticFeedback(val type: HapticFeedbackType) : AllExpenseEvent()
    }
}

private const val SELECTED_DATE = "SELECTED_DATE"
private const val SELECTED_TAG = "SELECTED_TAG"
private const val SELECTED_EXPENSE_IDS = "SELECTED_EXPENSE_IDS"
private const val EXPENSE_MULTI_SELECTION_MODE_ACTIVE = "EXPENSE_MULTI_SELECTION_MODE_ACTIVE"
private const val SHOW_DELETE_EXPENSE_CONFIRMATION = "SHOW_DELETE_EXPENSE_CONFIRMATION"
private const val SHOW_DELETE_TAG_CONFIRMATION = "SHOW_DELETE_TAG_CONFIRMATION"
private const val SHOW_TAG_INPUT = "SHOW_TAG_INPUT"
private const val TAG_INPUT = "TAG_INPUT"
private const val NEW_TAG_ERROR = "NEW_TAG_ERROR"