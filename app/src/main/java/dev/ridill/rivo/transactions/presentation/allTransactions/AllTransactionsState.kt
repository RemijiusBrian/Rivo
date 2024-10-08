package dev.ridill.rivo.transactions.presentation.allTransactions

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TransactionTypeFilter
import java.time.LocalDate

data class AllTransactionsState(
    val dateLimits: Pair<LocalDate, LocalDate> = DateUtil.dateNow() to DateUtil.dateNow(),
    val selectedDateRange: Pair<LocalDate, LocalDate>? = null,
    val selectedTransactionTypeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val aggregateAmount: Double = Double.Zero,
    val transactionListLabel: UiText = UiText.DynamicString(String.Empty),
    val selectedTransactionIds: Set<Long> = emptySet(),
    val transactionMultiSelectionModeActive: Boolean = false,
    val showDeleteTransactionConfirmation: Boolean = false,
    val showExcludedTransactions: Boolean = false,
    val selectedTagFilters: List<Tag> = emptyList(),
    val showAggregationConfirmation: Boolean = false,
    val showMultiSelectionOptions: Boolean = false,
    val showFilterOptions: Boolean = false
)