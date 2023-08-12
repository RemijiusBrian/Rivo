package dev.ridill.mym.settings.domain.backup

import android.content.Context
import dev.ridill.mym.R
import dev.ridill.mym.core.data.db.MYMDatabase
import dev.ridill.mym.core.domain.model.Resource
import dev.ridill.mym.core.domain.model.SimpleResource
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.domain.modal.DBCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalDataService(
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


    @Throws(BackupCachingFailedThrowable::class)
    suspend fun getDatabaseCache(): DBCache = withContext(Dispatchers.IO) {
        val cachePath = context.externalCacheDir ?: throw BackupCachingFailedThrowable()

        val dbFile = context.getDatabasePath(MYMDatabase.NAME)
        val dbWalFile = File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)

        val dbCache = File(cachePath, "DB_Backup")
        val walCache = File(dbCache.path + SQLITE_WAL_FILE_SUFFIX)
        val shmCache = File(dbCache.path + SQLITE_SHM_FILE_SUFFIX)

        if (dbCache.exists()) dbCache.delete()
        if (walCache.exists()) walCache.delete()
        if (shmCache.exists()) shmCache.delete()
        checkpointDb()

        dbFile.copyTo(dbCache, true)
        if (dbWalFile.exists()) dbWalFile.copyTo(walCache, true)
        if (dbShmFile.exists()) dbShmFile.copyTo(shmCache, true)

        DBCache(
            dbFile = dbCache,
            walFile = walCache.takeIf { dbWalFile.exists() },
            shmFile = shmCache.takeIf { dbWalFile.exists() }
        )
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

    private fun checkpointDb() {
        val writableDb = database.openHelper.writableDatabase
        writableDb.query("PRAGMA wal_checkpoint(FULL);")
        writableDb.query("PRAGMA wal_checkpoint(TRUNCATE);")
    }
}

private const val SQLITE_WAL_FILE_SUFFIX = "-wal"
private const val SQLITE_SHM_FILE_SUFFIX = "-shm"

class BackupCachingFailedThrowable : Throwable("Failed to create backup cache")