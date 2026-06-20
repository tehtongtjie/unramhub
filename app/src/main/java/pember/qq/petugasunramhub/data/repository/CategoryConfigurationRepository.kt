package pember.qq.petugasunramhub.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pember.qq.petugasunramhub.data.model.CategoryDto
import pember.qq.petugasunramhub.data.network.RetrofitClient

class CategoryConfigurationRepository {

    suspend fun getCategoryConfigurations(): Result<List<CategoryDto>> = withContext(Dispatchers.IO) {
        try {
            val selectColumns = "id,name,show_reporter_type,show_datetime,show_location,show_evidence"
            val categories = RetrofitClient.instance.getCategories(select = selectColumns)
            Result.success(categories)
        } catch (e: Throwable) {
            Result.failure(pember.qq.petugasunramhub.utils.error.AppException(pember.qq.petugasunramhub.utils.error.ErrorMapper.map(e)))
        }
    }
}
