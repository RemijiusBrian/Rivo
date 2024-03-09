package dev.ridill.rivo.scheduledTransaction.domain.model

import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class TransactionRepeatMode(
    @StringRes val labelRes: Int
) {
    ONE_TIME(R.string.transaction_repeat_mode_one_time),
    MONTHLY(R.string.transaction_repeat_mode_monthly),
    BI_MONTHLY(R.string.transaction_repeat_mode_bi_monthly)
}