package dev.ridill.rivo.folders.domain.model

import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class AggregateType(
    @StringRes val labelRes: Int
) {
    BALANCED(R.string.aggregate_type_balanced),
    AGG_DEBIT(R.string.aggregate_type_debit),
    AGG_CREDIT(R.string.aggregate_type_credit)
}