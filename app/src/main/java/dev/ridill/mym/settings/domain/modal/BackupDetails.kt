package dev.ridill.mym.settings.domain.modal

import android.os.Parcelable
import dev.ridill.mym.core.domain.util.DateUtil
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class BackupDetails(
    val name: String,
    val id: String,
    val timestamp: String
) : Parcelable {
    fun getParsedDateTime(): LocalDateTime? = DateUtil.parse(timestamp)
}