package dev.ridill.mym.settings.data.remote.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class GDriveFileMetadataDto(
    @Expose
    @SerializedName("name")
    val fileName: String,

    @Expose
    @SerializedName("mimeType")
    val mimeType: String,

    @Expose
    @SerializedName("parents")
    val parents: List<String>
)