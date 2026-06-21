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
import pember.qq.petugasunramhub.R
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
                    
                    // Load Attached Evidence Images if available (up to 3)
                    val imageUrls = report.evidenceImageUrls
                    if (imageUrls.isNotEmpty()) {
                        binding.layoutEvidence.visibility = View.VISIBLE
                        
                        // Load image 1
                        binding.cardEvidence1.visibility = View.VISIBLE
                        loadImageFromUrl(imageUrls[0], binding.imgDetailEvidence, binding.cardEvidence1)
                        binding.cardEvidence1.setOnClickListener {
                            showFullScreenImage(imageUrls[0])
                        }
                        
                        // Load image 2
                        if (imageUrls.size > 1) {
                            binding.cardEvidence2.visibility = View.VISIBLE
                            loadImageFromUrl(imageUrls[1], binding.imgDetailEvidence2, binding.cardEvidence2)
                            binding.cardEvidence2.setOnClickListener {
                                showFullScreenImage(imageUrls[1])
                            }
                        } else {
                            binding.cardEvidence2.visibility = View.GONE
                            binding.cardEvidence2.setOnClickListener(null)
                        }
                        
                        // Load image 3
                        if (imageUrls.size > 2) {
                            binding.cardEvidence3.visibility = View.VISIBLE
                            loadImageFromUrl(imageUrls[2], binding.imgDetailEvidence3, binding.cardEvidence3)
                            binding.cardEvidence3.setOnClickListener {
                                showFullScreenImage(imageUrls[2])
                            }
                        } else {
                            binding.cardEvidence3.visibility = View.GONE
                            binding.cardEvidence3.setOnClickListener(null)
                        }
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

    private fun loadImageFromUrl(imageUrl: String, imageView: android.widget.ImageView, cardView: View) {
        android.util.Log.d("DetailLaporan", "Loading image from URL: $imageUrl")
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    URL(imageUrl).openStream().use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DetailLaporan", "Gagal memuat gambar dari URL ($imageUrl): ${e.message}", e)
                    null
                }
            }
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                android.util.Log.w("DetailLaporan", "Bitmap null untuk URL: $imageUrl. Sembunyikan view.")
                cardView.visibility = View.GONE
            }
        }
    }

    private fun showFullScreenImage(imageUrl: String) {
        val dialog = android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_full_screen_image)

        val imgFullScreen = dialog.findViewById<android.widget.ImageView>(R.id.imgFullScreen)
        val btnClose = dialog.findViewById<android.widget.ImageButton>(R.id.btnCloseFullScreen)
        val progressBar = dialog.findViewById<android.widget.ProgressBar>(R.id.progressLoading)

        progressBar.visibility = View.VISIBLE

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    URL(imageUrl).openStream().use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DetailLaporan", "Gagal memuat gambar ukuran asli dari URL ($imageUrl): ${e.message}", e)
                    null
                }
            }
            progressBar.visibility = View.GONE
            if (bitmap != null) {
                imgFullScreen.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this@DetailLaporanActivity, "Gagal memuat gambar ukuran asli", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
