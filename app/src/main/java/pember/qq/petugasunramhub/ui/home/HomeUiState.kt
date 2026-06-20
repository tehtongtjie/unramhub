package pember.qq.petugasunramhub.ui.home

import pember.qq.petugasunramhub.data.model.Report
import pember.qq.petugasunramhub.utils.error.AppError

sealed interface CategoryUiState {
    object Loading : CategoryUiState
    data class Success(val categories: List<CivitasCategory>) : CategoryUiState
    object Empty : CategoryUiState
    data class Error(val error: AppError) : CategoryUiState
}

sealed interface LostItemUiState {
    object Loading : LostItemUiState
    data class Success(val items: List<CivitasLostItem>) : LostItemUiState
    object Empty : LostItemUiState
    data class Error(val error: AppError) : LostItemUiState
}

sealed interface RecentReportUiState {
    object Loading : RecentReportUiState
    data class Success(val reports: List<Report>) : RecentReportUiState
    object Empty : RecentReportUiState
    data class Error(val error: AppError) : RecentReportUiState
}
