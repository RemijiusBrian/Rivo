package dev.ridill.rivo.transactions.domain.model

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.transactions.presentation.components.TagColors
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class TransactionTag(
    val id: Long,
    val name: String,
    val colorCode: Int,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean
) : Parcelable {
    val color: Color
        get() = Color(colorCode)

    companion object {
        val NEW = TransactionTag(
            id = RivoDatabase.DEFAULT_ID_LONG,
            name = "",
            colorCode = TagColors.first().toArgb(),
            createdTimestamp = DateUtil.now(),
            excluded = false
        )
    }
}