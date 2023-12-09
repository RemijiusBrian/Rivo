package dev.ridill.rivo.folders.domain.model

import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class FoldersListOption(
    @StringRes val labelRes: Int
) {
    SHOW_HIDE_BALANCED(R.string.folder_list_option_hide_balanced)
}