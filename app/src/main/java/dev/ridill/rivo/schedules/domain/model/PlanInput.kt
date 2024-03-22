package dev.ridill.rivo.schedules.domain.model

import android.os.Parcelable
import androidx.compose.ui.graphics.toArgb
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.ui.theme.RivoSelectableColorsList
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class PlanInput(
    val id: Long,
    val name: String,
    val colorCode: Int,
    val createdTimestamp: LocalDateTime
) : Parcelable {
    companion object {
        val INITIAL = PlanInput(
            id = RivoDatabase.DEFAULT_ID_LONG,
            name = String.Empty,
            colorCode = RivoSelectableColorsList.first().toArgb(),
            createdTimestamp = DateUtil.now()
        )
    }

    val isNew: Boolean
        get() = id <= RivoDatabase.DEFAULT_ID_LONG
}