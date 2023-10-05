package dev.ridill.rivo.folders.domain.model

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

data class TransactionFolderDetails(
    val id: Long,
    val name: String,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean,
    val aggregateAmount: Double
) {
    val createdDateFormatted: String
        get() = createdTimestamp.format(DateUtil.Formatters.localizedDateMedium)

    val aggregateType: TransactionType?
        get() = if (aggregateAmount == Double.Zero) null
        else if (aggregateAmount < Double.Zero) TransactionType.CREDIT
        else TransactionType.DEBIT
}