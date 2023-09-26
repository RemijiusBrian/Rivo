package dev.ridill.rivo.transactionGroups.presentation.groupDetails

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.transactions.domain.model.Transaction
import java.time.LocalDateTime

data class TxGroupDetailsState(
    val editModeActive: Boolean = false,
    val createdTimestamp: LocalDateTime = DateUtil.now(),
    val isExcluded: Boolean = false,
    val transactions: List<Transaction> = emptyList()
) {
    val createdTimestampFormatted: String
        get() = createdTimestamp.format(DateUtil.Formatters.localizedDateMedium)
}