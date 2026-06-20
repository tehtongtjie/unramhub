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
        val config = when (categoryId) {
            1 -> CategoryFormConfig(
                showReporterType = true,
                showDateTime = true,
                showLocation = true,
                showEvidence = true
            )
            2 -> CategoryFormConfig(
                showReporterType = false,
                showDateTime = false,
                showLocation = true,
                showEvidence = true
            )
            3 -> CategoryFormConfig(
                showReporterType = false,
                showDateTime = false,
                showLocation = true,
                showEvidence = false
            )
            4 -> CategoryFormConfig(
                showReporterType = false,
                showDateTime = true,
                showLocation = true,
                showEvidence = true
            )
            else -> CategoryFormConfig(
                showReporterType = false,
                showDateTime = true,
                showLocation = true,
                showEvidence = true
            )
        }
        return Result.success(config)
    }
}
