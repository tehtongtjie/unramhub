package pember.qq.petugasunramhub.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pember.qq.petugasunramhub.data.model.User
import pember.qq.petugasunramhub.data.repository.AuthRepository

class LoginViewModel : ViewModel() {

    // Menggunakan inisialisasi langsung agar kompatibel dengan default ViewModelProvider.Factory bawaan 'by viewModels()'
    private val repository = AuthRepository()

    // Menggunakan backing property privat dan mengekspos LiveData immutable ke Activity
    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> get() = _loginState

    fun login(nimNip: String, password: String) {
        val cleanNimNip = nimNip.trim()
        val cleanPassword = password.trim()

        if (cleanNimNip.isBlank() || cleanPassword.isBlank()) {
            _loginState.value = LoginState.Error("NIM/NIP dan password tidak boleh kosong")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                // Memanfaatkan Result Kotlin secara idiomatis dengan blok fold
                repository.login(cleanNimNip, cleanPassword).fold(
                    onSuccess = { user ->
                        _loginState.value = LoginState.Success(user)
                    },
                    onFailure = { throwable ->
                        _loginState.value = LoginState.Error(throwable.message ?: "Login gagal. Silakan coba lagi.")
                    }
                )
            } catch (e: Exception) {
                // Antisipasi pertahanan jika repository melempar exception tak terduga ke coroutine
                _loginState.value = LoginState.Error(e.message ?: "Terjadi kesalahan sistem")
            }
        }
    }
}

// Menggunakan Sealed Interface untuk efisiensi alokasi memori di runtime
sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    data class Success(val user: User) : LoginState
    data class Error(val message: String) : LoginState
}