package dev.ridill.rivo.transactions.data.repository

import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.toTransaction
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {
    override suspend fun saveTransaction(
        amount: Double,
        id: Long,
        note: String?,
        timestamp: LocalDateTime,
        type: TransactionType,
        tagId: Long?,
        folderId: Long?,
        scheduleId: Long?,
        excluded: Boolean
    ): Transaction = withContext(Dispatchers.IO) {
        val entity = TransactionEntity(
            id = id,
            note = note.orEmpty(),
            amount = amount,
            timestamp = timestamp,
            type = type,
            isExcluded = excluded,
            tagId = tagId,
            folderId = folderId,
            scheduleId = scheduleId
        )
        val insertedId = dao.upsert(entity).first()
        entity.copy(id = insertedId)
            .toTransaction()
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    override suspend fun toggleExcluded(id: Long, excluded: Boolean) = withContext(Dispatchers.IO) {
        dao.toggleExclusionByIds(setOf(id), excluded)
    }
}