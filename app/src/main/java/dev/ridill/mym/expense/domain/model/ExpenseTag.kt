package dev.ridill.mym.expense.domain.model

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.ridill.mym.core.data.db.RivoDatabase
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.expense.presentation.components.TagColors
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class ExpenseTag(
    val id: Long,
    val name: String,
    val colorCode: Int,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean
) : Parcelable {
    val color: Color
        get() = Color(colorCode)

    companion object {
        val NEW = ExpenseTag(
            id = RivoDatabase.DEFAULT_ID_LONG,
            name = "",
            colorCode = TagColors.first().toArgb(),
            createdTimestamp = DateUtil.now(),
            excluded = false
        )
    }
}