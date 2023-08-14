package dev.ridill.mym.settings.data.repository

import dev.ridill.mym.R
import dev.ridill.mym.core.domain.model.Resource
import dev.ridill.mym.core.domain.model.SimpleResource
import dev.ridill.mym.core.domain.service.GoogleSignInService
import dev.ridill.mym.core.domain.util.log
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.data.remote.GDriveApi
import dev.ridill.mym.settings.domain.backup.BackupService
import dev.ridill.mym.settings.domain.backup.GDriveService
import dev.ridill.mym.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupRepositoryImpl(
    private val backupService: BackupService,
    private val gDriveApi: GDriveApi,
    private val signInService: GoogleSignInService,
    private val gDriveService: GDriveService
) : BackupRepository {
    override suspend fun performAppDataBackup(): SimpleResource = withContext(Dispatchers.IO) {
        try {
//            val token = signInService.getAccessToken()
//            gDriveApi.getBackupFilesList(token).files.forEach { file ->
//                gDriveApi.deleteFile(token, file.id)
//            }

            val backupFile = backupService.buildBackupFile()
            /*val metadataMap = mapOf(
                "name" to backupFile.name,
                "parents" to appDataFolderParents
            )
            val metadataJson = Gson().toJson(metadataMap)
            val metadataPart = metadataJson.toRequestBody(JSON_MIME_TYPE.toMediaTypeOrNull())

            val fileBody = backupFile.asRequestBody(BACKUP_MIME_TYPE.toMediaTypeOrNull())
            val mediaPart = MultipartBody.Part.createFormData(
                MEDIA_PART_KEY,
                backupFile.name,
                fileBody
            )

            val response = gDriveApi.uploadFile(
                token = token,
                metadata = metadataPart,
                file = mediaPart
            )*/

            gDriveService.getFilesList().files.forEach {
                gDriveService.deleteFile(it.id)
            }

            val response = gDriveService.uploadBackupFile(backupFile)

            println("AppDebug: Response - $response")

            Resource.Success(Unit)
        } catch (t: Throwable) {
            t.printStackTrace()
            Resource.Error(UiText.StringResource(R.string.error_unknown))
        }
    }

    override suspend fun performAppDataRestore(): SimpleResource = withContext(Dispatchers.IO) {
        try {
//            val token = signInService.getAccessToken()
//            val backupFile = gDriveApi.getBackupFilesList(token).files.firstOrNull()
//                ?: throw Throwable("No Backup Found")
//            val fileBody = gDriveApi.downloadFile(token, backupFile.id).body()
//                ?: throw Throwable("Body Null")
//            backupService.restoreBackupFile(fileBody.byteStream())
            val files = gDriveService.getFilesList()
            log { "Files List - $files" }
            val backupFile = files.files.firstOrNull()
                ?: throw Throwable("No Backup Found")
            val downloadedStream = gDriveService.downloadBackupFile(backupFile.id)
            backupService.restoreBackupFile(downloadedStream)
            Resource.Success(Unit)
        } catch (t: Throwable) {
            t.printStackTrace()
            Resource.Error(UiText.StringResource(R.string.error_unknown))
        }
    }
}

const val JSON_MIME_TYPE = "application/json"
const val BACKUP_MIME_TYPE = "application/octet-stream"

private val appDataFolderParents: List<String>
    get() = listOf("appDataFolder")