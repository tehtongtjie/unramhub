package pember.qq.petugasunramhub.ui.form

data class CategoryFormConfig(
    val showReporterType: Boolean,
    val showDateTime: Boolean,
    val showLocation: Boolean,
    val showEvidence: Boolean
)

interface CategoryFormConfigurationProvider {
    suspend fun getConfiguration(categoryId: Int): Result<CategoryFormConfig>
}

class DefaultCategoryFormConfigurationProvider : CategoryFormConfigurationProvider {
    override suspend fun getConfiguration(categoryId: Int): Result<CategoryFormConfig> {
        val config = CategoryFormConfig(
            showReporterType = true,
            showDateTime = true,
            showLocation = true,
            showEvidence = true
        )
        return Result.success(config)
    }
}
