package pember.qq.petugasunramhub.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import pember.qq.petugasunramhub.BuildConfig
import pember.qq.petugasunramhub.data.model.Report
import pember.qq.petugasunramhub.data.model.ReportRequest
import pember.qq.petugasunramhub.data.network.RetrofitClient
import pember.qq.petugasunramhub.utils.error.AppError
import pember.qq.petugasunramhub.utils.error.AppException
import pember.qq.petugasunramhub.utils.error.ErrorMapper
import java.io.File

class ReportRepository {

    private val client = OkHttpClient()

    suspend fun createReport(
        userId: Long,
        categoryId: Int,
        title: String,
        description: String,
        latitude: Double?,
        longitude: Double?,
        isAnonymous: Boolean,
        incidentLocation: String?,
        incidentDatetime: String?,
        reporterType: String?
    ): Result<Report> = withContext(Dispatchers.IO) {
        try {
            val requestBody = ReportRequest(
                userId = userId,
                categoryId = categoryId,
                title = title,
                description = description,
                latitude = latitude,
                longitude = longitude,
                isAnonymous = isAnonymous,
                status = "pending",
                incidentLocation = incidentLocation,
                incidentDatetime = incidentDatetime,
                reporterType = reporterType
            )
            val responseList = RetrofitClient.instance.createReport(requestBody)
            if (responseList.isNotEmpty()) {
                Result.success(responseList[0])
            } else {
                Result.failure(AppException(AppError.ValidationError("Gagal membuat laporan: data kosong")))
            }
        } catch (e: Throwable) {
            Result.failure(AppException(ErrorMapper.map(e)))
        }
    }

    suspend fun uploadReportMedia(file: File, reportId: Long): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileName = "bukti_report_${reportId}_${System.currentTimeMillis()}_${file.name}"
            val url = "${BuildConfig.SUPABASE_URL}/storage/v1/object/task-evidence/${fileName}"
            
            val body = file.readBytes().toRequestBody("image/jpeg".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_KEY}")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val publicUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/task-evidence/${fileName}"
                Result.success(publicUrl)
            } else {
                val errorBody = response.body?.string() ?: ""
                android.util.Log.e("ReportRepository", "Upload media gagal (status: ${response.code}): $errorBody")
                Result.failure(AppException(AppError.UploadError("Upload media gagal: $errorBody")))
            }
        } catch (e: Throwable) {
            Result.failure(AppException(ErrorMapper.map(e)))
        }
    }

    suspend fun insertReportMedia(reportId: Long, filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = pember.qq.petugasunramhub.data.model.ReportMediaRequest(
                reportId = reportId,
                filePath = filePath,
                fileType = "image"
            )
            val response = RetrofitClient.instance.insertReportMedia(body)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                android.util.Log.e("ReportRepository", "Gagal menyimpan metadata media (status: ${response.code()}): $errorBody")
                Result.failure(AppException(AppError.UploadError("Gagal menyimpan metadata: $errorBody")))
            }
        } catch (e: Throwable) {
            Result.failure(AppException(ErrorMapper.map(e)))
        }
    }

    suspend fun getCivitasReports(userId: Long): Result<List<Report>> = withContext(Dispatchers.IO) {
        try {
            val responseList = RetrofitClient.instance.getCivitasReports(
                userId = "eq.$userId"
            )
            Result.success(responseList)
        } catch (e: Throwable) {
            Result.failure(AppException(ErrorMapper.map(e)))
        }
    }

    suspend fun getReportDetail(reportId: Long): Result<Report> = withContext(Dispatchers.IO) {
        try {
            val responseList = RetrofitClient.instance.getReportDetail(
                id = "eq.$reportId"
            )
            if (responseList.isNotEmpty()) {
                Result.success(responseList[0])
            } else {
                Result.failure(AppException(AppError.ValidationError("Laporan tidak ditemukan")))
            }
        } catch (e: Throwable) {
            Result.failure(AppException(ErrorMapper.map(e)))
        }
    }
}
