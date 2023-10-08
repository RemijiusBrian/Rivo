package dev.ridill.rivo.core.domain.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.ui.graphics.vector.ImageVector
import dev.ridill.rivo.R

enum class SortOrder(
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    ASCENDING(R.string.sort_order_ascending, Icons.Default.ArrowUpward),
    DESCENDING(R.string.sort_order_descending, Icons.Default.ArrowDownward);

    operator fun not(): SortOrder = when (this) {
        ASCENDING -> DESCENDING
        DESCENDING -> ASCENDING
    }
}
