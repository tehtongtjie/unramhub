package pember.qq.petugasunramhub.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import pember.qq.petugasunramhub.data.repository.ReportRepository
import pember.qq.petugasunramhub.databinding.ActivityListLaporanBinding
import pember.qq.petugasunramhub.utils.SessionManager

class ListLaporanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListLaporanBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var reportRepository: ReportRepository
    private lateinit var adapter: ReportListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set action bar visibility
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        reportRepository = ReportRepository()

        setupRecyclerView()
        setupListeners()
        loadReports()
    }

    private fun setupRecyclerView() {
        binding.rvListLaporan.layoutManager = LinearLayoutManager(this)
        adapter = ReportListAdapter(emptyList()) { report ->
            val intent = Intent(this, DetailLaporanActivity::class.java).apply {
                putExtra("REPORT_ID", report.id)
            }
            startActivity(intent)
        }
        binding.rvListLaporan.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadReports(isRefreshing = true)
        }
    }

    private fun loadReports(isRefreshing: Boolean = false) {
        if (!isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }

        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            binding.progressBar.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(this, "Sesi Anda telah berakhir. Silakan login kembali.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val result = reportRepository.getCivitasReports(userId)
            
            binding.progressBar.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false

            result.fold(
                onSuccess = { reports ->
                    if (reports.isEmpty()) {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        adapter.updateData(emptyList())
                    } else {
                        binding.layoutEmptyState.visibility = View.GONE
                        adapter.updateData(reports)
                    }
                },
                onFailure = { error ->
                    Toast.makeText(
                        this@ListLaporanActivity,
                        "Gagal mengambil laporan: ${error.localizedMessage ?: "Terjadi kesalahan"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }
}
