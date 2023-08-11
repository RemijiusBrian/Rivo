package dev.ridill.mym.settings.domain.service

import android.content.Context
import dev.ridill.mym.R
import dev.ridill.mym.core.data.db.MYMDatabase
import dev.ridill.mym.core.domain.model.Resource
import dev.ridill.mym.core.domain.model.SimpleResource
import dev.ridill.mym.core.ui.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class BackupService(
    private val context: Context,
    private val database: MYMDatabase
) {
    suspend fun backupDb(): SimpleResource = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(MYMDatabase.NAME)
        val dbWalFile = File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)

        val dbCache = File(dbFile.path + "-Backup")
        val walCache = File(dbCache.path + SQLITE_WAL_FILE_SUFFIX)
        val shmCache = File(dbCache.path + SQLITE_SHM_FILE_SUFFIX)

        if (dbCache.exists()) dbCache.delete()
        if (walCache.exists()) walCache.delete()
        if (shmCache.exists()) shmCache.delete()
        checkpointDb()

        try {
            dbFile.copyTo(dbCache, true)
            if (dbWalFile.exists()) dbWalFile.copyTo(walCache, true)
            if (dbShmFile.exists()) dbShmFile.copyTo(shmCache, true)

            Resource.Success(Unit)
        } catch (t: Throwable) {
            t.printStackTrace()
            Resource.Error(UiText.StringResource(R.string.error_app_data_backup_failed))
        }
    }

    suspend fun restoreDb(): SimpleResource = withContext(Dispatchers.IO) {
        val dbPath = database.openHelper.readableDatabase.path
        val dbFile = File(dbPath)
        val dbWalFile = File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)

        val dbCache = File(dbFile.path + "-Backup")
        val walCache = File(dbCache.path + SQLITE_WAL_FILE_SUFFIX)
        val shmCache = File(dbCache.path + SQLITE_SHM_FILE_SUFFIX)

        try {
            dbCache.copyTo(dbFile, true)
            if (walCache.exists()) walCache.copyTo(dbWalFile, true)
            if (shmCache.exists()) shmCache.copyTo(dbShmFile, true)
            checkpointDb()

            Resource.Success(Unit)
        } catch (t: Throwable) {
            t.printStackTrace()
            Resource.Error(UiText.StringResource(R.string.error_app_data_restore_failed))
        }
    }

    private suspend fun checkpointDb() {
        val writableDb = database.openHelper.writableDatabase
        writableDb.query("PRAGMA wal_checkpoint(FULL);")
        writableDb.query("PRAGMA wal_checkpoint(TRUNCATE);")
    }
}

private const val SQLITE_WAL_FILE_SUFFIX = "-wal"
private const val SQLITE_SHM_FILE_SUFFIX = "-shm"