package pember.qq.petugasunramhub.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import pember.qq.petugasunramhub.BuildConfig
import pember.qq.petugasunramhub.data.network.RetrofitClient
import pember.qq.petugasunramhub.utils.error.AppError
import pember.qq.petugasunramhub.utils.error.AppException
import pember.qq.petugasunramhub.utils.error.ErrorMapper
import java.io.File

class UserProfileRepository {

    private val client = OkHttpClient()
    private val api = RetrofitClient.instance

    suspend fun getProfilePhotoUrl(userId: Long): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val photos = api.getUserProfilePhoto("eq.$userId")
            if (photos.isEmpty()) {
                Result.success(null)
            } else {
                val filePath = photos.first().filePath
                val publicUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/photo-profile/$filePath"
                Result.success(publicUrl)
            }
        } catch (e: Throwable) {
            Result.failure(AppException(ErrorMapper.map(e)))
        }
    }

    suspend fun uploadProfilePhoto(userId: Long, file: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileName = "profile_${userId}_${System.currentTimeMillis()}.jpg"
            val url = "${BuildConfig.SUPABASE_URL}/storage/v1/object/photo-profile/${fileName}"

            val body = file.readBytes().toRequestBody("image/jpeg".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_KEY}")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // Upsert reference in user_profile_photos table
                val dbBody = mapOf(
                    "user_id" to userId,
                    "file_path" to fileName
                )
                val dbResponse = api.upsertUserProfilePhoto(dbBody)
                if (dbResponse.isSuccessful) {
                    val publicUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/photo-profile/${fileName}"
                    Result.success(publicUrl)
                } else {
                    val errorBody = dbResponse.errorBody()?.string() ?: ""
                    android.util.Log.e("UserProfileRepository", "Simpan DB metadata gagal: $errorBody")
                    Result.failure(AppException(AppError.UploadError("Simpan DB metadata gagal: $errorBody")))
                }
            } else {
                val errorBody = response.body?.string() ?: ""
                android.util.Log.e("UserProfileRepository", "Upload storage gagal (status: ${response.code}): $errorBody")
                Result.failure(AppException(AppError.UploadError("Upload storage gagal: $errorBody")))
            }
        } catch (e: Throwable) {
            Result.failure(AppException(ErrorMapper.map(e)))
        }
    }

    suspend fun updateProfile(userId: Long, name: String, email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = mapOf(
                "name" to name,
                "email" to email
            )
            val response = api.updateProfile("eq.$userId", body)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                android.util.Log.e("UserProfileRepository", "Gagal memperbarui database user: $errorBody")
                Result.failure(AppException(AppError.ValidationError("Gagal memperbarui profil: $errorBody")))
            }
        } catch (e: Throwable) {
            Result.failure(AppException(ErrorMapper.map(e)))
        }
    }
}
