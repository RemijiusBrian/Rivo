package dev.ridill.rivo.transactions.domain.model

import android.icu.util.Currency
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolder
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
    val folder: TransactionFolder?
) {
    val date: LocalDate
        get() = timestamp.toLocalDate()

    val excluded: Boolean
        get() = isTransactionExcluded
                || tag?.excluded == true
                || folder?.excluded == true

    fun amountFormattedWithCurrency(currency: Currency): String = TextFormat.compactNumber(
        value = amount,
        currency = currency
    )
}