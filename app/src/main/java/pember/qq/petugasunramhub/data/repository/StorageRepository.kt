package pember.qq.petugasunramhub.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import pember.qq.petugasunramhub.BuildConfig
import java.io.File

class StorageRepository {

    private val client = OkHttpClient()

    suspend fun uploadFoto(file: File, reportId: Long): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Cek ukuran max 2MB
            val maxSize = 2 * 1024 * 1024 // 2MB
            if (file.length() > maxSize) {
                return@withContext Result.failure(Exception("Ukuran foto melebihi 2MB"))
            }

            val fileName = "bukti_${reportId}_${System.currentTimeMillis()}.jpg"
            val url = "${BuildConfig.SUPABASE_URL}/storage/v1/object/task-evidence/$fileName"
            
            val body = file.readBytes().toRequestBody("image/jpeg".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_KEY}")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val publicUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/task-evidence/$fileName"
                Result.success(publicUrl)
            } else {
                Result.failure(Exception("Upload gagal: ${response.code}"))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
