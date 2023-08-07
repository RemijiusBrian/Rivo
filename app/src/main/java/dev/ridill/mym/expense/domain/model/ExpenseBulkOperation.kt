package dev.ridill.mym.expense.domain.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.ui.graphics.vector.ImageVector
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.icons.Untag

enum class ExpenseBulkOperation(
    val icon: ImageVector,
    @StringRes val contentDescriptionRes: Int
) {
    UNTAG(Icons.Rounded.Untag, R.string.cd_untag_expenses),
    DELETE(Icons.Rounded.DeleteForever, R.string.cd_delete)
}