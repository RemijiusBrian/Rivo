package dev.ridill.rivo.transactions.domain.model

import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class TransactionOption(
    @StringRes val labelRes: Int
) {
    UNTAG(R.string.transaction_option_untag),
    MARK_AS_EXCLUDED(R.string.transaction_option_mark_excluded),
    MARK_AS_INCLUDED(R.string.transaction_option_mark_included),
    ADD_TO_GROUP(R.string.transaction_option_add_to_group)
}