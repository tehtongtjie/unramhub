package pember.qq.petugasunramhub.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long,
    @SerializedName("nim_nip") val nimNip: String,
    val name: String,
    val email: String,
    val role: String,
    @SerializedName("is_active") val isActive: Boolean
)