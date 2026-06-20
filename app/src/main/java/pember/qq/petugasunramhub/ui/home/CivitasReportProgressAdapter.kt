package pember.qq.petugasunramhub.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pember.qq.petugasunramhub.R

data class CivitasReportProgress(
    val id: Int,
    val title: String,
    val progress: Int,
    val status: String
)

class CivitasReportProgressAdapter(
    private val reports: List<CivitasReportProgress>,
    private val onItemClick: (CivitasReportProgress) -> Unit
) : RecyclerView.Adapter<CivitasReportProgressAdapter.ReportProgressViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportProgressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.civitas_item_report_progress, parent, false)
        return ReportProgressViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportProgressViewHolder, position: Int) {
        holder.bind(reports[position], onItemClick)
    }

    override fun getItemCount(): Int = reports.size

    class ReportProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvReportTitle)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressReport)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvReportStatus)
        private val btnSelengkapnya: Button = itemView.findViewById(R.id.btnSelengkapnya)

        fun bind(report: CivitasReportProgress, onItemClick: (CivitasReportProgress) -> Unit) {
            tvTitle.text = report.title
            progressBar.progress = report.progress
            tvStatus.text = report.status

            btnSelengkapnya.setOnClickListener {
                onItemClick(report)
            }
        }
    }
}
