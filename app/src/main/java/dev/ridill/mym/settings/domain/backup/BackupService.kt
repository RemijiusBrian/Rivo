package dev.ridill.mym.settings.domain.backup

import android.content.Context
import dev.ridill.mym.core.data.db.RivoDatabase
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.toByteArray
import dev.ridill.mym.core.domain.util.toInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class BackupService(
    private val context: Context,
    private val database: RivoDatabase
) {
    @Throws(BackupCachingFailedThrowable::class)
    suspend fun buildBackupFile(): File = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(RivoDatabase.NAME)
        val dbWalFile = File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)

        val cachePath = context.externalCacheDir ?: throw BackupCachingFailedThrowable()
        val backupFile = File(cachePath, backupFileName())
        if (backupFile.exists()) backupFile.delete()

        checkpointDb()
        backupFile.outputStream().use { outputStream ->
            // Write DB Data
            dbFile.inputStream().use dbInputStream@{
                val dbData = it.readBytes()
                outputStream.write(dbData.size.toByteArray())
                outputStream.write(dbData)
            }

            // Write WAL Data
            if (dbWalFile.exists()) dbWalFile.inputStream().use walInputStream@{
                val walData = it.readBytes()
                outputStream.write(walData.size.toByteArray())
                outputStream.write(walData)
            }

            // Write SHM Data
            if (dbShmFile.exists()) dbShmFile.inputStream().use shmInputStream@{
                val shmData = it.readBytes()
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

                var bytesLeft = dbSize
                var byteArray = ByteArray(0)
                while (bytesLeft > 0) {
                    val data = ByteArray(minOf(DEFAULT_BUFFER_SIZE, bytesLeft))
                    val bytesRead = inputStream.read(data)
                    byteArray += data.copyOfRange(0, bytesRead)
                    bytesLeft -= bytesRead
                }
                it.write(byteArray)
            }

            // Read WAL Data
            dbWalFile.outputStream().use walOutputStream@{
                val walSizeBytes = ByteArray(Int.SIZE_BYTES)
                val sizeBytesRead = inputStream.read(walSizeBytes)
                if (sizeBytesRead == -1) return@walOutputStream
                val walSize = walSizeBytes.toInt()

                var bytesLeft = walSize
                var byteArray = ByteArray(0)
                while (bytesLeft > 0) {
                    val data = ByteArray(minOf(DEFAULT_BUFFER_SIZE, bytesLeft))
                    val bytesRead = inputStream.read(data)
                    byteArray += data.copyOfRange(0, bytesRead)
                    bytesLeft -= bytesRead
                }
                it.write(byteArray)
            }

            // Read SHM Data
            dbShmFile.outputStream().use shmOutputStream@{
                val shmSizeBytes = ByteArray(Int.SIZE_BYTES)
                val sizeBytesRead = inputStream.read(shmSizeBytes)
                if (sizeBytesRead == -1) return@shmOutputStream
                val shmSize = shmSizeBytes.toInt()

                var bytesLeft = shmSize
                var byteArray = byteArrayOf()
                while (bytesLeft > 0) {
                    val data = ByteArray(minOf(DEFAULT_BUFFER_SIZE, bytesLeft))
                    val bytesRead = inputStream.read(data)
                    byteArray += data.copyOfRange(0, bytesRead)
                    bytesLeft -= bytesRead
                }
                it.write(byteArray)
            }
        }
        checkpointDb()
    }

    suspend fun clearLocalCache() = withContext(Dispatchers.IO) {
        val cacheDir = context.externalCacheDir
        cacheDir?.listFiles()
            ?.forEach { file ->
                file.delete()
            }
    }

    private fun checkpointDb() {
        val writableDb = database.openHelper.writableDatabase
        writableDb.query("PRAGMA wal_checkpoint(FULL);")
        writableDb.query("PRAGMA wal_checkpoint(TRUNCATE);")
    }

    private fun backupFileName(): String = "${DateUtil.now()}-$BACKUP_FILE_NAME"
}

private const val SQLITE_WAL_FILE_SUFFIX = "-wal"
private const val SQLITE_SHM_FILE_SUFFIX = "-shm"
const val BACKUP_FILE_NAME = "Backup.mym"

class BackupCachingFailedThrowable : Throwable("Failed to create backup cache")