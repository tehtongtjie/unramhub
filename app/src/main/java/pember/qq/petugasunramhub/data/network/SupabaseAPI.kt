package pember.qq.petugasunramhub.data.network

import pember.qq.petugasunramhub.data.model.Report
import pember.qq.petugasunramhub.data.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

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
        @Query("select") select: String = "id,title,description,status,is_anonymous,latitude,longitude,created_at,categories(name),users!reports_user_id_fkey(name,nim_nip),report_media(id,file_path,file_type),task_logs(id,old_status,new_status,notes,created_at)"
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
}