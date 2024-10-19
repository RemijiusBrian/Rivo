package dev.ridill.rivo.folders.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.Zero

enum class AggregateType(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    BALANCED(R.string.aggregate_type_balanced, R.drawable.ic_rounded_arrow_up_down),
    AGG_DEBIT(R.string.aggregate_type_debit, R.drawable.ic_rounded_arrow_up_right),
    AGG_CREDIT(R.string.aggregate_type_credit, R.drawable.ic_rounded_arrow_down_left);

    companion object {
        fun fromAmount(amount: Double): AggregateType = when {
            amount == Double.Zero -> BALANCED
            amount < Double.Zero -> AGG_CREDIT
            else -> AGG_DEBIT
        }
    }
}