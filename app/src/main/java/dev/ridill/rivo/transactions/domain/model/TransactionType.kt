package dev.ridill.rivo.transactions.domain.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.ui.graphics.vector.ImageVector
import dev.ridill.rivo.R

enum class TransactionType(
    @StringRes val labelRes: Int,
    val directionIcon: ImageVector
) {
    CREDIT(R.string.transaction_type_label_credit, Icons.Default.ArrowUpward),
    DEBIT(R.string.transaction_type_label_debit, Icons.Default.ArrowDownward)
}