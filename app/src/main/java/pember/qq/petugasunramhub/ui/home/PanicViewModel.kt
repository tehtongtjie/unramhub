package pember.qq.petugasunramhub.ui.home

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pember.qq.petugasunramhub.utils.error.AppError
import pember.qq.petugasunramhub.utils.error.AppException
import pember.qq.petugasunramhub.utils.error.ErrorMapper
import pember.qq.petugasunramhub.utils.location.LocationProvider
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed interface PanicUiState {
    object Idle : PanicUiState
    data class Loading(val message: String) : PanicUiState
    data class Success(val payloadText: String) : PanicUiState
    data class Error(val error: AppError) : PanicUiState
}

class PanicViewModel(
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _uiState = MutableLiveData<PanicUiState>(PanicUiState.Idle)
    val uiState: LiveData<PanicUiState> get() = _uiState

    fun requestLocation() {
        if (!locationProvider.isLocationEnabled()) {
            _uiState.value = PanicUiState.Error(
                AppError.LocationError("Layanan lokasi tidak aktif. Aktifkan GPS atau lokasi jaringan.")
            )
            return
        }

        _uiState.value = PanicUiState.Loading("Mengambil koordinat terkini...")
        viewModelScope.launch {
            locationProvider.getCurrentLocation().fold(
                onSuccess = { locationWithProvider ->
                    val payloadText = buildEmergencyPayloadText(
                        locationWithProvider.location,
                        locationWithProvider.provider
                    )
                    _uiState.value = PanicUiState.Success(payloadText)
                },
                onFailure = { error ->
                    val appError = if (error is AppException) {
                        error.error
                    } else {
                        ErrorMapper.map(error)
                    }
                    _uiState.value = PanicUiState.Error(appError)
                }
            )
        }
    }

    private fun buildEmergencyPayloadText(location: Location, provider: String): String {
        val requestedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return buildString {
            appendLine("type: emergency_request")
            appendLine("provider: $provider")
            appendLine("latitude: ${"%.6f".format(Locale.US, location.latitude)}")
            appendLine("longitude: ${"%.6f".format(Locale.US, location.longitude)}")
            appendLine("accuracy_meters: ${"%.1f".format(Locale.US, location.accuracy)}")
            append("requested_at: $requestedAt")
        }
    }
}

class PanicViewModelFactory(
    private val locationProvider: LocationProvider
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PanicViewModel::class.java)) {
            return PanicViewModel(locationProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
