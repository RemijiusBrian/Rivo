package dev.ridill.rivo.folders.domain.model

import dev.ridill.rivo.core.domain.util.DateUtil
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
        get() = AggregateType.fromAmount(aggregateAmount)
}