package dev.ridill.rivo.settings.data.repository

import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.gson.Gson
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.model.SimpleResource
import dev.ridill.rivo.core.domain.service.GoogleSignInService
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.logD
import dev.ridill.rivo.core.domain.util.logE
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.data.remote.GDriveApi
import dev.ridill.rivo.settings.data.remote.MEDIA_PART_KEY
import dev.ridill.rivo.settings.data.remote.dto.CreateGDriveFolderRequestDto
import dev.ridill.rivo.settings.data.toBackupDetails
import dev.ridill.rivo.settings.domain.backup.BackupCachingFailedThrowable
import dev.ridill.rivo.settings.domain.backup.BackupService
import dev.ridill.rivo.settings.domain.modal.BackupDetails
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class BackupRepositoryImpl(
    private val backupService: BackupService,
    private val gDriveApi: GDriveApi,
    private val signInService: GoogleSignInService,
    private val preferencesManager: PreferencesManager
) : BackupRepository {
    override suspend fun checkForBackup(): Resource<BackupDetails> = withContext(Dispatchers.IO) {
        try {
            logI { "Checking For Backup" }
            val email = signInService.getSignedInAccount()?.email
                ?: throw GoogleAuthException()
            val backupFolderName = backupFolderName(email)
            val backupFolder = gDriveApi.getBackupFolder(
                query = "trashed=false and name = '$backupFolderName'"
            ).files.firstOrNull()
                ?: throw NoBackupFoundThrowable()
            val backupFile = gDriveApi.getBackupFilesList(
                folderId = backupFolder.id
            ).files.firstOrNull()
                ?: throw NoBackupFoundThrowable()

            val backupDetails = backupFile.toBackupDetails()
            logD { "Backup Found - $backupDetails" }
            Resource.Success(backupDetails)
        } catch (t: NoBackupFoundThrowable) {
            logE(t)
            Resource.Error(UiText.StringResource(R.string.error_no_backup_found))
        } catch (t: Throwable) {
            logE(t)
            Resource.Error(UiText.StringResource(R.string.error_unknown))
        }
    }

    @Throws(
        InvalidEncryptionPasswordThrowable::class,
        BackupCachingFailedThrowable::class,
        IOException::class,
        UserRecoverableAuthException::class,
        GoogleAuthException::class
    )
    override suspend fun performAppDataBackup() = withContext(Dispatchers.IO) {
        logI { "Performing Data Backup" }
        val passwordHash = preferencesManager.preferences.first()
            .encryptionPasswordHash.orEmpty()
            .ifEmpty { throw InvalidEncryptionPasswordThrowable() }
        val email = signInService.getSignedInAccount()?.email
            ?: throw GoogleAuthException()
        val backupFolderName = backupFolderName(email)
        var backupFolder = gDriveApi.getBackupFolder(
            query = "name = '$backupFolderName' and trashed=false"
        ).files.firstOrNull()
        logD { "Received backup folder - $backupFolder" }
        if (backupFolder == null) {
            val createBackupFolderRequest = CreateGDriveFolderRequestDto(backupFolderName(email))
            val createBackupFolderMetadataPart = Gson().toJson(createBackupFolderRequest)
                .toRequestBody(JSON_MIME_TYPE.toMediaTypeOrNull())
            logD { "Create backup folder metadata - $createBackupFolderMetadataPart" }
            backupFolder = gDriveApi.createFolder(createBackupFolderMetadataPart)
        }
        val backupFile = backupService.buildBackupFile(passwordHash)
        val metadataMap = mapOf(
            "name" to backupFile.name,
            "parents" to listOf(backupFolder.id)
        )
        val metadataJson = Gson().toJson(metadataMap)
        val metadataPart = metadataJson.toRequestBody(JSON_MIME_TYPE.toMediaTypeOrNull())

        val fileBody = backupFile.asRequestBody(BACKUP_MIME_TYPE.toMediaTypeOrNull())
        val mediaPart = MultipartBody.Part.createFormData(
            MEDIA_PART_KEY,
            backupFile.name,
            fileBody
        )

        logD { "Backup file generated - ${backupFile.name}" }
        val gDriveBackup = gDriveApi.uploadFile(
            metadata = metadataPart,
            file = mediaPart
        )
        logI { "Backup file uploaded - $gDriveBackup" }

        preferencesManager.updateLastBackupTimestamp(DateUtil.now())
        gDriveApi.getOtherFilesInDrive(
            query = "trashed=false and files(id) != '${gDriveBackup.id}'"
        ).files.forEach { file ->
            gDriveApi.deleteFile(file.id)
        }
        logI { "Cleaned up Drive" }
        backupService.clearLocalCache()
        logI { "Cleaned up local cache" }
    }

    override suspend fun performAppDataRestore(
        details: BackupDetails,
        passwordHash: String
    ): SimpleResource = withContext(Dispatchers.IO) {
        try {
            logI { "Restoring Backup" }
            val response = gDriveApi.downloadFile(details.id)
            val fileBody = response.body()
                ?: throw BackupDownloadFailedThrowable()
            logI { "Downloaded backup data" }

            backupService.restoreBackupFile(
                dataInputStream = fileBody.byteStream(),
                password = passwordHash
            )
            preferencesManager.updateEncryptionPasswordHash(passwordHash)
            details.getParsedDateTime()?.let { preferencesManager.updateLastBackupTimestamp(it) }
            backupService.clearLocalCache()
            logI { "Cleaned up local cache" }

            logI { "Backup Restored" }
            Resource.Success(Unit)
        } catch (t: BackupDownloadFailedThrowable) {
            logE(t)
            Resource.Error(UiText.StringResource(R.string.error_download_backup_failed))
        } catch (t: Throwable) {
            logE(t)
            Resource.Error(UiText.StringResource(R.string.error_app_data_restore_failed))
        }
    }

    private fun backupFolderName(email: String): String =
        "Rivo $email backup"
}

const val JSON_MIME_TYPE = "application/json"
const val BACKUP_MIME_TYPE = "application/octet-stream"
const val APP_DATA_SPACE = "appDataFolder"
const val G_DRIVE_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
//const val G_DRIVE_SHORTCUT_MIME_TYPE = "application/vnd.google-apps.shortcut"

class NoBackupFoundThrowable : Throwable("No Backups Found")
class BackupDownloadFailedThrowable : Throwable("Failed to download backup data")
class InvalidEncryptionPasswordThrowable : Throwable("")