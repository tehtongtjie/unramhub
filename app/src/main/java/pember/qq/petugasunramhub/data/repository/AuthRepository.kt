package pember.qq.petugasunramhub.data.repository

import pember.qq.petugasunramhub.data.model.User
import pember.qq.petugasunramhub.data.network.RetrofitClient
import pember.qq.petugasunramhub.utils.error.AppError
import pember.qq.petugasunramhub.utils.error.AppException
import pember.qq.petugasunramhub.utils.error.ErrorMapper

class AuthRepository {

    suspend fun login(nimNip: String, password: String): Result<User> {
        return try {
            val api = try {
                RetrofitClient.instance
            } catch (e: Throwable) {
                android.util.Log.e("AuthRepository", "RetrofitClient initialization failed", e)
                return Result.failure(AppException(AppError.ConfigurationError("Gagal inisialisasi layanan data: ${e.message}", e)))
            }

            val users = api.login(
                nimNip = "eq.$nimNip",
                password = "eq.$password"
            )
            if (users.isEmpty()) {
                Result.failure(AppException(AppError.AuthenticationError("NIM/NIP atau password salah")))
            } else {
                Result.success(users.first())
            }
        } catch (e: Throwable) {
            android.util.Log.e("AuthRepository", "Error during login", e)
            val mapped = ErrorMapper.map(e)
            Result.failure(AppException(mapped))
        }
    }
}