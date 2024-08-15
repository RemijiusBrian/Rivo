package dev.ridill.rivo.folders.domain.model

import android.os.Parcelable
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class Folder(
    val id: Long,
    val name: String,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean
) : Parcelable {
    companion object {
        val NEW get() = Folder(
            id = RivoDatabase.DEFAULT_ID_LONG,
            name = String.Empty,
            createdTimestamp = DateUtil.now(),
            excluded = false
        )
    }
}