package dev.ridill.rivo.transactions.presentation.addEditTransaction

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.scheduledTransaction.domain.model.TransactionRepeatMode
import dev.ridill.rivo.transactions.domain.model.AmountTransformation
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

data class AddEditTransactionState(
    val isLoading: Boolean = false,
    val currency: Currency = LocaleUtil.defaultCurrency,
    val isScheduleTxMode: Boolean = false,
    val amountRecommendations: List<Long> = emptyList(),
    val tagsList: List<Tag> = emptyList(),
    val selectedTagId: Long? = null,
    val transactionTimestamp: LocalDateTime = DateUtil.now(),
    val transactionType: TransactionType = TransactionType.DEBIT,
    val isTransactionExcluded: Boolean = false,
    val showTransformationInput: Boolean = false,
    val selectedAmountTransformation: AmountTransformation = AmountTransformation.DIVIDE_BY,
    val showDeleteConfirmation: Boolean = false,
    val showNewTagInput: Boolean = false,
    val newTagError: UiText? = null,
    val showDateTimePicker: Boolean = false,
    val showFolderSelection: Boolean = false,
    val linkedFolderName: String? = null,
    val showRepeatModeSelection: Boolean = false,
    val selectedRepeatMode: TransactionRepeatMode = TransactionRepeatMode.ONE_TIME
) {
    val transactionDateFormatted: String
        get() = transactionTimestamp.format(DateUtil.Formatters.localizedDateMedium)
}