package dev.ridill.mym.settings.data.remote

import dev.ridill.mym.settings.data.remote.dto.GDriveFileDto
import dev.ridill.mym.settings.data.remote.dto.GDriveFilesListResponse
import dev.ridill.mym.settings.data.repository.APP_DATA_SPACE
import dev.ridill.mym.settings.domain.backup.BACKUP_FILE_NAME
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

interface GDriveApi {

    @Multipart
    @POST("upload/drive/v3/files?uploadType=multipart")
    suspend fun uploadFile(
        @Header(AUTH_HEADER_KEY) token: String,
        @Part(METADATA_PART_KEY) metadata: RequestBody,
        @Part file: MultipartBody.Part
    ): GDriveFileDto

    @GET("drive/v3/files?orderBy=recency&q=trashed=false&spaces=appDataFolder")
    suspend fun getFilesInAppDataFolder(
        @Header(AUTH_HEADER_KEY) token: String
    ): GDriveFilesListResponse

    @GET("drive/v3/files?orderBy=createdTime desc&q=name contains '$BACKUP_FILE_NAME' and trashed=false&spaces=$APP_DATA_SPACE")
    suspend fun getBackupFilesList(
        @Header(AUTH_HEADER_KEY) token: String
    ): GDriveFilesListResponse

    @Streaming
    @GET("drive/v3/files/{fileId}?alt=media&acknowledgeAbuse=true")
    suspend fun downloadFile(
        @Header(AUTH_HEADER_KEY) token: String,
        @Path("fileId") fileId: String
    ): Response<ResponseBody>

    @DELETE("drive/v3/files/{fileId}")
    suspend fun deleteFile(
        @Header(AUTH_HEADER_KEY) token: String,
        @Path("fileId") fileId: String
    )
}

private const val AUTH_HEADER_KEY = "Authorization"

const val METADATA_PART_KEY = "Metadata"
const val MEDIA_PART_KEY = "Media"