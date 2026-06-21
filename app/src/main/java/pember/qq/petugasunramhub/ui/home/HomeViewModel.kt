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
import pember.qq.petugasunramhub.data.repository.UserProfileRepository
import pember.qq.petugasunramhub.utils.error.AppError
import pember.qq.petugasunramhub.utils.error.AppException
import pember.qq.petugasunramhub.utils.error.ErrorMapper

class HomeViewModel : ViewModel() {

    private val categoryRepository = CategoryRepository()
    private val lostItemRepository = LostItemRepository()
    private val reportRepository = ReportRepository()
    private val userProfileRepository = UserProfileRepository()
    private val categoryUiMapper = CategoryUiMapper()

    private val _categoryState = MutableLiveData<CategoryUiState>(CategoryUiState.Loading)
    val categoryState: LiveData<CategoryUiState> get() = _categoryState

    private val _lostItemState = MutableLiveData<LostItemUiState>(LostItemUiState.Loading)
    val lostItemState: LiveData<LostItemUiState> get() = _lostItemState

    private val _recentReportState = MutableLiveData<RecentReportUiState>(RecentReportUiState.Loading)
    val recentReportState: LiveData<RecentReportUiState> get() = _recentReportState

    private val _profilePhotoState = MutableLiveData<ProfilePhotoUiState>(ProfilePhotoUiState.Loading)
    val profilePhotoState: LiveData<ProfilePhotoUiState> get() = _profilePhotoState

    fun refresh(userId: Long, force: Boolean = false) {
        if (userId == -1L) {
            _recentReportState.value = RecentReportUiState.Error(AppError.ValidationError("Sesi pengguna tidak valid."))
            return
        }

        // Check if data is already successfully loaded
        val categoriesNeedLoad = force || _categoryState.value !is CategoryUiState.Success
        val lostItemsNeedLoad = force || _lostItemState.value !is LostItemUiState.Success
        val reportsNeedLoad = force || _recentReportState.value !is RecentReportUiState.Success
        val profilePhotoNeedLoad = force || _profilePhotoState.value !is ProfilePhotoUiState.Success

        // Set to Loading only if it is not already loaded (prevents visual flickering)
        if (categoriesNeedLoad) _categoryState.value = CategoryUiState.Loading
        if (lostItemsNeedLoad) _lostItemState.value = LostItemUiState.Loading
        if (reportsNeedLoad) _recentReportState.value = RecentReportUiState.Loading
        if (profilePhotoNeedLoad) _profilePhotoState.value = ProfilePhotoUiState.Loading

        viewModelScope.launch {
            // Fetch categories, lost items, and profile photo only if needed
            val categoriesDeferred = if (categoriesNeedLoad) async { categoryRepository.getCategories() } else null
            val lostItemsDeferred = if (lostItemsNeedLoad) async { lostItemRepository.getLostItems() } else null
            val profilePhotoDeferred = if (profilePhotoNeedLoad) async { userProfileRepository.getProfilePhotoUrl(userId) } else null
            // Always refresh reports in background, but without blocking state (no flicker)
            val reportsDeferred = async { reportRepository.getCivitasReports(userId) }

            categoriesDeferred?.await()?.fold(
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

            lostItemsDeferred?.await()?.fold(
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

            profilePhotoDeferred?.await()?.fold(
                onSuccess = { url ->
                    _profilePhotoState.value = ProfilePhotoUiState.Success(url)
                },
                onFailure = { error ->
                    val appError = if (error is AppException) error.error else ErrorMapper.map(error)
                    _profilePhotoState.value = ProfilePhotoUiState.Error(appError)
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
                    // Show error only if we don't have previously loaded report data (avoids timeout blocker screen)
                    if (reportsNeedLoad) {
                        val appError = if (error is AppException) error.error else ErrorMapper.map(error)
                        _recentReportState.value = RecentReportUiState.Error(appError)
                    }
                }
            )
        }
    }
}
