package dev.ridill.rivo.transactions.presentation.allTransactions

import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class AllTransactionsMultiSelectionOption(
    @StringRes val labelRes: Int
) {
    UNTAG(R.string.all_transactions_multi_selection_option_untag),
    MARK_EXCLUDED(R.string.all_transactions_multi_selection_option_mark_excluded),
    UN_MARK_EXCLUDED(R.string.all_transactions_multi_selection_option_un_mark_excluded),
    ADD_TO_FOLDER(R.string.all_transactions_multi_selection_option_add_to_folder),
    REMOVE_FROM_FOLDERS(R.string.all_transactions_multi_selection_option_remove_from_folders),
    AGGREGATE(R.string.all_transactions_multi_selection_option_aggregate)
}