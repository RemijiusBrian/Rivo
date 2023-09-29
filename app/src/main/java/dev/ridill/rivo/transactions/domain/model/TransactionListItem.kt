package dev.ridill.rivo.transactions.domain.model

import android.icu.util.Currency
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolder
import java.time.LocalDate

data class TransactionListItem(
    val id: Long,
    val note: String,
    val amount: Double,
    val date: LocalDate,
    val type: TransactionType,
    val tag: TransactionTag?,
    val folder: TransactionFolder?,
    val excluded: Boolean
) {
    fun amountFormattedWithCurrency(currency: Currency): String = TextFormat.compactNumber(
        value = amount,
        currency = currency
    )
}