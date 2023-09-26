package dev.ridill.rivo.transactions.presentation.allTransactions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.state.ToggleableState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.transactions.domain.model.TransactionOption
import dev.ridill.rivo.transactions.domain.model.TransactionTag
import dev.ridill.rivo.transactions.domain.repository.AllTransactionsRepository
import dev.ridill.rivo.transactions.domain.repository.TagsRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Month
import javax.inject.Inject

@HiltViewModel
class AllTransactionsViewModel @Inject constructor(
    private val transactionRepo: AllTransactionsRepository,
    private val tagsRepo: TagsRepository,
    private val savedStateHandle: SavedStateHandle,
    private val eventBus: EventBus<AllTransactionsEvent>
) : ViewModel(), AllTransactionsActions {

    private val selectedDate = savedStateHandle
        .getStateFlow(SELECTED_DATE, DateUtil.now().toLocalDate())

    private val yearsList = transactionRepo.getTransactionYearsList()

    private val totalExpenditure = selectedDate.flatMapLatest { date ->
        transactionRepo.getTotalExpenditureForDate(date)
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

    private val selectedTag = savedStateHandle.getStateFlow<TransactionTag?>(SELECTED_TAG, null)

    private val showExcludedTransactions = transactionRepo.getShowExcludedTransactions()

    private val transactionList = combineTuple(
        selectedDate,
        selectedTag,
        showExcludedTransactions
    ).flatMapLatest { (date, tag, showExcluded) ->
        transactionRepo.getTransactionsForDateByTag(
            date = date,
            tagId = tag?.id,
            showExcluded = showExcluded
        )
    }.asStateFlow(viewModelScope, emptyList())

    private val selectedTransactionIds = savedStateHandle
        .getStateFlow<List<Long>>(SELECTED_TRANSACTION_IDS, emptyList())

    private val transactionSelectionState = combineTuple(
        transactionList,
        selectedTransactionIds
    ).map { (transactions, selectedIds) ->
        when {
            selectedIds.isEmpty() -> ToggleableState.Off
            transactions.all { it.id in selectedIds } -> ToggleableState.On
            else -> ToggleableState.Indeterminate
        }
    }.distinctUntilChanged()
        .asStateFlow(viewModelScope, ToggleableState.Off)

    private val transactionMultiSelectionModeActive = savedStateHandle
        .getStateFlow(TRANSACTION_MULTI_SELECTION_MODE_ACTIVE, false)

    private val showDeleteTransactionConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_TRANSACTION_CONFIRMATION, false)

    private val showDeleteTagConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_TAG_CONFIRMATION, false)

    private val showTagInput = savedStateHandle
        .getStateFlow(SHOW_TAG_INPUT, false)
    val tagInput = savedStateHandle.getStateFlow<TransactionTag?>(TAG_INPUT, null)
    private val tagInputError = savedStateHandle.getStateFlow<UiText?>(NEW_TAG_ERROR, null)

    val state = combineTuple(
        selectedDate,
        yearsList,
        totalExpenditure,
        tagsWithExpenditures,
        selectedTag,
        transactionList,
        selectedTransactionIds,
        transactionSelectionState,
        transactionMultiSelectionModeActive,
        showDeleteTransactionConfirmation,
        showDeleteTagConfirmation,
        showTagInput,
        tagInputError,
        showExcludedTransactions
    ).map { (
                selectedDate,
                yearsList,
                totalExpenditure,
                tagsWithExpenditures,
                selectedTag,
                transactionList,
                selectedTransactionIds,
                transactionSelectionState,
                transactionMultiSelectionModeActive,
                showDeleteTransactionConfirmation,
                showDeleteTagConfirmation,
                showTagInput,
                tagInputError,
                showExcludedTransactions
            ) ->
        AllTransactionsState(
            selectedDate = selectedDate,
            yearsList = yearsList,
            totalExpenditure = totalExpenditure,
            tagsWithExpenditures = tagsWithExpenditures,
            selectedTag = selectedTag,
            transactionList = transactionList,
            selectedTransactionIds = selectedTransactionIds,
            transactionSelectionState = transactionSelectionState,
            transactionMultiSelectionModeActive = transactionMultiSelectionModeActive,
            showDeleteTransactionConfirmation = showDeleteTransactionConfirmation,
            showDeleteTagConfirmation = showDeleteTagConfirmation,
            showTagInput = showTagInput,
            tagInputError = tagInputError,
            showExcludedTransactions = showExcludedTransactions
        )
    }.asStateFlow(viewModelScope, AllTransactionsState())

    val events = eventBus.eventFlow

    init {
        refreshSelectedIdsListOnTransactionListChange()
    }

    private fun refreshSelectedIdsListOnTransactionListChange() = viewModelScope.launch {
        transactionList.collectLatest { list ->
            val ids = list.map { it.id }
            savedStateHandle[SELECTED_TRANSACTION_IDS] = selectedTransactionIds.value
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

    override fun onTagClick(tag: TransactionTag) {
        if (transactionMultiSelectionModeActive.value) viewModelScope.launch {
            val selectedIds = selectedTransactionIds.value
            tagsRepo.assignTagToTransactions(tag.id, selectedIds)
            dismissMultiSelectionMode()
            savedStateHandle[SELECTED_TAG] = tag
            eventBus.send(AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.tag_assigned_to_transactions)))
        } else {
            savedStateHandle[SELECTED_TAG] = tag
                .takeIf { it != selectedTag.value }
        }
    }

    override fun onNewTagClick() {
        savedStateHandle[TAG_INPUT] = TransactionTag.NEW
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
                AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.tag_saved))
            )
        }
    }

    private fun hideAndClearTagInput() {
        savedStateHandle[SHOW_TAG_INPUT] = false
        savedStateHandle[TAG_INPUT] = null
    }

    override fun onToggleShowExcludedTransactions(value: Boolean) {
        viewModelScope.launch {
            transactionRepo.toggleShowExcludedTransactions(value)
        }
    }

    override fun onTransactionLongClick(id: Long) {
        viewModelScope.launch {
            if (transactionMultiSelectionModeActive.value) dismissMultiSelectionMode()
            else {
                savedStateHandle[SELECTED_TAG] = null
                enableMultiSelectionModeWithId(id)
            }
            eventBus.send(AllTransactionsEvent.ProvideHapticFeedback(HapticFeedbackType.LongPress))
        }
    }

    override fun onTransactionClick(id: Long) {
        if (transactionMultiSelectionModeActive.value) {
            val selectedIds = selectedTransactionIds.value
            if (id in selectedIds) {
                savedStateHandle[SELECTED_TRANSACTION_IDS] = selectedIds - id
                if (selectedTransactionIds.value.isEmpty()) dismissMultiSelectionMode()
            } else {
                savedStateHandle[SELECTED_TRANSACTION_IDS] = selectedIds + id
            }
        } else viewModelScope.launch {
            eventBus.send(AllTransactionsEvent.NavigateToAddEditTransactionScreen(id))
        }
    }

    override fun onSelectionStateChange() {
        when (transactionSelectionState.value) {
            ToggleableState.On -> {
                savedStateHandle[SELECTED_TRANSACTION_IDS] = emptyList<Long>()
            }

            else -> {
                savedStateHandle[SELECTED_TRANSACTION_IDS] = transactionList.value.map { it.id }
            }
        }
    }

    override fun onDismissMultiSelectionMode() {
        viewModelScope.launch {
            dismissMultiSelectionMode()
            eventBus.send(AllTransactionsEvent.ProvideHapticFeedback(HapticFeedbackType.LongPress))
        }
    }

    private fun dismissMultiSelectionMode() {
        savedStateHandle[TRANSACTION_MULTI_SELECTION_MODE_ACTIVE] = false
        savedStateHandle[SELECTED_TRANSACTION_IDS] = emptyList<Long>()
    }

    private fun enableMultiSelectionModeWithId(id: Long) {
        savedStateHandle[SELECTED_TRANSACTION_IDS] = listOf(id)
        savedStateHandle[TRANSACTION_MULTI_SELECTION_MODE_ACTIVE] = true
    }

    override fun onTransactionOptionClick(option: TransactionOption) {
        val selectedTransactionIds = selectedTransactionIds.value.ifEmpty { return }
        viewModelScope.launch {
            when (option) {
                TransactionOption.UNTAG -> {
                    unTagTransactions(selectedTransactionIds)
                }

                TransactionOption.MARK_AS_EXCLUDED -> {
                    toggleTransactionExclusion(selectedTransactionIds, true)
                }

                TransactionOption.MARK_AS_INCLUDED -> {
                    toggleTransactionExclusion(selectedTransactionIds, false)
                }
            }
        }
    }

    private suspend fun unTagTransactions(ids: List<Long>) {
        tagsRepo.untagTransactions(ids)
        dismissMultiSelectionMode()
        eventBus.send(AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.transactions_untagged)))
    }

    private suspend fun toggleTransactionExclusion(ids: List<Long>, excluded: Boolean) {
        transactionRepo.toggleTransactionExclusionByIds(
            ids = ids,
            excluded = excluded
        )
        dismissMultiSelectionMode()
        eventBus.send(
            AllTransactionsEvent.ShowUiMessage(
                UiText.StringResource(
                    if (excluded) R.string.transactions_excluded_from_expenditure
                    else R.string.transactions_included_in_expenditure
                )
            )
        )
    }

    override fun onDeleteSelectedTransactionsClick() {
        savedStateHandle[SHOW_DELETE_TRANSACTION_CONFIRMATION] = true
    }

    override fun onDeleteTransactionDismiss() {
        savedStateHandle[SHOW_DELETE_TRANSACTION_CONFIRMATION] = false
    }

    override fun onDeleteTransactionConfirm() {
        val selectedIds = selectedTransactionIds.value
        if (selectedIds.isEmpty()) {
            savedStateHandle[SHOW_DELETE_TRANSACTION_CONFIRMATION] = false
            return
        }
        viewModelScope.launch {
            deleteTransactions(selectedIds)
            savedStateHandle[SHOW_DELETE_TRANSACTION_CONFIRMATION] = false
            dismissMultiSelectionMode()
            eventBus.send(AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.transactions_deleted)))
        }
    }

    private suspend fun deleteTransactions(ids: List<Long>) {
        transactionRepo.deleteTransactionsByIds(ids)
    }

    override fun onEditTagClick(tag: TransactionTag) {
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
                AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.tag_deleted))
            )
        }
    }

    override fun onDeleteTagWithTransactionsClick() {
        val tagId = tagInput.value?.id ?: return
        viewModelScope.launch {
            tagsRepo.deleteTagWithTransactions(tagId)
            savedStateHandle[SELECTED_TAG] = null
            savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = false
            hideAndClearTagInput()
            eventBus.send(
                AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.tag_deleted_with_transactions))
            )
        }
    }

    sealed class AllTransactionsEvent {
        data class NavigateToAddEditTransactionScreen(val transactionId: Long) :
            AllTransactionsEvent()

        data class ShowUiMessage(val uiText: UiText) : AllTransactionsEvent()
        data class ProvideHapticFeedback(val type: HapticFeedbackType) : AllTransactionsEvent()
    }
}

private const val SELECTED_DATE = "SELECTED_DATE"
private const val SELECTED_TAG = "SELECTED_TAG"
private const val SELECTED_TRANSACTION_IDS = "SELECTED_TRANSACTION_IDS"
private const val TRANSACTION_MULTI_SELECTION_MODE_ACTIVE =
    "TRANSACTION_MULTI_SELECTION_MODE_ACTIVE"
private const val SHOW_DELETE_TRANSACTION_CONFIRMATION = "SHOW_DELETE_TRANSACTION_CONFIRMATION"
private const val SHOW_DELETE_TAG_CONFIRMATION = "SHOW_DELETE_TAG_CONFIRMATION"
private const val SHOW_TAG_INPUT = "SHOW_TAG_INPUT"
private const val TAG_INPUT = "TAG_INPUT"
private const val NEW_TAG_ERROR = "NEW_TAG_ERROR"