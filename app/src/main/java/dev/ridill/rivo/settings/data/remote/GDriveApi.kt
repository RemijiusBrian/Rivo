package dev.ridill.rivo.settings.data.remote

import dev.ridill.rivo.settings.data.remote.dto.GDriveFileDto
import dev.ridill.rivo.settings.data.remote.dto.GDriveFilesListResponse
import dev.ridill.rivo.settings.data.repository.APP_DATA_SPACE
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface GDriveApi {
    @Multipart
    @POST("upload/drive/v3/files?spaces=$APP_DATA_SPACE&fields=$QUERY_PARAM_FIELDS")
    suspend fun createFolder(
        @Part(METADATA_PART_KEY) metadata: RequestBody
    ): GDriveFileDto

    @Multipart
    @POST("upload/drive/v3/files?uploadType=multipart&spaces=$APP_DATA_SPACE&&fields=$QUERY_PARAM_FIELDS")
    suspend fun uploadFile(
        @Part(METADATA_PART_KEY) metadata: RequestBody,
        @Part file: MultipartBody.Part
    ): GDriveFileDto

    @GET("drive/v3/files?spaces=$APP_DATA_SPACE&fields=$QUERY_PARAM_FILE_FIELDS")
    suspend fun getFilesList(
        @Query("q") q: String,
        @Query("orderBy") orderByConditions: String = DEFAULT_ORDER_BY
    ): GDriveFilesListResponse

    @Streaming
    @GET("drive/v3/files/{fileId}?alt=media&acknowledgeAbuse=true")
    suspend fun downloadFile(
        @Path("fileId") fileId: String
    ): Response<ResponseBody>

    @DELETE("drive/v3/files/{fileId}")
    suspend fun deleteFile(
        @Path("fileId") fileId: String
    ): Response<Void>
}

private const val DEFAULT_ORDER_BY = "createdTime desc"
private const val QUERY_PARAM_FILE_FIELDS = "files(id,name,parents)"
private const val QUERY_PARAM_FIELDS = "id,name,parents"
const val METADATA_PART_KEY = "Metadata"
const val MEDIA_PART_KEY = "Media"