package dev.ridill.mym.settings.domain.backup

import android.content.Context
import dev.ridill.mym.core.data.db.MYMDatabase
import dev.ridill.mym.core.domain.util.log
import dev.ridill.mym.core.domain.util.toByteArray
import dev.ridill.mym.core.domain.util.toInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class BackupService(
    private val context: Context,
    private val database: MYMDatabase
) {

    @Throws(BackupCachingFailedThrowable::class)
    suspend fun buildBackupFile(): File = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(MYMDatabase.NAME)
        val dbWalFile = File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)

        val cachePath = context.externalCacheDir ?: throw BackupCachingFailedThrowable()
        val backupFile = File(cachePath, BACKUP_FILE_NAME)
        if (backupFile.exists()) backupFile.delete()

        checkpointDb()
        backupFile.outputStream().use { outputStream ->
            // Write DB Data
            dbFile.inputStream().use dbInputStream@{
                val dbData = it.readBytes()
                log { "DB Size - ${dbData.size}" }
                log { "DB Data - ${dbData.contentToString()}" }
                outputStream.write(dbData.size.toByteArray())
                outputStream.write(dbData)
            }

            // Write WAL Data
            if (dbWalFile.exists()) dbWalFile.inputStream().use walInputStream@{
                val walData = it.readBytes()
                log { "WAL Size - ${walData.size}" }
                log { "WAL Data - ${walData.contentToString()}" }
                outputStream.write(walData.size.toByteArray())
                outputStream.write(walData)
            }

            // Write SHM Data
            if (dbShmFile.exists()) dbShmFile.inputStream().use shmInputStream@{
                val shmData = it.readBytes()
                log { "SHM Size - ${shmData.size}" }
                log { "SHM Data - ${shmData.contentToString()}" }
                outputStream.write(shmData.size.toByteArray())
                outputStream.write(shmData)
            }
        }

        backupFile
    }

    @Throws(BackupCachingFailedThrowable::class)
    suspend fun restoreBackupFile(dataInputStream: InputStream) = withContext(Dispatchers.IO) {
        val dbPath = database.openHelper.readableDatabase.path
        val dbFile = dbPath?.let { File(it) } ?: throw BackupCachingFailedThrowable()
        val dbWalFile = File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)

        dataInputStream.use { inputStream ->
            // Read DB Data
            dbFile.outputStream().use dbOutputStream@{
                val dbSizeBytes = ByteArray(Int.SIZE_BYTES)
                inputStream.read(dbSizeBytes)
                val dbSize = dbSizeBytes.toInt()
                log { "DB Size - $dbSize" }
                val dbData = ByteArray(dbSize)
                inputStream.read(dbData)
                log { "DB Data - ${dbData.contentToString()}" }
                it.write(dbData)
            }

            // Read WAL Data
            dbWalFile.outputStream().use walOutputStream@{
                val walSizeBytes = ByteArray(Int.SIZE_BYTES)
                val bytesRead = inputStream.read(walSizeBytes)
                if (bytesRead == -1) return@walOutputStream
                val walSize = walSizeBytes.toInt()

                log { "WAL Size - $walSize" }
                val walData = ByteArray(walSize)
                inputStream.read(walData)
                log { "WAL Data - ${walData.contentToString()}" }
                it.write(walData)
            }

            // Read SHM Data
            dbShmFile.outputStream().use shmOutputStream@{
                val shmSizeBytes = ByteArray(Int.SIZE_BYTES)
                val bytesRead = inputStream.read(shmSizeBytes)
                if (bytesRead == -1) return@shmOutputStream
                val shmSize = shmSizeBytes.toInt()

                log { "SHM Size - $shmSize" }
                val shmData = ByteArray(shmSize)
                inputStream.read(shmData)
                log { "SHM Data - ${shmData.contentToString()}" }
                it.write(shmData)
            }

            log { "Restore Done" }
        }
        checkpointDb()
    }

    private fun checkpointDb() {
        val writableDb = database.openHelper.writableDatabase
        writableDb.query("PRAGMA wal_checkpoint(FULL);")
        writableDb.query("PRAGMA wal_checkpoint(TRUNCATE);")
    }

    /*suspend fun backupDb(): SimpleResource = withContext(Dispatchers.IO) {
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
    }*/

    /*suspend fun restoreDb(): SimpleResource = withContext(Dispatchers.IO) {
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
    }*/
}

private const val SQLITE_WAL_FILE_SUFFIX = "-wal"
private const val SQLITE_SHM_FILE_SUFFIX = "-shm"
const val BACKUP_FILE_NAME = "Backup.mym"

class BackupCachingFailedThrowable : Throwable("Failed to create backup cache")