package dev.ridill.mym.settings.data.remote

import dev.ridill.mym.settings.data.remote.dto.GDriveFileMetadataDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface GDriveApi {

    @Multipart
    @POST("/upload/drive/v3/files?uploadType=multipart")
    suspend fun uploadFile(
        @Header(AUTH_HEADER_KEY) token: String,
        @Part(METADATA_PART_KEY) metadata: RequestBody,
        @Part file: MultipartBody.Part
    ): GDriveFileMetadataDto
}

private const val AUTH_HEADER_KEY = "Authorization"

const val METADATA_PART_KEY = "Metadata"
const val MEDIA_PART_KEY = "Media"
const val JSON_MIME_TYPE = "application/json"
const val DB_MIME_TYPE = "application/x-sqlite3"