package dev.ridill.rivo.settings.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class GDriveFileDto(
    @Expose
    @SerializedName("id")
    val id: String,
    @Expose
    @SerializedName("kind")
    val kind: String,
    @Expose
    @SerializedName("mimeType")
    val mimeType: String,
    @Expose
    @SerializedName("name")
    val name: String
)