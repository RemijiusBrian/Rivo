package dev.ridill.rivo.transactions.presentation.allTransactions

import android.icu.util.Currency
import androidx.compose.ui.state.ToggleableState
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.transactions.domain.model.TagInfo
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDate

data class AllTransactionsState(
    val selectedDate: LocalDate = DateUtil.now().toLocalDate(),
    val yearsList: List<Int> = emptyList(),
    val totalAmount: Double = Double.Zero,
    val currency: Currency = LocaleUtil.defaultCurrency,
    val tagsWithExpenditures: List<TagInfo> = emptyList(),
    val selectedTagId: Long? = null,
    val selectedTransactionTypeFilter: TransactionType? = null,
    val transactionListLabel: UiText = UiText.DynamicString(String.Empty),
    val transactionList: List<TransactionListItem> = emptyList(),
    val selectedTransactionIds: Set<Long> = emptySet(),
    val transactionSelectionState: ToggleableState = ToggleableState.Off,
    val transactionMultiSelectionModeActive: Boolean = false,
    val showDeleteTransactionConfirmation: Boolean = false,
    val showDeleteTagConfirmation: Boolean = false,
    val showTagInput: Boolean = false,
    val tagInputError: UiText? = null,
    val showExcludedTransactions: Boolean = false,
    val showFolderSelection: Boolean = false
)