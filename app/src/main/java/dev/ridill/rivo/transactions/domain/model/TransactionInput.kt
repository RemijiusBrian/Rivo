package dev.ridill.rivo.transactions.domain.model

import android.os.Parcelable
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class TransactionInput(
    val id: Long,
    val amount: String,
    val note: String,
    val timestamp: LocalDateTime,
    val type: TransactionType,
    val tagId: Long?,
    val folderId: Long?,
    val excluded: Boolean
) : Parcelable {
    companion object {
        val DEFAULT = TransactionInput(
            id = RivoDatabase.DEFAULT_ID_LONG,
            amount = String.Empty,
            note = String.Empty,
            timestamp = DateUtil.now(),
            type = TransactionType.DEBIT,
            tagId = null,
            folderId = null,
            excluded = false
        )
    }
}