package dev.ridill.rivo.expense.domain.model

import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class ExpenseOption(
    @StringRes val labelRes: Int
) {
    DE_TAG(R.string.expense_option_de_tag),
    MARK_AS_EXCLUDED(R.string.expense_option_mark_excluded),
    MARK_AS_INCLUDED(R.string.expense_option_mark_included)
}