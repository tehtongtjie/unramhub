package pember.qq.petugasunramhub.ui.home

data class CivitasCategory(
    val id: Int,
    val label: String,
    val iconResId: Int
)

data class CivitasLostItem(
    val id: Long,
    val title: String,
    val date: String,
    val timeAgo: String,
    val imageUrl: String?
)
