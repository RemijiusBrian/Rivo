package dev.ridill.rivo.transactions.presentation.allTransactions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class AllTransactionsMultiSelectionOption(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int
) {
    DELETE(
        iconRes = R.drawable.ic_rounded_delete,
        labelRes = R.string.action_delete
    ),
    UNTAG(
        iconRes = R.drawable.ic_rounded_untag,
        labelRes = R.string.all_transactions_multi_selection_option_untag
    ),
    MARK_EXCLUDED(
        iconRes = R.drawable.ic_rounded_exclude,
        labelRes = R.string.all_transactions_multi_selection_option_mark_excluded
    ),
    UN_MARK_EXCLUDED(
        iconRes = R.drawable.ic_rounded_exclude,
        labelRes = R.string.all_transactions_multi_selection_option_un_mark_excluded
    ),
    ADD_TO_FOLDER(
        iconRes = R.drawable.ic_outline_add_folder,
        labelRes = R.string.all_transactions_multi_selection_option_add_to_folder
    ),
    REMOVE_FROM_FOLDERS(
        iconRes = R.drawable.ic_outline_remove_folder,
        labelRes = R.string.all_transactions_multi_selection_option_remove_from_folders
    ),
    AGGREGATE(
        iconRes = R.drawable.ic_total,
        labelRes = R.string.all_transactions_multi_selection_option_aggregate
    )
}