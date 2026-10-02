package hu.unicon.szakdoga.model

import com.google.gson.annotations.SerializedName

data class Document(
    @SerializedName("id") val id: Long,
    @SerializedName("folderId") val folderId: Long? = null,
    @SerializedName("title") val title: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("storagePath") val storagePath: String? = null,
    @SerializedName("fileSize") val fileSize: Long? = null,
    @SerializedName("uploadDate") val uploadDate: String? = null
)
