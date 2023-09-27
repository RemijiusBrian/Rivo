package dev.ridill.rivo.transactionGroups.domain.model

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.transactions.domain.model.TransactionDirection
import java.time.LocalDateTime

data class TxGroupDetails(
    val id: Long,
    val name: String,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean,
    val aggregateAmount: Double
) {
    val createdDateFormatted: String
        get() = createdTimestamp.format(DateUtil.Formatters.localizedDateMedium)

    val aggregateDirection: TransactionDirection?
        get() = if (aggregateAmount == Double.Zero) null
        else if (aggregateAmount < Double.Zero) TransactionDirection.INCOMING
        else TransactionDirection.OUTGOING
}