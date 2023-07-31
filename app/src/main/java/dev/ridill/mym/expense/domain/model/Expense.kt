package dev.ridill.mym.expense.domain.model

import android.os.Parcelable
import com.notkamui.keval.Keval
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.domain.util.tryOrNull
import kotlinx.parcelize.Parcelize

@Parcelize
data class Expense(
    val id: Long,
    val amount: String,
    val note: String
) : Parcelable {

    val evalAmount: Double
        get() = tryOrNull { Keval.eval(amount) } ?: -1.0

    companion object {
        val DEFAULT = Expense(
            id = Long.Zero,
            amount = "",
            note = ""
        )
    }
}