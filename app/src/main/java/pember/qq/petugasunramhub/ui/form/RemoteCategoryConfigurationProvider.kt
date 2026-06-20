package pember.qq.petugasunramhub.ui.form

import pember.qq.petugasunramhub.data.repository.CategoryConfigurationRepository

class RemoteCategoryConfigurationProvider(
    private val repository: CategoryConfigurationRepository = CategoryConfigurationRepository(),
    private val localFallback: DefaultCategoryFormConfigurationProvider = DefaultCategoryFormConfigurationProvider()
) : CategoryFormConfigurationProvider {

    override suspend fun getConfiguration(categoryId: Int): Result<CategoryFormConfig> {
        return repository.getCategoryConfigurations().fold(
            onSuccess = { remoteCategories ->
                val matchingCategory = remoteCategories.find { it.id == categoryId }
                if (matchingCategory != null &&
                    matchingCategory.showReporterType != null &&
                    matchingCategory.showDateTime != null &&
                    matchingCategory.showLocation != null &&
                    matchingCategory.showEvidence != null
                ) {
                    val config = CategoryFormConfig(
                        showReporterType = matchingCategory.showReporterType,
                        showDateTime = matchingCategory.showDateTime,
                        showLocation = matchingCategory.showLocation,
                        showEvidence = matchingCategory.showEvidence
                    )
                    Result.success(config)
                } else {
                    // Falls back to local config if category not found or has null config fields
                    localFallback.getConfiguration(categoryId)
                }
            },
            onFailure = {
                // Falls back to local config on server/network failure
                localFallback.getConfiguration(categoryId)
            }
        )
    }
}
