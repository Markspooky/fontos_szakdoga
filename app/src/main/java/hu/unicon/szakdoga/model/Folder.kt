package hu.unicon.szakdoga.model

import com.google.gson.annotations.SerializedName

data class Folder(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("parentId") val parentId: Long? = null
)
