package pember.qq.petugasunramhub.data.repository

import pember.qq.petugasunramhub.data.model.User
import pember.qq.petugasunramhub.data.network.RetrofitClient

class AuthRepository {

    suspend fun login(nimNip: String, password: String): Result<User> {
        return try {
            val api = try {
                RetrofitClient.instance
            } catch (e: Throwable) {
                android.util.Log.e("AuthRepository", "RetrofitClient initialization failed", e)
                return Result.failure(Exception("Gagal inisialisasi layanan data: ${e.message}"))
            }

            val users = api.login(
                nimNip = "eq.$nimNip",
                password = "eq.$password"
            )
            if (users.isEmpty()) {
                Result.failure(Exception("NIM/NIP atau password salah"))
            } else {
                Result.success(users.first())
            }
        } catch (e: Throwable) {
            android.util.Log.e("AuthRepository", "Error during login", e)
            Result.failure(Exception("Gagal terhubung: ${e.localizedMessage ?: e.javaClass.simpleName}"))
        }
    }
}