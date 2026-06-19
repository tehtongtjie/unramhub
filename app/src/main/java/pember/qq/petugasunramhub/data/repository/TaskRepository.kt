package pember.qq.petugasunramhub.data.repository

import pember.qq.petugasunramhub.data.network.RetrofitClient

class TaskRepository {

    suspend fun updateStatus(
        reportId: Long,
        newStatus: String,
        oldStatus: String,
        userId: Long,
        notes: String = "",
        photoPath: String? = null
    ): Result<Unit> {
        return try {
            // Update status laporan — ini yang paling penting
            RetrofitClient.instance.updateStatus(
                id = "eq.$reportId",
                body = mapOf("status" to newStatus)
            )

            // Insert task log — dibungkus try-catch sendiri
            // agar kalau gagal tidak mempengaruhi hasil update status
            try {
                val logBody = mutableMapOf(
                    "report_id" to reportId.toString(),
                    "changed_by" to userId.toString(),
                    "old_status" to oldStatus,
                    "new_status" to newStatus,
                    "notes" to notes
                )
                photoPath?.let { logBody["photo_path"] = it }
                RetrofitClient.instance.insertTaskLog(body = logBody)
            } catch (logError: Exception) {
                android.util.Log.w("TaskRepo", "Task log gagal tapi status sudah terupdate: ${logError.message}")
            }

            Result.success(Unit)
        } catch (e: Throwable) {
            android.util.Log.e("TaskRepo", "Gagal update status: ${e.message}", e)
            Result.failure(Exception(e.message ?: "Gagal update status"))
        }
    }
}