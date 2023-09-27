package dev.ridill.rivo.transactionGroups.presentation.groupDetails

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.CurrencyUtil
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.transactions.domain.model.TransactionDirection
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import java.time.LocalDateTime

data class TxGroupDetailsState(
    val groupId: Long = Long.Zero,
    val editModeActive: Boolean = false,
    val createdTimestamp: LocalDateTime = DateUtil.now(),
    val isExcluded: Boolean = false,
    val currency: Currency = CurrencyUtil.default,
    val aggregateAmount: Double = Double.Zero,
    val aggregateDirection: TransactionDirection? = null,
    val transactions: List<TransactionListItem> = emptyList()
) {
    val createdTimestampFormatted: String
        get() = createdTimestamp.format(DateUtil.Formatters.localizedDateMedium)
}