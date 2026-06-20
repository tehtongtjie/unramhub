package pember.qq.petugasunramhub.data.model

data class HomeCategory(
    val id: Int,
    val name: String
)

enum class HomeCategoryKind {
    VIOLENCE_AND_HARASSMENT,
    FACILITY_DAMAGE,
    EMERGENCY,
    LOST_AND_FOUND,
    OTHER
}
