package dev.ridill.rivo.settings.domain.backup

import android.content.Context
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.crypto.CryptoManager
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.core.domain.util.toByteArray
import dev.ridill.rivo.core.domain.util.toInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class BackupService(
    private val context: Context,
    private val database: RivoDatabase,
    private val cryptoManager: CryptoManager
) {
    @Throws(BackupCachingFailedThrowable::class)
    suspend fun buildBackupFile(password: String): File = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(RivoDatabase.NAME)
        val dbWalFile = File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)

        val cachePath = context.externalCacheDir ?: throw BackupCachingFailedThrowable()
        logI { "Create temp backup cache file" }
        val tempCache = File(cachePath, "TempBackupCache.backup")
        if (tempCache.exists()) tempCache.delete()

        logI { "Checkpoint DB" }
        checkpointDb()
        logI { "Read DB data into temp backup cache file" }
        tempCache.outputStream().use tempCacheOutputStream@{ outputStream ->
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

        logI { "Create DB backup file" }
        val backupFile = File(cachePath, backupFileName())
        if (backupFile.exists()) backupFile.delete()
        tempCache.inputStream().use tempCacheInputStream@{
            val rawBytes = it.readBytes()
            logI { "Encrypt temp backup cache data" }
            val encryptionResult = cryptoManager.encrypt(rawBytes, password)
            backupFile.outputStream().use backupFileOutputStream@{ outputStream ->
                logI { "Writ encrypted temp backup cache data to backup file" }
                outputStream.write(encryptionResult.iv.size.toByteArray())
                outputStream.write(encryptionResult.iv)
                outputStream.write(encryptionResult.data)
            }
        }

        backupFile
    }

    @Throws(BackupCachingFailedThrowable::class)
    suspend fun restoreBackupFile(dataInputStream: InputStream, password: String) =
        withContext(Dispatchers.IO) {
            val dbPath = database.openHelper.readableDatabase.path
            val dbFile = dbPath?.let { File(it) } ?: throw BackupCachingFailedThrowable()
            val dbWalFile = File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
            val dbShmFile = File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)

            val cachePath = context.externalCacheDir ?: throw BackupCachingFailedThrowable()
            logI { "Create temp decrypted cache " }
            val tempDecryptCache = File(cachePath, "TempDecryptCache.backup")

            dataInputStream.use dataInputStream@{ inputStream ->
                val ivSizeBytes = ByteArray(Int.SIZE_BYTES)
                inputStream.read(ivSizeBytes)
                val ivSize = ivSizeBytes.toInt()

                logI { "Read iv data" }
                var ivBytesLeft = ivSize
                var ivBytes = ByteArray(0)
                while (ivBytesLeft > 0) {
                    val data = ByteArray(minOf(DEFAULT_BUFFER_SIZE, ivBytesLeft))
                    val bytesRead = inputStream.read(data)
                    ivBytes += data.copyOfRange(0, bytesRead)
                    ivBytesLeft -= bytesRead
                }

                logI { "Read encrypted data" }
                val dataBytes = inputStream.readBytes()
                logI { "Decrypt data" }
                val decryptedBytes = cryptoManager.decrypt(dataBytes, ivBytes, password)

                logI { "Write decrypted data to temp decrypt cache" }
                tempDecryptCache.outputStream().use tempDecryptCacheOutputStream@{
                    it.write(decryptedBytes)
                }
            }

            logI { "Write temp decrypt cache to DB files" }
            tempDecryptCache.inputStream().use tempDecryptCacheInputStream@{ inputStream ->
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
const val BACKUP_FILE_NAME = "Rivo.backup"

class BackupCachingFailedThrowable : Throwable("Failed to create backup cache")