package pember.qq.petugasunramhub.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pember.qq.petugasunramhub.data.model.User
import pember.qq.petugasunramhub.data.repository.AuthRepository
import pember.qq.petugasunramhub.utils.error.AppError
import pember.qq.petugasunramhub.utils.error.AppException
import pember.qq.petugasunramhub.utils.error.ErrorMapper

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> get() = _loginState

    fun login(nimNip: String, password: String) {
        val cleanNimNip = nimNip.trim()
        val cleanPassword = password.trim()

        if (cleanNimNip.isBlank() || cleanPassword.isBlank()) {
            _loginState.value = LoginState.Error(AppError.ValidationError("NIM/NIP dan password tidak boleh kosong"))
            return
        }

        val prefix = cleanNimNip.uppercase()
        if (prefix.startsWith("OFF") || prefix.startsWith("ADM")) {
            _loginState.value = LoginState.Error(AppError.ValidationError("Akses ditolak: Aplikasi ini hanya untuk Civitas."))
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                repository.login(cleanNimNip, cleanPassword).fold(
                    onSuccess = { user ->
                        _loginState.value = LoginState.Success(user)
                    },
                    onFailure = { throwable ->
                        val appError = if (throwable is AppException) {
                            throwable.error
                        } else {
                            ErrorMapper.map(throwable)
                        }
                        _loginState.value = LoginState.Error(appError)
                    }
                )
            } catch (e: Throwable) {
                _loginState.value = LoginState.Error(ErrorMapper.map(e))
            }
        }
    }
}

sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    data class Success(val user: User) : LoginState
    data class Error(val error: AppError) : LoginState
}