package pember.qq.petugasunramhub.data.model

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("show_reporter_type") val showReporterType: Boolean? = null,
    @SerializedName("show_datetime") val showDateTime: Boolean? = null,
    @SerializedName("show_location") val showLocation: Boolean? = null,
    @SerializedName("show_evidence") val showEvidence: Boolean? = null
)

data class LostItemReportDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("categories") val category: LostItemCategoryDto?,
    @SerializedName("report_media") val reportMedia: List<LostItemMediaDto>?
)

data class LostItemCategoryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class LostItemMediaDto(
    @SerializedName("file_path") val filePath: String
)
