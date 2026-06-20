package pember.qq.petugasunramhub.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pember.qq.petugasunramhub.data.model.HomeCategory
import pember.qq.petugasunramhub.data.network.RetrofitClient
import pember.qq.petugasunramhub.utils.error.AppException
import pember.qq.petugasunramhub.utils.error.ErrorMapper

class CategoryRepository {

    suspend fun getCategories(): Result<List<HomeCategory>> = withContext(Dispatchers.IO) {
        try {
            val categories = RetrofitClient.instance.getCategories().map { dto ->
                HomeCategory(
                    id = dto.id,
                    name = dto.name
                )
            }

            Result.success(categories)
        } catch (e: Throwable) {
            Result.failure(AppException(ErrorMapper.map(e)))
        }
    }
}
