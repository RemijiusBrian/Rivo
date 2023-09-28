package dev.ridill.rivo.transactions.presentation.addEditTransaction

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.CurrencyUtil
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.model.TransactionTag
import java.time.LocalDateTime

data class AddEditTransactionState(
    val currency: Currency = CurrencyUtil.default,
    val amountRecommendations: List<Long> = emptyList(),
    val tagsList: List<TransactionTag> = emptyList(),
    val selectedTagId: Long? = null,
    val transactionTimestamp: LocalDateTime = DateUtil.now(),
    val transactionFolderId: Long? = null,
    val transactionType: TransactionType = TransactionType.DEBIT,
    val isTransactionExcluded: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showNewTagInput: Boolean = false,
    val newTagError: UiText? = null,
    val showDateTimePicker: Boolean = false
) {
    val transactionDateFormatted: String
        get() = transactionTimestamp.format(DateUtil.Formatters.localizedDateMedium)
}