package pember.qq.petugasunramhub.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pember.qq.petugasunramhub.data.model.User
import pember.qq.petugasunramhub.data.repository.UserProfileRepository
import pember.qq.petugasunramhub.utils.SessionManager
import pember.qq.petugasunramhub.utils.error.AppError
import pember.qq.petugasunramhub.utils.error.AppException
import pember.qq.petugasunramhub.utils.error.ErrorMapper
import java.io.File

sealed interface EditProfileUiState {
    object Idle : EditProfileUiState
    object Loading : EditProfileUiState
    data class Success(val user: User, val profilePhotoUrl: String?) : EditProfileUiState
    data class Error(val error: AppError) : EditProfileUiState
}

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userProfileRepository = UserProfileRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableLiveData<EditProfileUiState>(EditProfileUiState.Idle)
    val uiState: LiveData<EditProfileUiState> get() = _uiState

    private val _profilePhotoUrl = MutableLiveData<String?>()
    val profilePhotoUrl: LiveData<String?> get() = _profilePhotoUrl

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> get() = _saveSuccess

    fun loadInitialData() {
        val currentUser = sessionManager.getUser()
        if (currentUser == null) {
            _uiState.value = EditProfileUiState.Error(AppError.ValidationError("Sesi pengguna tidak ditemukan."))
            return
        }

        _uiState.value = EditProfileUiState.Loading
        viewModelScope.launch {
            userProfileRepository.getProfilePhotoUrl(currentUser.id).fold(
                onSuccess = { url ->
                    _profilePhotoUrl.value = url
                    _uiState.value = EditProfileUiState.Success(currentUser, url)
                },
                onFailure = { error ->
                    // Set photo state to null but keep user data
                    _profilePhotoUrl.value = null
                    _uiState.value = EditProfileUiState.Success(currentUser, null)
                }
            )
        }
    }

    fun saveProfile(name: String, email: String, imageFile: File?) {
        val currentUser = sessionManager.getUser()
        if (currentUser == null) {
            _uiState.value = EditProfileUiState.Error(AppError.ValidationError("Sesi pengguna tidak ditemukan."))
            return
        }

        if (name.isBlank()) {
            _uiState.value = EditProfileUiState.Error(AppError.ValidationError("Nama tidak boleh kosong."))
            return
        }

        if (email.isBlank()) {
            _uiState.value = EditProfileUiState.Error(AppError.ValidationError("Email tidak boleh kosong."))
            return
        }

        _uiState.value = EditProfileUiState.Loading
        viewModelScope.launch {
            try {
                // 1. Upload photo if selected
                var updatedPhotoUrl = _profilePhotoUrl.value
                if (imageFile != null) {
                    val uploadResult = userProfileRepository.uploadProfilePhoto(currentUser.id, imageFile)
                    uploadResult.fold(
                        onSuccess = { url ->
                            updatedPhotoUrl = url
                            _profilePhotoUrl.value = url
                        },
                        onFailure = { error ->
                            val appError = if (error is AppException) error.error else ErrorMapper.map(error)
                            _uiState.value = EditProfileUiState.Error(appError)
                            return@launch
                        }
                    )
                }

                // 2. Update text fields in Supabase users table
                userProfileRepository.updateProfile(currentUser.id, name, email).fold(
                    onSuccess = {
                        // 3. Update local session manager cache
                        val updatedUser = User(
                            id = currentUser.id,
                            nimNip = currentUser.nimNip,
                            name = name,
                            email = email,
                            role = currentUser.role,
                            isActive = currentUser.isActive
                        )
                        sessionManager.saveUser(updatedUser)
                        _uiState.value = EditProfileUiState.Success(updatedUser, updatedPhotoUrl)
                        _saveSuccess.value = true
                    },
                    onFailure = { error ->
                        val appError = if (error is AppException) error.error else ErrorMapper.map(error)
                        _uiState.value = EditProfileUiState.Error(appError)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = EditProfileUiState.Error(ErrorMapper.map(e))
            }
        }
    }
}
