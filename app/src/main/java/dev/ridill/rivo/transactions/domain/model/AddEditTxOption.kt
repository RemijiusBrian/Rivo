package dev.ridill.rivo.transactions.domain.model

import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class AddEditTxOption(
    @StringRes val labelRes: Int
) {
    SCHEDULE_FOR_LATER(R.string.schedule_for_later)
}