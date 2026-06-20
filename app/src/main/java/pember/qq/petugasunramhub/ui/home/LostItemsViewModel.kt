package pember.qq.petugasunramhub.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pember.qq.petugasunramhub.data.repository.LostItemRepository
import pember.qq.petugasunramhub.utils.error.AppError
import pember.qq.petugasunramhub.utils.error.AppException
import pember.qq.petugasunramhub.utils.error.ErrorMapper

sealed interface LostItemsUiState {
    object Loading : LostItemsUiState
    data class Success(val items: List<CivitasLostItem>) : LostItemsUiState
    object Empty : LostItemsUiState
    data class Error(val error: AppError) : LostItemsUiState
}

class LostItemsViewModel(
    private val repository: LostItemRepository = LostItemRepository()
) : ViewModel() {

    private val _uiState = MutableLiveData<LostItemsUiState>(LostItemsUiState.Loading)
    val uiState: LiveData<LostItemsUiState> get() = _uiState

    fun loadLostItems() {
        _uiState.value = LostItemsUiState.Loading
        viewModelScope.launch {
            repository.getLostItems(limit = 50).fold(
                onSuccess = { items ->
                    _uiState.value = if (items.isEmpty()) {
                        LostItemsUiState.Empty
                    } else {
                        LostItemsUiState.Success(items)
                    }
                },
                onFailure = { error ->
                    val appError = if (error is AppException) {
                        error.error
                    } else {
                        ErrorMapper.map(error)
                    }
                    _uiState.value = LostItemsUiState.Error(appError)
                }
            )
        }
    }
}
