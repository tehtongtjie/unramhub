package pember.qq.petugasunramhub.data.network

import pember.qq.petugasunramhub.data.model.CategoryDto
import pember.qq.petugasunramhub.data.model.LostItemReportDto
import pember.qq.petugasunramhub.data.model.Report
import pember.qq.petugasunramhub.data.model.ReportRequest
import pember.qq.petugasunramhub.data.model.User
import pember.qq.petugasunramhub.data.model.ReportMediaRequest
import pember.qq.petugasunramhub.data.model.UserProfilePhotoDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

    @GET("categories")
    suspend fun getCategories(
        @Query("select") select: String = "id,name"
    ): List<CategoryDto>

    @GET("users")
    suspend fun login(
        @Query("nim_nip") nimNip: String,
        @Query("password") password: String,
        @Query("is_active") isActive: String = "eq.true",
        @Query("select") select: String = "id,nim_nip,name,email,role,is_active"
    ): List<User>

    @GET("reports")
    suspend fun getMyReports(
        @Query("assigned_to") assignedTo: String,
        @Query("select") select: String = "id,title,status,created_at,categories(name)"
    ): List<Report>

    @GET("reports")
    suspend fun getReportDetail(
        @Query("id") id: String,
        @Query("select") select: String = "id,title,description,status,is_anonymous,latitude,longitude,created_at,categories(name),users!reports_user_id_fkey(name,nim_nip),report_media(id,file_path,file_type),task_logs(id,old_status,new_status,notes,created_at),incident_location,incident_datetime,reporter_type"
    ): List<Report>

    @POST("task_logs")
    suspend fun insertTaskLog(
        @Body body: Map<String, String>
    ): retrofit2.Response<Unit>

    @PATCH("reports")
    suspend fun updateStatus(
        @Query("id") id: String,
        @Body body: Map<String, String>
    ): retrofit2.Response<Unit>

    @Headers("Prefer: return=representation")
    @POST("reports")
    suspend fun createReport(
        @Body body: ReportRequest
    ): List<Report>

    @POST("report_media")
    suspend fun insertReportMedia(
        @Body body: ReportMediaRequest
    ): retrofit2.Response<Unit>

    @GET("reports")
    suspend fun getCivitasReports(
        @Query("user_id") userId: String,
        @Query("select") select: String = "id,title,description,status,is_anonymous,latitude,longitude,created_at,categories(name),incident_location,incident_datetime,reporter_type"
    ): List<Report>

    @GET("reports")
    suspend fun getHomeLostItemReports(
        @Query("select") select: String = "id,title,created_at,categories(id,name),report_media(file_path)",
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int = 10
    ): List<LostItemReportDto>

    @GET("user_profile_photos")
    suspend fun getUserProfilePhoto(
        @Query("user_id") userId: String,
        @Query("select") select: String = "id,user_id,file_path"
    ): List<UserProfilePhotoDto>

    @Headers("Prefer: resolution=merge-duplicates")
    @POST("user_profile_photos")
    suspend fun upsertUserProfilePhoto(
        @Body body: Map<String, @JvmSuppressWildcards Any>,
        @Query("on_conflict") onConflict: String = "user_id"
    ): retrofit2.Response<Unit>

    @PATCH("users")
    suspend fun updateProfile(
        @Query("id") idFilter: String,
        @Body body: Map<String, String>
    ): retrofit2.Response<Unit>
}
