package dev.ridill.mym.settings.data.repository

import com.google.gson.Gson
import dev.ridill.mym.core.domain.service.GoogleSignInService
import dev.ridill.mym.settings.data.remote.DB_MIME_TYPE
import dev.ridill.mym.settings.data.remote.GDriveApi
import dev.ridill.mym.settings.data.remote.JSON_MIME_TYPE
import dev.ridill.mym.settings.data.remote.MEDIA_PART_KEY
import dev.ridill.mym.settings.domain.BackupRepository
import dev.ridill.mym.settings.domain.backup.LocalDataService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class BackupRepositoryImpl(
    private val localDataService: LocalDataService,
    private val gDriveApi: GDriveApi,
    private val signInService: GoogleSignInService
) : BackupRepository {
    override suspend fun performAppDataBackup() = withContext(Dispatchers.IO) {
        try {
            val token = signInService.getAccessToken()
            val authToken = "Bearer $token"

            uploadDbData(authToken)

        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private suspend fun uploadDbData(token: String) {
        val dbCache = localDataService.getDatabaseCache()
        println("AppDebug: DB Cache - $dbCache")
        uploadDbFile(dbCache.dbFile, token)
        dbCache.walFile?.let { uploadDbFile(it, token) }
        dbCache.shmFile?.let { uploadDbFile(it, token) }
    }

    private suspend fun uploadDbFile(file: File, token: String) {
        val metadataMap = mapOf(
            "name" to "${file.name}.db",
            "parents" to appDataFolderParents
        )
        val metadataJson = Gson().toJson(metadataMap)
        val metadataPart = metadataJson.toRequestBody(JSON_MIME_TYPE.toMediaTypeOrNull())

        val fileBody = file.asRequestBody(DB_MIME_TYPE.toMediaTypeOrNull())
        val mediaPart = MultipartBody.Part.createFormData(
            MEDIA_PART_KEY,
            file.name,
            fileBody
        )

        val response = gDriveApi.uploadFile(
            token = token,
            metadata = metadataPart,
            file = mediaPart
        )

        println("AppDebug: Response - $response")
    }
}

private val appDataFolderParents: List<String>
    get() = listOf("appDataFolder")