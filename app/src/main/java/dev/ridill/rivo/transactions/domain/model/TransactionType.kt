package dev.ridill.rivo.transactions.domain.model

import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class TransactionType(
    @StringRes val labelRes: Int
) {
    CREDIT(R.string.transaction_type_label_credit),
    DEBIT(R.string.transaction_type_label_debit)
}