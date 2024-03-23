package dev.ridill.rivo.transactions.domain.model

import android.os.Parcelable
import androidx.compose.ui.graphics.toArgb
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.ui.theme.RivoSelectableColorsList
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class Tag(
    val id: Long,
    val name: String,
    val colorCode: Int,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean
) : Parcelable {
    companion object {
        val NEW = Tag(
            id = RivoDatabase.DEFAULT_ID_LONG,
            name = String.Empty,
            colorCode = RivoSelectableColorsList.first().toArgb(),
            createdTimestamp = DateUtil.now(),
            excluded = false
        )
    }
}