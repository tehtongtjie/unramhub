package pember.qq.petugasunramhub.data.model

import com.google.gson.annotations.SerializedName

data class Report(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("status") val status: String,
    @SerializedName("is_anonymous") val isAnonymous: Boolean,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("categories") val categories: Category?,
    @SerializedName("users") val users: ReportUser?,
    @SerializedName("report_media") val reportMedia: List<ReportMedia>?,
    @SerializedName("task_logs") val taskLogs: List<TaskLog>?
)

data class Category(
    @SerializedName("name") val name: String
)

data class ReportUser(
    @SerializedName("name") val name: String,
    @SerializedName("nim_nip") val nimNip: String
)

data class ReportMedia(
    @SerializedName("id") val id: Long,
    @SerializedName("file_path") val filePath: String,
    @SerializedName("file_type") val fileType: String
)

data class TaskLog(
    @SerializedName("id") val id: Long,
    @SerializedName("old_status") val oldStatus: String?,
    @SerializedName("new_status") val newStatus: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("created_at") val createdAt: String
)