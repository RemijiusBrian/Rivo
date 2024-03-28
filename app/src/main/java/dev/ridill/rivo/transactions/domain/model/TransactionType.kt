package dev.ridill.rivo.transactions.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class TransactionType(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    CREDIT(R.string.transaction_type_label_credit, R.drawable.ic_rounded_arrow_down_left),
    DEBIT(R.string.transaction_type_label_debit, R.drawable.ic_rounded_arrow_up_right)
}