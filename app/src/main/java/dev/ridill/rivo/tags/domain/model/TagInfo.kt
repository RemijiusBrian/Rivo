package dev.ridill.rivo.tags.domain.model

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.util.TextFormat
import java.time.LocalDateTime
import java.util.Currency

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

    fun amountFormatted(currency: Currency): String =
        TextFormat.currency(aggregate, currency)
}