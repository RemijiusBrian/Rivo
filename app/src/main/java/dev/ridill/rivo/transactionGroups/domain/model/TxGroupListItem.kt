package dev.ridill.rivo.transactionGroups.domain.model

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.util.TextFormat
import java.time.LocalDateTime

data class TxGroupListItem(
    val id: Long,
    val name: String,
    val createdTimestamp: LocalDateTime,
    val aggregateAmount: Double
) {
    val createdDateFormatted: String
        get() = createdTimestamp.format(DateUtil.Formatters.localizedDateMedium)

    val aggregateAmountFormatted: String
        get() = TextFormat.compactNumber(aggregateAmount)
}