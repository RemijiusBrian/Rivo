package dev.ridill.rivo.folders.domain.model

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Zero
import java.time.LocalDateTime

data class FolderDetails(
    val id: Long,
    val name: String,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean,
    val aggregateAmount: Double
) {
    val createdDateFormatted: String
        get() = createdTimestamp.format(DateUtil.Formatters.localizedDateMedium)

    val aggregateType: AggregateType
        get() = if (aggregateAmount == Double.Zero) AggregateType.BALANCED
        else if (aggregateAmount < Double.Zero) AggregateType.AGG_CREDIT
        else AggregateType.AGG_DEBIT
}