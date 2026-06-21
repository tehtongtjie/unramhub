package pember.qq.petugasunramhub.data.model

import com.google.gson.annotations.SerializedName

data class UserProfilePhotoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("file_path") val filePath: String
)
