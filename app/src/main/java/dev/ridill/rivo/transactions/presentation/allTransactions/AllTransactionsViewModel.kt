package dev.ridill.rivo.transactions.presentation.allTransactions

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.state.ToggleableState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.addOrRemove
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.tags.domain.repository.TagsRepository
import dev.ridill.rivo.transactions.domain.model.TransactionTypeFilter
import dev.ridill.rivo.transactions.domain.repository.AllTransactionsRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
        .getStateFlow(SELECTED_DATE, DateUtil.dateNow())

    private val yearsList = transactionRepo.getTransactionYearsList()

    private val currency = selectedDate.flatMapLatest {
        transactionRepo.getCurrencyPreference(it)
    }.distinctUntilChanged()

    val tagInfoPagingData = selectedDate.flatMapLatest {
        tagsRepo.getTopTagInfoPagingData(
            date = it,
            limit = 5
        )
    }.cachedIn(viewModelScope)

    private val transactionTypeFilter = savedStateHandle
        .getStateFlow(TRANSACTION_TYPE_FILTER, TransactionTypeFilter.ALL)

    private val showExcludedTransactions = transactionRepo.getShowExcludedOption()

    private val selectedTagId = savedStateHandle.getStateFlow<Long?>(SELECTED_TAG_ID, null)
    private val selectedTag = selectedTagId.flatMapLatest { tagId ->
        tagId?.let(tagsRepo::getTagByIdFlow)
            ?: flowOf(null)
    }.distinctUntilChanged()

    private val transactionList = combineTuple(
        selectedDate,
        selectedTag,
        transactionTypeFilter,
        showExcludedTransactions
    ).flatMapLatest { (
                          date,
                          selectedTag,
                          typeFilter,
                          showExcluded
                      ) ->
        transactionRepo.getAllTransactionsList(
            date = date,
            tagId = selectedTag?.id,
            transactionType = TransactionTypeFilter.mapToTransactionType(typeFilter),
            showExcluded = selectedTag?.excluded == true || showExcluded
        )
    }.asStateFlow(viewModelScope, emptyList())

    private val selectedTransactionIds = savedStateHandle
        .getStateFlow<Set<Long>>(SELECTED_TRANSACTION_IDS, emptySet())
    private val transactionMultiSelectionModeActive = selectedTransactionIds
        .map { it.isNotEmpty() }
        .distinctUntilChanged()

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

    private val aggregateAmount = combineTuple(
        selectedDate,
        transactionTypeFilter,
        selectedTag,
        showExcludedTransactions,
        selectedTransactionIds
    ).flatMapLatest { (
                          date,
                          typeFilter,
                          selectedTag,
                          addExcluded,
                          selectedTxIds
                      ) ->
        transactionRepo.getAmountAggregate(
            date = date,
            type = TransactionTypeFilter.mapToTransactionType(typeFilter),
            tagId = selectedTag?.id,
            addExcluded = selectedTag?.excluded == true || addExcluded,
            selectedTxIds = selectedTxIds.ifEmpty { null }
        )
    }.distinctUntilChanged()

    private val transactionListLabel = combineTuple(
        selectedTag,
        transactionTypeFilter
    ).map { (tag, type) ->
        when {
            tag != null -> UiText.DynamicString(tag.name)
            type == TransactionTypeFilter.ALL -> UiText.StringResource(R.string.all_transactions)
            else -> UiText.StringResource(type.labelRes)
        }
    }.distinctUntilChanged()


    private val showDeleteTransactionConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_TRANSACTION_CONFIRMATION, false)

    private val showAggregationConfirmation = savedStateHandle
        .getStateFlow(SHOW_AGGREGATION_CONFIRMATION, false)

    private val showMultiSelectionOptions = savedStateHandle
        .getStateFlow(SHOW_MULTI_SELECTION_OPTIONS, false)

    private val showFilterOptions = savedStateHandle
        .getStateFlow(SHOW_FILTER_OPTIONS, false)

    val state = combineTuple(
        selectedDate,
        yearsList,
        transactionTypeFilter,
        currency,
        aggregateAmount,
        transactionListLabel,
        transactionList,
        selectedTransactionIds,
        transactionSelectionState,
        transactionMultiSelectionModeActive,
        showDeleteTransactionConfirmation,
        showExcludedTransactions,
        showAggregationConfirmation,
        showMultiSelectionOptions,
        showFilterOptions
    ).map { (
                selectedDate,
                yearsList,
                transactionTypeFilter,
                currency,
                aggregateAmount,
                transactionListLabel,
                transactionList,
                selectedTransactionIds,
                transactionSelectionState,
                transactionMultiSelectionModeActive,
                showDeleteTransactionConfirmation,
                showExcludedTransactions,
                showAggregationConfirmation,
                showMultiSelectionOptions,
                showFilterOptions
            ) ->
        AllTransactionsState(
            selectedDate = selectedDate,
            yearsList = yearsList,
            selectedTransactionTypeFilter = transactionTypeFilter,
            currency = currency,
            aggregateAmount = aggregateAmount,
            transactionListLabel = transactionListLabel,
            transactionList = transactionList,
            selectedTransactionIds = selectedTransactionIds,
            transactionSelectionState = transactionSelectionState,
            transactionMultiSelectionModeActive = transactionMultiSelectionModeActive,
            showDeleteTransactionConfirmation = showDeleteTransactionConfirmation,
            showExcludedTransactions = showExcludedTransactions,
            showAggregationConfirmation = showAggregationConfirmation,
            showMultiSelectionOptions = showMultiSelectionOptions,
            showFilterOptions = showFilterOptions
        )
    }.asStateFlow(viewModelScope, AllTransactionsState())

    val events = eventBus.eventFlow

    init {
        refreshSelectedIdsListOnTransactionListChange()
        keepSelectedDateUpdated()
    }

    private fun refreshSelectedIdsListOnTransactionListChange() = viewModelScope.launch {
        transactionList.collectLatest { list ->
            val ids = list.map { it.id }
            savedStateHandle[SELECTED_TRANSACTION_IDS] = selectedTransactionIds.value
                .filter { it in ids }
                .toSet()
        }
    }

    private fun keepSelectedDateUpdated() = viewModelScope.launch {
        yearsList.collectLatest { yearsList ->
            val selectedDate = selectedDate.value
            if (selectedDate.year !in yearsList) {
                savedStateHandle[SELECTED_DATE] = DateUtil.dateNow()
                    .withMonth(selectedDate.monthValue)
            }
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

    override fun onTypeFilterSelect(filter: TransactionTypeFilter) {
        savedStateHandle[TRANSACTION_TYPE_FILTER] = filter
    }

    override fun onShowExcludedToggle(showExcluded: Boolean) {
        viewModelScope.launch {
            transactionRepo.toggleShowExcludedOption(showExcluded)
        }
    }

    override fun onTransactionLongPress(id: Long) {
        viewModelScope.launch {
            savedStateHandle[SELECTED_TRANSACTION_IDS] = selectedTransactionIds.value + id
            eventBus.send(AllTransactionsEvent.ProvideHapticFeedback(HapticFeedbackType.LongPress))
        }
    }

    override fun onTransactionSelectionChange(id: Long) {
        savedStateHandle[SELECTED_TRANSACTION_IDS] = selectedTransactionIds.value.addOrRemove(id)
    }

    override fun onSelectionStateChange() {
        when (transactionSelectionState.value) {
            ToggleableState.On -> {
                savedStateHandle[SELECTED_TRANSACTION_IDS] = emptySet<Long>()
            }

            else -> {
                savedStateHandle[SELECTED_TRANSACTION_IDS] = transactionList.value
                    .map { it.id }
                    .toSet()
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
        savedStateHandle[SELECTED_TRANSACTION_IDS] = emptySet<Long>()
    }

    override fun onMultiSelectionOptionsClick() {
        savedStateHandle[SHOW_MULTI_SELECTION_OPTIONS] = true
    }

    override fun onMultiSelectionOptionsDismiss() {
        savedStateHandle[SHOW_MULTI_SELECTION_OPTIONS] = false
    }

    override fun onMultiSelectionOptionSelect(option: AllTransactionsMultiSelectionOption) {
        savedStateHandle[SHOW_MULTI_SELECTION_OPTIONS] = false
        val selectedTransactionIds = selectedTransactionIds.value.ifEmpty { return }
        viewModelScope.launch {
            when (option) {
                AllTransactionsMultiSelectionOption.DELETE -> {
                    savedStateHandle[SHOW_DELETE_TRANSACTION_CONFIRMATION] = true
                }

                AllTransactionsMultiSelectionOption.ASSIGN_TAG -> {
                    eventBus.send(AllTransactionsEvent.NavigateToTagSelection(false))
                }

                AllTransactionsMultiSelectionOption.REMOVE_TAG -> {
                    removeTagForTransactions(selectedTransactionIds)
                }

                AllTransactionsMultiSelectionOption.EXCLUDE_FROM_EXPENDITURE -> {
                    toggleTransactionExclusion(selectedTransactionIds, true)
                }

                AllTransactionsMultiSelectionOption.INCLUDE_IN_EXPENDITURE -> {
                    toggleTransactionExclusion(selectedTransactionIds, false)
                }

                AllTransactionsMultiSelectionOption.ADD_TO_FOLDER -> {
                    showFolderSelection()
                }

                AllTransactionsMultiSelectionOption.REMOVE_FROM_FOLDERS -> {
                    removeTransactionsFromFolders(selectedTransactionIds)
                }

                AllTransactionsMultiSelectionOption.AGGREGATE_TOGETHER -> {
                    savedStateHandle[SHOW_AGGREGATION_CONFIRMATION] = true
                }
            }
        }
    }

    fun onTagSelectionResultToAssignTag(selectedId: Long) = viewModelScope.launch {
        transactionRepo.setTagIdToTransactions(selectedId, selectedTransactionIds.value)
        dismissMultiSelectionMode()
        savedStateHandle[SELECTED_TAG_ID] = selectedId
        eventBus.send(AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.tag_assigned_to_transactions)))
    }

    private suspend fun removeTagForTransactions(ids: Set<Long>) {
        transactionRepo.setTagIdToTransactions(null, ids)
        dismissMultiSelectionMode()
        eventBus.send(AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.tag_removed_from_transactions)))
    }

    private suspend fun toggleTransactionExclusion(ids: Set<Long>, excluded: Boolean) {
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

    private suspend fun showFolderSelection() {
        eventBus.send(AllTransactionsEvent.NavigateToFolderSelection)
    }

    private suspend fun removeTransactionsFromFolders(ids: Set<Long>) {
        transactionRepo.removeTransactionsFromFolders(ids)
        dismissMultiSelectionMode()
        eventBus.send(AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.transactions_removed_from_their_folders)))
    }

    fun onFolderSelect(folderId: Long) {
        viewModelScope.launch {
            val selectedIds = selectedTransactionIds.value
            transactionRepo.addTransactionsToFolderByIds(
                ids = selectedIds,
                folderId = folderId
            )
            eventBus.send(
                AllTransactionsEvent.ShowUiMessage(
                    UiText.PluralResource(
                        R.plurals.transaction_added_to_folder_message,
                        selectedIds.size
                    )
                )
            )
            dismissMultiSelectionMode()
        }
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

    private suspend fun deleteTransactions(ids: Set<Long>) {
        transactionRepo.deleteTransactionsByIds(ids)
    }

    override fun onAggregationDismiss() {
        savedStateHandle[SHOW_AGGREGATION_CONFIRMATION] = false
    }

    override fun onAggregationConfirm() {
        viewModelScope.launch {
            val selectedIds = selectedTransactionIds.value
            val selectedDate = selectedDate.value
            val dateTimeNow = DateUtil.now()
            transactionRepo.aggregateIntoSingleNewTransactions(
                ids = selectedIds,
                dateTime = dateTimeNow
                    .withMonth(selectedDate.monthValue)
                    .withYear(selectedDate.year)
            )
            savedStateHandle[SHOW_AGGREGATION_CONFIRMATION] = false
            eventBus.send(AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.aggregation_successful)))
        }
    }

    override fun onFilterOptionsClick() {
        savedStateHandle[SHOW_FILTER_OPTIONS] = true
    }

    override fun onFilterOptionsDismiss() {
        savedStateHandle[SHOW_FILTER_OPTIONS] = false
    }

    sealed interface AllTransactionsEvent {
        data class ShowUiMessage(val uiText: UiText) : AllTransactionsEvent
        data class ProvideHapticFeedback(val type: HapticFeedbackType) : AllTransactionsEvent
        data class NavigateToTagSelection(val multiSelection: Boolean) : AllTransactionsEvent
        data object NavigateToFolderSelection : AllTransactionsEvent
        data class NavigateToFolderDetailsWithIds(val transactionIds: Set<Long>) :
            AllTransactionsEvent
    }
}

private const val SELECTED_DATE = "SELECTED_DATE"
private const val TRANSACTION_TYPE_FILTER = "TRANSACTION_TYPE_FILTER"
private const val SELECTED_TAG_ID = "SELECTED_TAG_ID"
private const val SELECTED_TRANSACTION_IDS = "SELECTED_TRANSACTION_IDS"
private const val SHOW_DELETE_TRANSACTION_CONFIRMATION = "SHOW_DELETE_TRANSACTION_CONFIRMATION"
private const val SHOW_AGGREGATION_CONFIRMATION = "SHOW_AGGREGATION_CONFIRMATION"
private const val SHOW_MULTI_SELECTION_OPTIONS = "SHOW_MULTI_SELECTION_OPTIONS"
private const val SHOW_FILTER_OPTIONS = "SHOW_FILTER_OPTIONS"