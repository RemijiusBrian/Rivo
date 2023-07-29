package dev.ridill.mym.expense.domain.model

import android.os.Parcelable
import dev.ridill.mym.core.domain.util.Zero
import kotlinx.parcelize.Parcelize

@Parcelize
data class Expense(
    val id: Long,
    val amount: String,
    val note: String
) : Parcelable {
    companion object {
        val DEFAULT = Expense(
            id = Long.Zero,
            amount = "",
            note = ""
        )
    }
}