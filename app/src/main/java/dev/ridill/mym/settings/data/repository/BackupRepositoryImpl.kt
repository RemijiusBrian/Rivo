package dev.ridill.mym.settings.data.repository

import com.google.gson.Gson
import dev.ridill.mym.R
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.model.Resource
import dev.ridill.mym.core.domain.model.SimpleResource
import dev.ridill.mym.core.domain.service.GoogleSignInService
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.data.remote.GDriveApi
import dev.ridill.mym.settings.data.remote.MEDIA_PART_KEY
import dev.ridill.mym.settings.data.toBackupDetails
import dev.ridill.mym.settings.domain.backup.BACKUP_FILE_NAME
import dev.ridill.mym.settings.domain.backup.BackupService
import dev.ridill.mym.settings.domain.modal.BackupDetails
import dev.ridill.mym.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class BackupRepositoryImpl(
    private val backupService: BackupService,
    private val gDriveApi: GDriveApi,
    private val signInService: GoogleSignInService,
    private val preferencesManager: PreferencesManager
) : BackupRepository {
    override suspend fun checkForBackup(): Resource<BackupDetails> = withContext(Dispatchers.IO) {
        try {
            val token = signInService.getAccessToken()
            val backupFile = gDriveApi.getBackupFilesList(token).files.firstOrNull()
                ?: throw NoBackupFoundThrowable()

            val backupDetails = backupFile.toBackupDetails()
            Resource.Success(backupDetails)
        } catch (t: NoBackupFoundThrowable) {
            Resource.Error(UiText.StringResource(R.string.error_no_backup_found))
        } catch (t: Throwable) {
            Resource.Error(
                t.localizedMessage?.let { UiText.DynamicString(it) }
                    ?: UiText.StringResource(R.string.error_unknown)
            )
        }
    }

    override suspend fun performAppDataBackup(): SimpleResource = withContext(Dispatchers.IO) {
        try {
            val token = signInService.getAccessToken()
            gDriveApi.getFilesInAppDataFolder(token).files.forEach { file ->
                gDriveApi.deleteFile(token, file.id)
            }

            val backupFile = backupService.buildBackupFile()
            val metadataMap = mapOf(
                "name" to backupFile.name,
                "parents" to backupParents
            )
            val metadataJson = Gson().toJson(metadataMap)
            val metadataPart = metadataJson.toRequestBody(JSON_MIME_TYPE.toMediaTypeOrNull())

            val fileBody = backupFile.asRequestBody(BACKUP_MIME_TYPE.toMediaTypeOrNull())
            val mediaPart = MultipartBody.Part.createFormData(
                MEDIA_PART_KEY,
                backupFile.name,
                fileBody
            )

            gDriveApi.uploadFile(
                token = token,
                metadata = metadataPart,
                file = mediaPart
            )

            preferencesManager.updateLastBackupTimestamp(DateUtil.now())
            Resource.Success(Unit)
        } catch (t: Throwable) {
            t.printStackTrace()
            Resource.Error(UiText.StringResource(R.string.error_unknown))
        }
    }

    override suspend fun performAppDataRestore(): SimpleResource = withContext(Dispatchers.IO) {
        try {
            val token = signInService.getAccessToken()
            val backupFile = gDriveApi.getBackupFilesList(token).files.firstOrNull()
                ?: throw NoBackupFoundThrowable()

            val response = gDriveApi.downloadFile(token, backupFile.id)
            val fileBody = response.body()
                ?: throw Throwable("Body Null")

            backupService.restoreBackupFile(fileBody.byteStream())
            DateUtil.parse(
                backupFile.name.removeSuffix("-$BACKUP_FILE_NAME")
            )?.let { preferencesManager.updateLastBackupTimestamp(it) }

            Resource.Success(Unit)
        } catch (t: Throwable) {
            t.printStackTrace()
            Resource.Error(UiText.StringResource(R.string.error_unknown))
        }
    }
}

const val JSON_MIME_TYPE = "application/json"
const val BACKUP_MIME_TYPE = "application/octet-stream"
const val APP_DATA_SPACE = "appDataFolder"
private val backupParents: List<String>
    get() = listOf(APP_DATA_SPACE)

class NoBackupFoundThrowable : Throwable("No Backups Found")