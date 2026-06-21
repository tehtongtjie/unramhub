package pember.qq.petugasunramhub.ui.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pember.qq.petugasunramhub.data.repository.ReportRepository
import pember.qq.petugasunramhub.utils.error.AppError
import pember.qq.petugasunramhub.utils.error.AppException
import pember.qq.petugasunramhub.utils.error.ErrorMapper
import java.io.File

sealed interface FormUiState {
    object LoadingConfig : FormUiState
    object Ready : FormUiState
    data class Error(val error: AppError) : FormUiState
}

sealed interface FormSubmissionState {
    object Idle : FormSubmissionState
    object Loading : FormSubmissionState
    data class ValidationError(val error: AppError) : FormSubmissionState
    data class UploadError(val error: AppError) : FormSubmissionState
    data class NetworkError(val error: AppError) : FormSubmissionState
    object Success : FormSubmissionState
}

data class FormInput(
    val description: String,
    val location: String,
    val reporterName: String,
    val reporterNim: String,
    val contact: String,
    val reporterTypePosition: Int,
    val reporterTypeSelectedValue: String,
    val eventTime: String,
    val eventDate: String
)

class FormLaporanViewModel(
    private val reportRepository: ReportRepository = ReportRepository(),
    private val configProvider: CategoryFormConfigurationProvider = RemoteCategoryConfigurationProvider()
) : ViewModel() {

    private val _uiState = MutableLiveData<FormUiState>(FormUiState.LoadingConfig)
    val uiState: LiveData<FormUiState> get() = _uiState

    private val _submissionState = MutableLiveData<FormSubmissionState>(FormSubmissionState.Idle)
    val submissionState: LiveData<FormSubmissionState> get() = _submissionState

    private val _formConfig = MutableLiveData<CategoryFormConfig>()
    val formConfig: LiveData<CategoryFormConfig> get() = _formConfig

    fun loadConfiguration(categoryId: Int) {
        _uiState.value = FormUiState.LoadingConfig
        viewModelScope.launch {
            configProvider.getConfiguration(categoryId).fold(
                onSuccess = { config ->
                    _formConfig.value = config
                    _uiState.value = FormUiState.Ready
                },
                onFailure = { error ->
                    val appError = if (error is AppException) error.error else ErrorMapper.map(error)
                    _uiState.value = FormUiState.Error(appError)
                }
            )
        }
    }

    fun submitReport(
        input: FormInput,
        categoryId: Int,
        categoryName: String,
        isAnonymous: Boolean,
        imageFiles: List<File>,
        userId: Long
    ) {
        val config = _formConfig.value
        if (config == null) {
            _submissionState.value = FormSubmissionState.ValidationError(
                AppError.ValidationError("Konfigurasi form belum siap.")
            )
            return
        }

        // 1. Validation Logic
        val validationResult = validateInput(input, config, isAnonymous)
        if (validationResult != null) {
            _submissionState.value = FormSubmissionState.ValidationError(
                AppError.ValidationError(validationResult)
            )
            return
        }

        _submissionState.value = FormSubmissionState.Loading

        // 2. Submission Workflow
        viewModelScope.launch {
            // Clean-up helper for temp files
            fun cleanUpTempFiles() {
                for (file in imageFiles) {
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }

            try {
                val title = "$categoryName - ${if (isAnonymous) "Anonim" else input.reporterName}"
                
                // Parse coordinates
                var lat: Double? = null
                var lon: Double? = null
                val regex = "\\(([-+]?\\d+\\.\\d+),\\s*([-+]?\\d+\\.\\d+)\\)".toRegex()
                val matchResult = regex.find(input.location)
                if (matchResult != null) {
                    lat = matchResult.groupValues[1].toDoubleOrNull()
                    lon = matchResult.groupValues[2].toDoubleOrNull()
                }

                // Parse event date/time to ISO format if visible
                val combinedDateTime = if (config.showDateTime) {
                    formatIsoOffsetDateTime(input.eventDate, input.eventTime)
                } else {
                    null
                }

                val createResult = reportRepository.createReport(
                    userId = userId,
                    categoryId = categoryId,
                    title = title,
                    description = input.description,
                    latitude = lat,
                    longitude = lon,
                    isAnonymous = isAnonymous,
                    incidentLocation = input.location,
                    incidentDatetime = combinedDateTime,
                    reporterType = if (config.showReporterType) input.reporterTypeSelectedValue else null
                )

                createResult.fold(
                    onSuccess = { report ->
                        if (imageFiles.isNotEmpty()) {
                            var anyUploadFailed = false
                            var uploadErrorDetail: Throwable? = null

                            for (file in imageFiles) {
                                val uploadResult = reportRepository.uploadReportMedia(file, report.id)
                                uploadResult.fold(
                                    onSuccess = { publicUrl ->
                                        val mediaResult = reportRepository.insertReportMedia(report.id, publicUrl)
                                        mediaResult.fold(
                                            onSuccess = {
                                                // Success for this file, continue
                                            },
                                            onFailure = { mediaError ->
                                                anyUploadFailed = true
                                                uploadErrorDetail = mediaError
                                            }
                                        )
                                    },
                                    onFailure = { uploadError ->
                                        anyUploadFailed = true
                                        uploadErrorDetail = uploadError
                                    }
                                )
                                if (anyUploadFailed) break
                            }

                            cleanUpTempFiles()

                            if (anyUploadFailed) {
                                val errorMsg = uploadErrorDetail
                                val appError = if (errorMsg is AppException) errorMsg.error else ErrorMapper.map(errorMsg ?: Exception("Gagal mengupload beberapa bukti"))
                                _submissionState.value = FormSubmissionState.UploadError(appError)
                            } else {
                                _submissionState.value = FormSubmissionState.Success
                            }
                        } else {
                            _submissionState.value = FormSubmissionState.Success
                        }
                    },
                    onFailure = { createError ->
                        cleanUpTempFiles()
                        val appError = if (createError is AppException) createError.error else ErrorMapper.map(createError)
                        _submissionState.value = FormSubmissionState.NetworkError(appError)
                    }
                )
            } catch (e: Exception) {
                cleanUpTempFiles()
                _submissionState.value = FormSubmissionState.NetworkError(ErrorMapper.map(e))
            }
        }
    }

    private fun formatIsoOffsetDateTime(dateStr: String, timeStr: String): String? {
        return try {
            if (dateStr.isBlank() || timeStr.isBlank()) return null
            
            val dateParts = dateStr.split("/")
            if (dateParts.size != 3) return null
            
            val day = dateParts[0].toInt()
            val month = dateParts[1].toInt()
            val year = dateParts[2].toInt()
            
            val timeParts = timeStr.split(":")
            if (timeParts.size != 2) return null
            
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            
            val localDateTime = java.time.LocalDateTime.of(year, month, day, hour, minute)
            val offsetDateTime = java.time.OffsetDateTime.of(
                localDateTime, 
                java.time.ZoneId.systemDefault().rules.getOffset(localDateTime)
            )
            offsetDateTime.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } catch (_: Exception) {
            null
        }
    }

    private fun validateInput(input: FormInput, config: CategoryFormConfig, isAnonymous: Boolean): String? {
        if (config.showReporterType && input.reporterTypePosition == 0) {
            return "Pilih Jenis Pelapor!"
        }

        if (input.description.isEmpty()) {
            return "Deskripsi kejadian tidak boleh kosong"
        }

        if (config.showLocation && input.location.isEmpty()) {
            return "Lokasi kejadian tidak boleh kosong"
        }

        if (!isAnonymous) {
            if (input.reporterName.isEmpty()) {
                return "Nama pelapor tidak boleh kosong"
            }
            if (input.reporterNim.isEmpty()) {
                return "NIM pelapor tidak boleh kosong"
            }
            if (input.contact.isEmpty()) {
                return "Kontak tidak boleh kosong"
            }
        }

        return null
    }

    fun resetSubmissionState() {
        _submissionState.value = FormSubmissionState.Idle
    }
}
