package dev.ridill.rivo.transactions.domain.model

import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import java.time.LocalDateTime

data class Transaction(
    val id: Long,
    val amount: String,
    val note: String,
    val timestamp: LocalDateTime,
    val type: TransactionType,
    val tagId: Long?,
    val folderId: Long?,
    val excluded: Boolean
) {
    companion object {
        val DEFAULT = Transaction(
            id = RivoDatabase.DEFAULT_ID_LONG,
            amount = "",
            note = "",
            timestamp = DateUtil.now(),
            type = TransactionType.DEBIT,
            tagId = null,
            folderId = null,
            excluded = false
        )
    }
}