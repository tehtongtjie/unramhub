package pember.qq.petugasunramhub.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pember.qq.petugasunramhub.R

// Model data sederhana untuk kategori
data class CivitasCategory(
    val id: Int,
    val label: String,
    val iconResId: Int // Menyimpan ID drawable untuk icon kategori
)

class CivitasCategoryAdapter(
    private val categories: List<CivitasCategory>,
    private val onItemClick: (CivitasCategory) -> Unit
) : RecyclerView.Adapter<CivitasCategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.civitas_item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], onItemClick)
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewIcon: View = itemView.findViewById(R.id.viewCivitasCategoryIcon)
        private val tvLabel: TextView = itemView.findViewById(R.id.tvCivitasCategoryLabel)

        fun bind(category: CivitasCategory, onItemClick: (CivitasCategory) -> Unit) {
            tvLabel.text = category.label

            // Set gambar background/icon jika ada (sementara menggunakan bawaan sistem jika res id default)
            if (category.iconResId != 0) {
                viewIcon.setBackgroundResource(category.iconResId)
            }

            itemView.setOnClickListener {
                onItemClick(category)
            }
        }
    }
}