package hu.unicon.szakdoga.model

import com.google.gson.annotations.SerializedName

data class Material(
    @SerializedName("id") val id: Long,
    @SerializedName("categoryId") val categoryId: Long? = null,
    @SerializedName("categoryName") val categoryName: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("skuCode") val skuCode: String? = null,
    @SerializedName("details") val details: String? = null,
    @SerializedName("lastUpdate") val lastUpdate: String? = null
)
