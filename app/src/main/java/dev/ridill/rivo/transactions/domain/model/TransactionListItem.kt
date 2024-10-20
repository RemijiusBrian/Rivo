package dev.ridill.rivo.transactions.domain.model

import androidx.compose.runtime.Composable
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.tags.domain.model.Tag
import java.time.LocalDate
import java.time.LocalDateTime

data class TransactionListItem(
    val id: Long,
    val note: String,
    val amount: Double,
    val timestamp: LocalDateTime,
    val type: TransactionType,
    val isTransactionExcluded: Boolean,
    val tag: Tag?,
    val folder: Folder?,
    val scheduleId: Long?
) {
    val date: LocalDate
        get() = timestamp.toLocalDate()

    val excluded: Boolean
        get() = isTransactionExcluded
                || tag?.excluded == true
                || folder?.excluded == true

    val amountFormatted: String
        @Composable
        get() = TextFormat.currencyAmount(amount)
}