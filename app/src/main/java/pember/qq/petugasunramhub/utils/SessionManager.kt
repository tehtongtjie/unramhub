package pember.qq.petugasunramhub.utils

import android.content.Context
import pember.qq.petugasunramhub.data.model.User

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("unramhub_session", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        prefs.edit().apply {
            putLong("user_id", user.id)
            putString("nim_nip", user.nimNip)
            putString("name", user.name)
            putString("email", user.email)
            putString("role", user.role)
            apply()
        }
    }

    fun getUser(): User? {
        val id = prefs.getLong("user_id", -1)
        if (id == -1L) return null
        
        return User(
            id = id,
            nimNip = prefs.getString("nim_nip", "") ?: "",
            name = prefs.getString("name", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            role = prefs.getString("role", "") ?: "",
            isActive = true // Default true karena session tersimpan
        )
    }

    fun getUserId(): Long = prefs.getLong("user_id", -1)
    fun getName(): String? = prefs.getString("name", null)
    fun getRole(): String? = prefs.getString("role", null)
    fun isLoggedIn(): Boolean = getUserId() != -1L
    fun logout() = prefs.edit().clear().apply()
}
