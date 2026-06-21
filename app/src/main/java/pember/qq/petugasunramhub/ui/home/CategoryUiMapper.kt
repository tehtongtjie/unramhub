package pember.qq.petugasunramhub.ui.home

import pember.qq.petugasunramhub.R
import pember.qq.petugasunramhub.data.model.HomeCategory
import pember.qq.petugasunramhub.data.model.HomeCategoryKind
import pember.qq.petugasunramhub.data.repository.HomeCategoryClassifier

class CategoryUiMapper(
    private val classifier: HomeCategoryClassifier = HomeCategoryClassifier()
) {

    fun map(categories: List<HomeCategory>): List<CivitasCategory> {
        return categories
            .sortedBy { displayOrder(it) }
            .map { category ->
                CivitasCategory(
                    id = category.id,
                    label = displayLabel(category),
                    iconResId = displayIcon(category)
                )
            }
    }

    private fun displayOrder(category: HomeCategory): Int {
        return when (classifier.classify(category)) {
            HomeCategoryKind.VIOLENCE_AND_HARASSMENT -> 0
            HomeCategoryKind.FACILITY_DAMAGE -> 1
            HomeCategoryKind.EMERGENCY -> 2
            HomeCategoryKind.LOST_AND_FOUND -> 3
            HomeCategoryKind.OTHER -> 4
        }
    }

    private fun displayLabel(category: HomeCategory): String {
        return when (classifier.classify(category)) {
            HomeCategoryKind.VIOLENCE_AND_HARASSMENT -> "Kekerasan/\nPelecehan"
            HomeCategoryKind.FACILITY_DAMAGE -> "Kerusakan\nFasilitas"
            HomeCategoryKind.EMERGENCY -> "Bencana/\nDarurat"
            HomeCategoryKind.LOST_AND_FOUND -> "Barang Hilang\n/Temuan"
            HomeCategoryKind.OTHER -> category.name
        }
    }

    private fun displayIcon(category: HomeCategory): Int {
        return when (classifier.classify(category)) {
            HomeCategoryKind.VIOLENCE_AND_HARASSMENT -> R.drawable.ic_kekerasan
            HomeCategoryKind.FACILITY_DAMAGE -> R.drawable.ic_kerusakan
            HomeCategoryKind.EMERGENCY -> R.drawable.ic_kebakaran
            HomeCategoryKind.LOST_AND_FOUND -> R.drawable.ic_barang_hilang
            HomeCategoryKind.OTHER -> R.drawable.ic_lainnya
        }
    }
}
