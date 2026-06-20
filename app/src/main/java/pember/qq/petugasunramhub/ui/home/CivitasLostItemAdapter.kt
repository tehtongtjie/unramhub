package pember.qq.petugasunramhub.ui.home

import android.graphics.BitmapFactory
import java.net.URL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pember.qq.petugasunramhub.R

class CivitasLostItemAdapter(
    private var lostItems: List<CivitasLostItem>,
    private val onDetailClick: (CivitasLostItem) -> Unit
) : RecyclerView.Adapter<CivitasLostItemAdapter.LostItemViewHolder>() {

    fun updateData(newLostItems: List<CivitasLostItem>) {
        lostItems = newLostItems
        notifyDataSetChanged()
    }

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
            imgItem.setImageDrawable(null)
            imgItem.setBackgroundColor(android.graphics.Color.parseColor("#CCCCCC"))
            imgItem.tag = item.imageUrl

            val imageUrl = item.imageUrl
            if (!imageUrl.isNullOrBlank()) {
                Thread {
                    val bitmap = try {
                        URL(imageUrl).openStream().use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)
                        }
                    } catch (_: Exception) {
                        null
                    }

                    imgItem.post {
                        if (imgItem.tag == imageUrl && bitmap != null) {
                            imgItem.setImageBitmap(bitmap)
                            imgItem.setBackgroundResource(0)
                        }
                    }
                }.start()
            }

            btnSelengkapnya.setOnClickListener {
                onDetailClick(item)
            }
        }
    }
}
