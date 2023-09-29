package dev.ridill.rivo.transactions.presentation.allTransactions

import android.icu.util.Currency
import androidx.compose.ui.state.ToggleableState
import dev.ridill.rivo.core.domain.util.CurrencyUtil
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolder
import dev.ridill.rivo.transactions.domain.model.TagWithExpenditure
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.Tag
import java.time.LocalDate

data class AllTransactionsState(
    val selectedDate: LocalDate = DateUtil.now().toLocalDate(),
    val yearsList: List<Int> = emptyList(),
    val totalExpenditure: Double = Double.Zero,
    val currency: Currency = CurrencyUtil.default,
    val tagsWithExpenditures: List<TagWithExpenditure> = emptyList(),
    val selectedTag: Tag? = null,
    val transactionList: List<TransactionListItem> = emptyList(),
    val selectedTransactionIds: List<Long> = emptyList(),
    val transactionSelectionState: ToggleableState = ToggleableState.Off,
    val transactionMultiSelectionModeActive: Boolean = false,
    val showDeleteTransactionConfirmation: Boolean = false,
    val showDeleteTagConfirmation: Boolean = false,
    val showTagInput: Boolean = false,
    val tagInputError: UiText? = null,
    val showExcludedTransactions: Boolean = false,
    val showFolderSelection: Boolean = false,
    val foldersList: List<TransactionFolder> = emptyList()
)