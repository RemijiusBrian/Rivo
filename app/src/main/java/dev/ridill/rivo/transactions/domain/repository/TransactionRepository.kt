package dev.ridill.rivo.transactions.domain.repository

import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

interface TransactionRepository {
    suspend fun saveTransaction(
        amount: Double,
        id: Long = RivoDatabase.DEFAULT_ID_LONG,
        note: String? = null,
        timestamp: LocalDateTime = DateUtil.now(),
        type: TransactionType = TransactionType.DEBIT,
        tagId: Long? = null,
        folderId: Long? = null,
        scheduleId: Long? = null,
        excluded: Boolean = false
    ): Transaction
    suspend fun delete(id: Long)
    suspend fun toggleExcluded(id: Long, excluded: Boolean)
}