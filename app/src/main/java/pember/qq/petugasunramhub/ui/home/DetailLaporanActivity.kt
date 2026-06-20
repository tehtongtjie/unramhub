package pember.qq.petugasunramhub.ui.home

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pember.qq.petugasunramhub.databinding.ActivityDetailLaporanBinding
import java.net.URL

class DetailLaporanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailLaporanBinding
    private val viewModel: DetailLaporanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide default Action Bar
        supportActionBar?.hide()

        setupListeners()
        observeViewModel()

        val reportId = intent.getLongExtra("REPORT_ID", -1L)
        android.util.Log.d("DetailLaporanActivity", "onCreate started with reportId: $reportId")
        if (reportId == -1L) {
            Toast.makeText(this, "Laporan tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.loadReportDetail(reportId)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is DetailUiState.Loading -> {
                    // Silent loading to keep UI appearance exactly unchanged
                }
                is DetailUiState.Success -> {
                    val report = state.report
                    
                    // Set Status Value and Text Color
                    binding.tvStatusValue.text = report.statusLabel
                    binding.tvStatusValue.setTextColor(Color.parseColor(report.statusColorHex))
                    binding.cardStatus.setCardBackgroundColor(Color.parseColor(report.statusBgOpacityColorHex))
                    
                    // Set Title & Category
                    binding.tvDetailTitle.text = report.title
                    binding.tvDetailCategory.text = report.categoryName
                    
                    // Set Date
                    binding.tvDetailDate.text = report.dateText
                    
                    // Set Location
                    binding.tvDetailLocation.text = report.locationText
                    
                    // Set Reporter
                    if (report.isAnonymous) {
                        binding.layoutDetailReporter.visibility = View.GONE
                    } else {
                        binding.layoutDetailReporter.visibility = View.VISIBLE
                        binding.tvDetailReporter.text = report.reporterText
                    }
                    
                    // Set Description
                    binding.tvDetailDescription.text = report.descriptionText
                    
                    // Load Attached Evidence Image if available
                    val imageUrl = report.evidenceImageUrl
                    if (imageUrl != null) {
                        binding.layoutEvidence.visibility = View.VISIBLE
                        loadImageFromUrl(imageUrl)
                    } else {
                        binding.layoutEvidence.visibility = View.GONE
                    }
                }
                is DetailUiState.Error -> {
                    Toast.makeText(
                        this,
                        "Gagal mengambil detail: ${state.error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                is DetailUiState.Empty -> {
                    Toast.makeText(this, "Laporan tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun loadImageFromUrl(imageUrl: String) {
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val input = URL(imageUrl).openStream()
                    BitmapFactory.decodeStream(input)
                } catch (e: Exception) {
                    android.util.Log.e("DetailLaporan", "Gagal memuat gambar: ${e.message}")
                    null
                }
            }
            if (bitmap != null) {
                binding.imgDetailEvidence.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this@DetailLaporanActivity, "Gagal memuat bukti gambar pendukung", Toast.LENGTH_SHORT).show()
                binding.layoutEvidence.visibility = View.GONE
            }
        }
    }
}
