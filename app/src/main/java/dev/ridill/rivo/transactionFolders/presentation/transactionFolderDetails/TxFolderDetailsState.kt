package dev.ridill.rivo.transactionFolders.presentation.transactionFolderDetails

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.CurrencyUtil
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

data class TxFolderDetailsState(
    val folderId: Long = Long.Zero,
    val editModeActive: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val createdTimestamp: LocalDateTime = DateUtil.now(),
    val isExcluded: Boolean = false,
    val currency: Currency = CurrencyUtil.default,
    val aggregateAmount: Double = Double.Zero,
    val aggregateType: TransactionType? = null,
    val transactions: List<TransactionListItem> = emptyList()
) {
    val createdTimestampFormatted: String
        get() = createdTimestamp.format(DateUtil.Formatters.localizedDateMedium)
}