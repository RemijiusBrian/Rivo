package dev.ridill.rivo.transactions.domain.model

import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class TransactionOption(
    @StringRes val labelRes: Int
) {
    UNTAG(R.string.transaction_option_untag),
    MARK_EXCLUDED(R.string.transaction_option_mark_excluded),
    UN_MARK_EXCLUDED(R.string.transaction_option_un_mark_excluded),
    ADD_TO_FOLDER(R.string.transaction_option_add_to_folder),
    REMOVE_FROM_FOLDERS(R.string.transaction_option_remove_from_folders),
}