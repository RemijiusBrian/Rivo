package dev.ridill.rivo.folders.data.repository

import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.folders.data.toFolder
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.domain.repository.AddEditFolderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class AddEditFolderRepositoryImpl(
    private val dao: FolderDao
) : AddEditFolderRepository {
    override suspend fun getFolderDetails(id: Long): Folder? = withContext(Dispatchers.IO) {
        dao.getFolderById(id)?.toFolder()
    }

    override suspend fun saveFolder(
        id: Long,
        name: String,
        timestamp: LocalDateTime,
        excluded: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val entity = FolderEntity(
            id = id,
            name = name,
            createdTimestamp = timestamp,
            isExcluded = excluded
        )
        dao.upsert(entity).first()
    }
}