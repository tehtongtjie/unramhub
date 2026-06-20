package pember.qq.petugasunramhub.data.repository

import pember.qq.petugasunramhub.data.model.HomeCategory
import pember.qq.petugasunramhub.data.model.HomeCategoryKind

class HomeCategoryClassifier {

    fun classify(category: HomeCategory): HomeCategoryKind {
        return when (normalize(category.name)) {
            "kekerasan/pelecehan" -> HomeCategoryKind.VIOLENCE_AND_HARASSMENT
            "kerusakan fasilitas" -> HomeCategoryKind.FACILITY_DAMAGE
            "bencana/darurat" -> HomeCategoryKind.EMERGENCY
            "barang hilang/temuan" -> HomeCategoryKind.LOST_AND_FOUND
            else -> HomeCategoryKind.OTHER
        }
    }

    fun isLostAndFound(category: HomeCategory?): Boolean {
        return category != null && classify(category) == HomeCategoryKind.LOST_AND_FOUND
    }

    private fun normalize(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace("\\s*/\\s*".toRegex(), "/")
            .replace("\\s+".toRegex(), " ")
    }
}
