package pember.qq.petugasunramhub.ui.form

import pember.qq.petugasunramhub.data.repository.CategoryConfigurationRepository

class RemoteCategoryConfigurationProvider(
    private val repository: CategoryConfigurationRepository = CategoryConfigurationRepository(),
    private val localFallback: DefaultCategoryFormConfigurationProvider = DefaultCategoryFormConfigurationProvider()
) : CategoryFormConfigurationProvider {

    override suspend fun getConfiguration(categoryId: Int): Result<CategoryFormConfig> {
        return Result.success(
            CategoryFormConfig(
                showReporterType = true,
                showDateTime = true,
                showLocation = true,
                showEvidence = true
            )
        )
    }
}
