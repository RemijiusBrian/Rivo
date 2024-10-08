package dev.ridill.rivo.transactions.presentation.allTransactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.addOrRemove
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.navigation.destinations.AddEditTxResult
import dev.ridill.rivo.core.ui.navigation.destinations.NavDestination
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.tags.domain.repository.TagsRepository
import dev.ridill.rivo.transactions.domain.model.AllTransactionsMultiSelectionOption
import dev.ridill.rivo.transactions.domain.model.TransactionTypeFilter
import dev.ridill.rivo.transactions.domain.repository.AllTransactionsRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AllTransactionsViewModel @Inject constructor(
    private val transactionRepo: AllTransactionsRepository,
    private val tagsRepo: TagsRepository,
    private val savedStateHandle: SavedStateHandle,
    private val eventBus: EventBus<AllTransactionsEvent>
) : ViewModel(), AllTransactionsActions {

    private val dateLimits = transactionRepo.getDateLimits()
    private val selectedDateRange = savedStateHandle
        .getStateFlow<Pair<LocalDate, LocalDate>?>(SELECTED_DATE_RANGE, null)

    val tagInfoPagingData = selectedDateRange.flatMapLatest {
        tagsRepo.getTopTagInfoPagingData(
            dateRange = it,
            limit = 5
        )
    }.cachedIn(viewModelScope)

    private val transactionTypeFilter = savedStateHandle
        .getStateFlow(TRANSACTION_TYPE_FILTER, TransactionTypeFilter.ALL)

    private val showExcludedTransactions = transactionRepo.getShowExcludedOption()

    private val selectedTagIds = savedStateHandle
        .getStateFlow<Set<Long>>(SELECTED_TAG_IDS, emptySet())
    private val selectedTags = selectedTagIds.flatMapLatest { ids ->
        tagsRepo.getTagsListFlowByIds(ids)
    }

    private val selectedTransactionIds = savedStateHandle
        .getStateFlow<Set<Long>>(SELECTED_TRANSACTION_IDS, emptySet())
    private val transactionMultiSelectionModeActive = selectedTransactionIds
        .map { it.isNotEmpty() }
        .distinctUntilChanged()

    val transactionsPagingData = combineTuple(
        selectedDateRange,
        transactionTypeFilter,
        showExcludedTransactions,
        selectedTagIds
    ).flatMapLatest { (
                          dateRange,
                          typeFilter,
                          showExcluded,
                          tagIds
                      ) ->
        transactionRepo.getAllTransactionsPaged(
            dateRange = dateRange,
            transactionType = TransactionTypeFilter.mapToTransactionType(typeFilter),
            showExcluded = showExcluded,
            tagIds = tagIds
        )
    }.cachedIn(viewModelScope)

    private val aggregateAmount = combineTuple(
        selectedDateRange,
        transactionTypeFilter,
        selectedTagIds,
        showExcludedTransactions,
        selectedTransactionIds
    )
        /*.filter { (dateRange, _, _, _, _) ->
            dateRange != null
        }*/
        .flatMapLatest { (
                             dateRange,
                             typeFilter,
                             selectedTagIds,
                             addExcluded,
                             selectedTxIds
                         ) ->
            transactionRepo.getAmountAggregate(
                dateRange = dateRange,
                type = TransactionTypeFilter.mapToTransactionType(typeFilter),
                tagIds = selectedTagIds,
                addExcluded = addExcluded,
                selectedTxIds = selectedTxIds
            )
        }
        .onEmpty { emit(Double.Zero) }
        .distinctUntilChanged()

    private val transactionListLabel = transactionTypeFilter.mapLatest { type ->
        when {
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
        dateLimits,
        selectedDateRange,
        transactionTypeFilter,
        aggregateAmount,
        transactionListLabel,
        selectedTransactionIds,
        transactionMultiSelectionModeActive,
        showDeleteTransactionConfirmation,
        showExcludedTransactions,
        showAggregationConfirmation,
        showMultiSelectionOptions,
        showFilterOptions,
        selectedTags
    ).map { (
                dateLimits,
                selectedDateRange,
                transactionTypeFilter,
                aggregateAmount,
                transactionListLabel,
                selectedTransactionIds,
                transactionMultiSelectionModeActive,
                showDeleteTransactionConfirmation,
                showExcludedTransactions,
                showAggregationConfirmation,
                showMultiSelectionOptions,
                showFilterOptions,
                selectedTags
            ) ->
        AllTransactionsState(
            dateLimits = dateLimits,
            selectedDateRange = selectedDateRange,
            selectedTransactionTypeFilter = transactionTypeFilter,
            aggregateAmount = aggregateAmount,
            transactionListLabel = transactionListLabel,
            selectedTransactionIds = selectedTransactionIds,
            transactionMultiSelectionModeActive = transactionMultiSelectionModeActive,
            showDeleteTransactionConfirmation = showDeleteTransactionConfirmation,
            showExcludedTransactions = showExcludedTransactions,
            showAggregationConfirmation = showAggregationConfirmation,
            showMultiSelectionOptions = showMultiSelectionOptions,
            showFilterOptions = showFilterOptions,
            selectedTagFilters = selectedTags
        )
    }.asStateFlow(viewModelScope, AllTransactionsState())

    val events = eventBus.eventFlow

    init {
        keepSelectedDateRangeUpdated()
    }

    private fun keepSelectedDateRangeUpdated() = viewModelScope.launch {
        dateLimits.collectLatest { (minDate, maxDate) ->
            val selectedRange = selectedDateRange.value
            val selectedMin = selectedRange?.first
            val selectedMax = selectedRange?.second

            savedStateHandle[SELECTED_DATE_RANGE] = selectedMin?.coerceAtLeast(minDate)
                ?.to(selectedMax?.coerceAtLeast(maxDate))
        }
    }

    override fun onStartDateSelect(date: LocalDate) {
        savedStateHandle[SELECTED_DATE_RANGE] = selectedDateRange.value?.copy(first = date)
    }

    override fun onEndDateSelect(date: LocalDate) {
        savedStateHandle[SELECTED_DATE_RANGE] = selectedDateRange.value?.copy(second = date)
    }

    override fun onDateRangeClear() {
        savedStateHandle[SELECTED_DATE_RANGE] = null
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
        savedStateHandle[SELECTED_TRANSACTION_IDS] = selectedTransactionIds.value + id
    }

    override fun onTransactionSelectionChange(id: Long) {
        savedStateHandle[SELECTED_TRANSACTION_IDS] = selectedTransactionIds.value.addOrRemove(id)
    }

    override fun onDismissMultiSelectionMode() {
        dismissMultiSelectionMode()
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
                    savedStateHandle[TAG_RESULT_PURPOSE] = TAG_RESULT_FOR_ASSIGNMENT
                    eventBus.send(AllTransactionsEvent.NavigateToTagSelection(false, emptySet()))
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

    fun onTagSelectionResult(ids: Set<Long>) = viewModelScope.launch {
        when (savedStateHandle.get<String>(TAG_RESULT_PURPOSE)) {
            TAG_RESULT_FOR_ASSIGNMENT -> {
                ids.firstOrNull()
                    ?.let { assignTagToTransactions(it) }
            }

            TAG_RESULT_FOR_FILTER -> {
                savedStateHandle[SELECTED_TAG_IDS] = ids
            }

            else -> error("Invalid tag result purpose")
        }
    }

    private suspend fun assignTagToTransactions(selectedId: Long) {
        transactionRepo.setTagIdToTransactions(selectedId, selectedTransactionIds.value)
        dismissMultiSelectionMode()
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

    override fun onChangeTagFiltersClick() {
        viewModelScope.launch {
            savedStateHandle[TAG_RESULT_PURPOSE] = TAG_RESULT_FOR_FILTER
            eventBus.send(AllTransactionsEvent.NavigateToTagSelection(true, selectedTagIds.value))
        }
    }

    override fun onClearTagFilterClick() {
        savedStateHandle[SELECTED_TAG_IDS] = emptySet<Long>()
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
        if (folderId == NavDestination.ARG_INVALID_ID_LONG) return
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

    fun onAddEditTxNavResult(result: AddEditTxResult) = viewModelScope.launch {
        val event = when (result) {
            AddEditTxResult.TRANSACTION_DELETED ->
                AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.transaction_deleted))

            AddEditTxResult.TRANSACTION_SAVED ->
                AllTransactionsEvent.ShowUiMessage(UiText.StringResource(R.string.transaction_saved))

            AddEditTxResult.SCHEDULE_SAVED -> AllTransactionsEvent.ScheduleSaved
        }

        eventBus.send(event)
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
            val dateTimeNow = DateUtil.now()
            transactionRepo.aggregateIntoSingleNewTransactions(
                ids = selectedIds,
                dateTime = dateTimeNow
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
        data class NavigateToTagSelection(
            val multiSelection: Boolean,
            val preSelectedIds: Set<Long>
        ) : AllTransactionsEvent

        data object NavigateToFolderSelection : AllTransactionsEvent
        data object ScheduleSaved : AllTransactionsEvent
    }
}

private const val SELECTED_DATE_RANGE = "SELECTED_DATE_RANGE"
private const val TRANSACTION_TYPE_FILTER = "TRANSACTION_TYPE_FILTER"
private const val SELECTED_TAG_IDS = "SELECTED_TAG_IDS"
private const val SELECTED_TRANSACTION_IDS = "SELECTED_TRANSACTION_IDS"
private const val SHOW_DELETE_TRANSACTION_CONFIRMATION = "SHOW_DELETE_TRANSACTION_CONFIRMATION"
private const val SHOW_AGGREGATION_CONFIRMATION = "SHOW_AGGREGATION_CONFIRMATION"
private const val SHOW_MULTI_SELECTION_OPTIONS = "SHOW_MULTI_SELECTION_OPTIONS"
private const val SHOW_FILTER_OPTIONS = "SHOW_FILTER_OPTIONS"

private const val TAG_RESULT_PURPOSE = "TAG_RESULT_PURPOSE"
private const val TAG_RESULT_FOR_ASSIGNMENT = "TAG_RESULT_FOR_ASSIGNMENT"
private const val TAG_RESULT_FOR_FILTER = "TAG_RESULT_FOR_FILTER"