package dev.ridill.rivo.tags.domain.model

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.folders.domain.model.AggregateType
import java.time.LocalDateTime

data class TagInfo(
    val id: Long,
    val name: String,
    val color: Color,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean,
    val aggregate: Double
) {
    val createdTimestampFormatted: String
        get() = createdTimestamp.format(DateUtil.Formatters.localizedDateMedium)

    val aggregateType: AggregateType
        get() = AggregateType.fromAmount(aggregate)
}