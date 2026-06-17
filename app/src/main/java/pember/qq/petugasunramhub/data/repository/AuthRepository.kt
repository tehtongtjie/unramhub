package pember.qq.petugasunramhub.data.repository

import pember.qq.petugasunramhub.data.model.User
import pember.qq.petugasunramhub.data.network.RetrofitClient

class AuthRepository {

    suspend fun login(nimNip: String, password: String): Result<User> {
        return try {
            val users = RetrofitClient.instance.login(
                nimNip = "eq.$nimNip",
                password = "eq.$password"
            )
            if (users.isEmpty()) {
                Result.failure(Exception("NIM/NIP atau password salah"))
            } else {
                Result.success(users.first())
            }
        } catch (e: Throwable) {
            Result.failure(Exception("Gagal terhubung: ${e.message}"))
        }
    }
}