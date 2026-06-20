package pember.qq.petugasunramhub.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pember.qq.petugasunramhub.R
import pember.qq.petugasunramhub.data.model.Report

class ReportListAdapter(
    private var reports: List<Report>,
    private val onItemClick: (Report) -> Unit
) : RecyclerView.Adapter<ReportListAdapter.ReportViewHolder>() {

    fun updateData(newReports: List<Report>) {
        reports = newReports
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.civitas_item_report_progress, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position], onItemClick)
    }

    override fun getItemCount(): Int = reports.size

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvReportTitle)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressReport)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvReportStatus)
        private val btnSelengkapnya: Button = itemView.findViewById(R.id.btnSelengkapnya)

        fun bind(report: Report, onItemClick: (Report) -> Unit) {
            // Build a descriptive title containing the category name if available
            val categoryPart = report.categories?.name?.let { "[$it] " } ?: ""
            tvTitle.text = "Laporan ${categoryPart}${report.title}"
            
            val progressVal = getStatusProgress(report.status)
            progressBar.progress = progressVal
            
            // Set Progress Bar Tint Color based on Status
            val colorHex = getStatusColor(report.status)
            progressBar.progressTintList = ColorStateList.valueOf(Color.parseColor(colorHex))
            
            // Set Status Label and Text Color
            tvStatus.text = getStatusLabel(report.status)
            tvStatus.setTextColor(Color.parseColor(colorHex))

            btnSelengkapnya.setOnClickListener {
                android.util.Log.d("ReportListAdapter", "btnSelengkapnya clicked for report ID: ${report.id}")
                onItemClick(report)
            }
        }

        private fun getStatusProgress(status: String): Int {
            return when (status.lowercase()) {
                "pending" -> 25
                "assigned" -> 50
                "processing" -> 75
                "completed" -> 100
                else -> 10
            }
        }

        private fun getStatusLabel(status: String): String {
            return when (status.lowercase()) {
                "pending" -> "Dalam Antrean"
                "assigned" -> "Telah Diterima"
                "processing" -> "Sedang Diproses"
                "completed" -> "Selesai"
                else -> status.replaceFirstChar { it.uppercase() }
            }
        }

        private fun getStatusColor(status: String): String {
            return when (status.lowercase()) {
                "pending" -> "#FFC107" // Amber
                "assigned" -> "#0D6EFD" // Blue Accent
                "processing" -> "#17A2B8" // Cyan/Teal
                "completed" -> "#28A745" // Green
                else -> "#6C757D" // Muted Gray
            }
        }
    }
}
