package pember.qq.petugasunramhub.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import pember.qq.petugasunramhub.data.repository.CategoryRepository
import pember.qq.petugasunramhub.data.repository.LostItemRepository
import pember.qq.petugasunramhub.data.repository.ReportRepository
import pember.qq.petugasunramhub.utils.error.AppError
import pember.qq.petugasunramhub.utils.error.AppException
import pember.qq.petugasunramhub.utils.error.ErrorMapper

class HomeViewModel : ViewModel() {

    private val categoryRepository = CategoryRepository()
    private val lostItemRepository = LostItemRepository()
    private val reportRepository = ReportRepository()
    private val categoryUiMapper = CategoryUiMapper()

    private val _categoryState = MutableLiveData<CategoryUiState>(CategoryUiState.Loading)
    val categoryState: LiveData<CategoryUiState> get() = _categoryState

    private val _lostItemState = MutableLiveData<LostItemUiState>(LostItemUiState.Loading)
    val lostItemState: LiveData<LostItemUiState> get() = _lostItemState

    private val _recentReportState = MutableLiveData<RecentReportUiState>(RecentReportUiState.Loading)
    val recentReportState: LiveData<RecentReportUiState> get() = _recentReportState

    fun refresh(userId: Long) {
        if (userId == -1L) {
            _recentReportState.value = RecentReportUiState.Error(AppError.ValidationError("Sesi pengguna tidak valid."))
            return
        }

        _categoryState.value = CategoryUiState.Loading
        _lostItemState.value = LostItemUiState.Loading
        _recentReportState.value = RecentReportUiState.Loading

        viewModelScope.launch {
            val categoriesDeferred = async { categoryRepository.getCategories() }
            val lostItemsDeferred = async { lostItemRepository.getLostItems() }
            val reportsDeferred = async { reportRepository.getCivitasReports(userId) }

            categoriesDeferred.await().fold(
                onSuccess = { categories ->
                    val uiCategories = categoryUiMapper.map(categories)
                    _categoryState.value = if (uiCategories.isEmpty()) {
                        CategoryUiState.Empty
                    } else {
                        CategoryUiState.Success(uiCategories)
                    }
                },
                onFailure = { error ->
                    val appError = if (error is AppException) error.error else ErrorMapper.map(error)
                    _categoryState.value = CategoryUiState.Error(appError)
                }
            )

            lostItemsDeferred.await().fold(
                onSuccess = { items ->
                    _lostItemState.value = if (items.isEmpty()) {
                        LostItemUiState.Empty
                    } else {
                        LostItemUiState.Success(items)
                    }
                },
                onFailure = { error ->
                    val appError = if (error is AppException) error.error else ErrorMapper.map(error)
                    _lostItemState.value = LostItemUiState.Error(appError)
                }
            )

            reportsDeferred.await().fold(
                onSuccess = { reports ->
                    val limitedReports = reports.take(3)
                    _recentReportState.value = if (limitedReports.isEmpty()) {
                        RecentReportUiState.Empty
                    } else {
                        RecentReportUiState.Success(limitedReports)
                    }
                },
                onFailure = { error ->
                    val appError = if (error is AppException) error.error else ErrorMapper.map(error)
                    _recentReportState.value = RecentReportUiState.Error(appError)
                }
            )
        }
    }
}
