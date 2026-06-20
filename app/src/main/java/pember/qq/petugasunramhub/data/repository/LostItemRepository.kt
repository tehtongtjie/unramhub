package pember.qq.petugasunramhub.data.repository

import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pember.qq.petugasunramhub.data.model.HomeCategory
import pember.qq.petugasunramhub.data.network.RetrofitClient
import pember.qq.petugasunramhub.ui.home.CivitasLostItem

class LostItemRepository(
    private val categoryRepository: CategoryRepository = CategoryRepository(),
    private val categoryClassifier: HomeCategoryClassifier = HomeCategoryClassifier()
) {

    suspend fun getLostItems(limit: Int = 10): Result<List<CivitasLostItem>> = withContext(Dispatchers.IO) {
        try {
            val categories = categoryRepository.getCategories().getOrThrow()
            val lostAndFoundCategoryIds = categories
                .filter { categoryClassifier.isLostAndFound(it) }
                .map(HomeCategory::id)
                .toSet()

            val items = RetrofitClient.instance.getHomeLostItemReports(limit = 50)
                .filter { dto ->
                    dto.category?.id in lostAndFoundCategoryIds
                }
                .map { dto ->
                    CivitasLostItem(
                        id = dto.id,
                        title = dto.title,
                        date = formatDate(dto.createdAt),
                        timeAgo = formatRelativeTime(dto.createdAt),
                        imageUrl = dto.reportMedia
                            ?.firstOrNull()
                            ?.filePath
                            ?.takeIf { it.isNotBlank() }
                    )
                }
                .take(limit)

            Result.success(items)
        } catch (e: Throwable) {
            Result.failure(pember.qq.petugasunramhub.utils.error.AppException(pember.qq.petugasunramhub.utils.error.ErrorMapper.map(e)))
        }
    }

    private fun formatDate(isoString: String): String {
        return try {
            val parsed = OffsetDateTime.parse(isoString)
            parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("id", "ID")))
        } catch (_: Exception) {
            isoString
        }
    }

    private fun formatRelativeTime(isoString: String): String {
        return try {
            val createdAt = OffsetDateTime.parse(isoString)
            val duration = Duration.between(createdAt, OffsetDateTime.now())
            val minutes = duration.toMinutes().coerceAtLeast(0)

            when {
                minutes < 60 -> "Dilaporkan ${minutes.coerceAtLeast(1)} menit lalu"
                minutes < 1440 -> "Dilaporkan ${minutes / 60} jam lalu"
                else -> "Dilaporkan ${minutes / 1440} hari lalu"
            }
        } catch (_: Exception) {
            "Dilaporkan baru-baru ini"
        }
    }
}
