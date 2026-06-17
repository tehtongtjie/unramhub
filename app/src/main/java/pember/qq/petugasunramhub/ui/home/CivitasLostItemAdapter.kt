package pember.qq.petugasunramhub.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pember.qq.petugasunramhub.R

// Model data sederhana untuk laporan barang hilang
data class CivitasLostItem(
    val id: Int,
    val title: String,
    val date: String,
    val timeAgo: String,
    val imageResId: Int // Menyimpan ID drawable untuk gambar kunci/barang
)

class CivitasLostItemAdapter(
    private val lostItems: List<CivitasLostItem>,
    private val onDetailClick: (CivitasLostItem) -> Unit
) : RecyclerView.Adapter<CivitasLostItemAdapter.LostItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LostItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.civitas_item_lost_item, parent, false)
        return LostItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: LostItemViewHolder, position: Int) {
        holder.bind(lostItems[position], onDetailClick)
    }

    override fun getItemCount(): Int = lostItems.size

    class LostItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgItem: ImageView = itemView.findViewById(R.id.imgCivitasLostItem)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvCivitasLostItemTitle)
        private val tvTime: TextView = itemView.findViewById(R.id.tvCivitasLostItemTime)
        private val btnSelengkapnya: Button = itemView.findViewById(R.id.btnCivitasSelengkapnyaLost)

        fun bind(item: CivitasLostItem, onDetailClick: (CivitasLostItem) -> Unit) {
            tvTitle.text = item.title
            tvTime.text = "${item.date}\n${item.timeAgo}"

            // Set gambar barang hilang
            if (item.imageResId != 0) {
                imgItem.setImageResource(item.imageResId)
                imgItem.setBackgroundResource(0) // Menghapus background placeholder abu-abu
            }

            btnSelengkapnya.setOnClickListener {
                onDetailClick(item)
            }
        }
    }
}