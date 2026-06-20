package pember.qq.petugasunramhub.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pember.qq.petugasunramhub.data.model.Report
import pember.qq.petugasunramhub.data.repository.ReportRepository
import pember.qq.petugasunramhub.utils.error.AppError
import pember.qq.petugasunramhub.utils.error.AppException
import pember.qq.petugasunramhub.utils.error.ErrorMapper

data class DetailReportPresentation(
    val id: Long,
    val title: String,
    val categoryName: String,
    val dateText: String,
    val locationText: String,
    val isAnonymous: Boolean,
    val reporterText: String,
    val descriptionText: String,
    val statusLabel: String,
    val statusColorHex: String,
    val statusBgOpacityColorHex: String,
    val evidenceImageUrl: String?
)

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val report: DetailReportPresentation) : DetailUiState
    object Empty : DetailUiState
    data class Error(val error: AppError) : DetailUiState
}

class DetailLaporanViewModel(
    private val reportRepository: ReportRepository = ReportRepository()
) : ViewModel() {

    private val _uiState = MutableLiveData<DetailUiState>(DetailUiState.Loading)
    val uiState: LiveData<DetailUiState> get() = _uiState

    fun loadReportDetail(reportId: Long) {
        if (reportId == -1L) {
            _uiState.value = DetailUiState.Error(
                AppError.ValidationError("ID Laporan tidak valid.")
            )
            return
        }

        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            reportRepository.getReportDetail(reportId).fold(
                onSuccess = { report ->
                    val presentation = mapToPresentation(report)
                    _uiState.value = DetailUiState.Success(presentation)
                },
                onFailure = { error ->
                    val appError = if (error is AppException) {
                        error.error
                    } else {
                        ErrorMapper.map(error)
                    }
                    _uiState.value = DetailUiState.Error(appError)
                }
            )
        }
    }

    private fun mapToPresentation(report: Report): DetailReportPresentation {
        // Status label mapping
        val statusLabel = when (report.status.lowercase()) {
            "pending" -> "Dalam Antrean (Pending)"
            "assigned" -> "Diterima (Assigned)"
            "processing" -> "Sedang Diproses"
            "completed" -> "Selesai"
            else -> report.status.replaceFirstChar { it.uppercase() }
        }

        // Status text color hex
        val statusColorHex = when (report.status.lowercase()) {
            "pending" -> "#FFC107"
            "assigned" -> "#0D6EFD"
            "processing" -> "#17A2B8"
            "completed" -> "#28A745"
            else -> "#6C757D"
        }

        // Status card background color hex with 15% opacity (#26xxxxxx)
        val statusBgOpacityColorHex = when (report.status.lowercase()) {
            "pending" -> "#26FFC107"
            "assigned" -> "#260D6EFD"
            "processing" -> "#2617A2B8"
            "completed" -> "#2628A745"
            else -> "#266C757D"
        }

        // Location & Description fallback (LEGACY_REPORT_SUPPORT)
        val locationText: String
        val descriptionText: String

        if (report.incidentLocation != null) {
            locationText = report.incidentLocation
            descriptionText = report.description
        } else {
            // LEGACY_REPORT_SUPPORT
            locationText = report.description.substringAfter("Lokasi: ").substringBefore("\n")
            val rawDescription = report.description
            descriptionText = if (rawDescription.contains("Deskripsi Kejadian:\n")) {
                rawDescription.substringAfter("Deskripsi Kejadian:\n")
            } else {
                rawDescription
            }
        }

        // Date-time formatting
        val dateText = formatDateTime(report.createdAt)

        // Reporter profile details
        val reporterName = report.users?.name ?: "Civitas"
        val reporterNim = report.users?.nimNip?.let { " ($it)" } ?: ""
        val reporterText = "$reporterName$reporterNim"

        // Image file attachment url
        val mediaList = report.reportMedia
        val evidenceImageUrl = if (!mediaList.isNullOrEmpty()) {
            mediaList[0].filePath.takeIf { it.isNotBlank() }
        } else {
            null
        }

        return DetailReportPresentation(
            id = report.id,
            title = report.title,
            categoryName = "Kategori: ${report.categories?.name ?: "Lainnya"}",
            dateText = dateText,
            locationText = locationText,
            isAnonymous = report.isAnonymous,
            reporterText = reporterText,
            descriptionText = descriptionText,
            statusLabel = statusLabel,
            statusColorHex = statusColorHex,
            statusBgOpacityColorHex = statusBgOpacityColorHex,
            evidenceImageUrl = evidenceImageUrl
        )
    }

    private fun formatDateTime(isoString: String): String {
        return try {
            val datePart = isoString.substringBefore("T")
            val timePart = isoString.substringAfter("T").take(5)
            val dateSplit = datePart.split("-")
            if (dateSplit.size == 3) {
                "${dateSplit[2]}/${dateSplit[1]}/${dateSplit[0]} $timePart"
            } else {
                isoString
            }
        } catch (_: Exception) {
            isoString
        }
    }
}
