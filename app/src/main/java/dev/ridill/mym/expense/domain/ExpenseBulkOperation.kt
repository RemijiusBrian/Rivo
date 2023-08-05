package dev.ridill.mym.expense.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.ridill.mym.R

enum class ExpenseBulkOperation(
    @DrawableRes val iconRes: Int,
    @StringRes val contentDescriptionRes: Int
) {
    UNTAG(R.drawable.ic_untag, R.string.cd_untag_expenses),
    DELETE(R.drawable.ic_delete, R.string.cd_delete)
}