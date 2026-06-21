package pember.qq.petugasunramhub.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import pember.qq.petugasunramhub.data.model.Report
import pember.qq.petugasunramhub.databinding.CivitasHomeBinding
import pember.qq.petugasunramhub.utils.SessionManager
import pember.qq.petugasunramhub.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import android.graphics.BitmapFactory

class CivitasHomeActivity : AppCompatActivity() {
    private lateinit var binding: CivitasHomeBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var categoryAdapter: CivitasCategoryAdapter
    private lateinit var lostItemAdapter: CivitasLostItemAdapter
    private lateinit var progressAdapter: ReportListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CivitasHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        displayUserProfile()
        setupListeners()
        setupCategoriesRecyclerView()
        setupReportProgressRecyclerView()
        setupLostItemsRecyclerView()
        observeViewModel()
    }

    private fun displayUserProfile() {
        // Ambil data user yang sedang aktif dari SessionManager
        val currentUser = sessionManager.getUser()

        if (currentUser != null) {
            // Set nama dan NIM/NIP ke TextView sesuai id di civitas_home.xml
            binding.tvCivitasUserName.text = currentUser.name
            binding.tvCivitasUserNim.text = currentUser.nimNip
        } else {
            // Antisipasi jika data sesi kosong (langsung tendang balik ke Login)
            val intent = Intent(this, pember.qq.petugasunramhub.ui.login.LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupListeners() {
        // Aksi Klik Tombol Panic
        binding.btnCivitasPanic.setOnClickListener {
            startActivitySafely(Intent(this, PanicActivity::class.java))
        }

        // Banner Pelaporan Utama (Harus memilih kategori terlebih dahulu)
        binding.btnCivitasLaporBanner.setOnClickListener {
            val state = viewModel.categoryState.value
            if (state is CategoryUiState.Success) {
                showCategorySelectionDialog(state.categories)
            } else {
                Toast.makeText(this, "Daftar kategori belum siap, silakan coba lagi.", Toast.LENGTH_SHORT).show()
            }
        }

        // Lihat Semua Progress
        binding.tvCivitasLihatSemuaProgress.setOnClickListener {
            startActivitySafely(Intent(this, ListLaporanActivity::class.java))
        }

        // Lihat Semua Barang Hilang
        binding.tvCivitasLihatSemuaLostItems.setOnClickListener {
            startActivitySafely(Intent(this, LostItemsActivity::class.java))
        }

        // Aksi Klik Foto Profil untuk Edit Profil
        binding.imgCivitasProfile.setOnClickListener {
            startActivitySafely(Intent(this, EditProfileActivity::class.java))
        }
    }

    private fun setupCategoriesRecyclerView() {
        binding.rvCivitasCategories.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        categoryAdapter = CivitasCategoryAdapter(emptyList()) { category ->
            showReportingTypeDialog(category)
        }
        binding.rvCivitasCategories.adapter = categoryAdapter
    }

    private fun showCustomSelectionDialog(title: String, options: List<CivitasDialogOption>) {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)

        val dialogBinding = pember.qq.petugasunramhub.databinding.DialogCustomSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        // Make window background transparent to support CardView corner radius
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Force layout width to match parent with horizontal margins
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.tvDialogTitle.text = title
        dialogBinding.btnDialogClose.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.rvDialogOptions.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        dialogBinding.rvDialogOptions.adapter = CivitasDialogOptionAdapter(options) {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCategorySelectionDialog(categories: List<CivitasCategory>) {
        val options = categories.map { category ->
            CivitasDialogOption(
                label = category.label.replace("\n", " "),
                iconResId = category.iconResId,
                action = {
                    showReportingTypeDialog(category)
                }
            )
        }
        showCustomSelectionDialog("Pilih Kategori Laporan", options)
    }

    private fun showReportingTypeDialog(category: CivitasCategory) {
        val cleanCategoryName = category.label.replace("\n", " ")
        val options = listOf(
            CivitasDialogOption(
                label = "Anonim",
                iconResId = R.drawable.ic_rounded_anonymous,
                action = {
                    val intentKeForm = Intent(this, pember.qq.petugasunramhub.ui.form.FormLaporanActivity::class.java).apply {
                        putExtra("CATEGORY_ID", category.id)
                        putExtra("CATEGORY_NAME", cleanCategoryName)
                        putExtra("EXTRA_IS_ANONYMOUS", true)
                    }
                    startActivitySafely(intentKeForm)
                }
            ),
            CivitasDialogOption(
                label = "User biasa",
                iconResId = R.drawable.ic_rounded_person,
                action = {
                    val intentKeForm = Intent(this, pember.qq.petugasunramhub.ui.form.FormLaporanActivity::class.java).apply {
                        putExtra("CATEGORY_ID", category.id)
                        putExtra("CATEGORY_NAME", cleanCategoryName)
                        putExtra("EXTRA_IS_ANONYMOUS", false)
                    }
                    startActivitySafely(intentKeForm)
                }
            )
        )
        showCustomSelectionDialog("Pilih jenis pelapor disini", options)
    }

    private fun setupLostItemsRecyclerView() {
        binding.rvCivitasLostItems.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        lostItemAdapter = CivitasLostItemAdapter(emptyList()) { item ->
            Toast.makeText(this, "Melihat detail barang: ${item.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvCivitasLostItems.adapter = lostItemAdapter
    }

    private fun setupReportProgressRecyclerView() {
        // Konfigurasi RecyclerView Progress Laporan secara Vertikal
        binding.rvCivitasReportProgress.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        progressAdapter = ReportListAdapter(emptyList()) { report ->
            val intent = Intent(this, DetailLaporanActivity::class.java).apply {
                putExtra("REPORT_ID", report.id)
            }
            startActivitySafely(intent)
        }
        binding.rvCivitasReportProgress.adapter = progressAdapter
    }

    override fun onResume() {
        super.onResume()
        val userId = sessionManager.getUserId()
        if (userId != -1L) {
            viewModel.refresh(userId)
        }
    }

    private fun observeViewModel() {
        viewModel.categoryState.observe(this) { state ->
            when (state) {
                is CategoryUiState.Loading -> showCategoryStatus("Memuat kategori...")
                is CategoryUiState.Success -> {
                    categoryAdapter.updateData(state.categories)
                    binding.tvCategoryStatus.visibility = View.GONE
                    binding.rvCivitasCategories.visibility = View.VISIBLE
                }
                is CategoryUiState.Empty -> showCategoryStatus("Kategori belum tersedia.")
                is CategoryUiState.Error -> showCategoryStatus(state.error.message)
            }
        }

        viewModel.recentReportState.observe(this) { state ->
            when (state) {
                is RecentReportUiState.Loading -> showReportStatus("Memuat progress laporan...")
                is RecentReportUiState.Success -> showRecentReports(state.reports)
                is RecentReportUiState.Empty -> showReportStatus("Belum ada laporan untuk ditampilkan.")
                is RecentReportUiState.Error -> showReportStatus(state.error.message)
            }
        }

        viewModel.lostItemState.observe(this) { state ->
            when (state) {
                is LostItemUiState.Loading -> showLostItemStatus("Memuat info kehilangan dan temuan...")
                is LostItemUiState.Success -> {
                    lostItemAdapter.updateData(state.items)
                    binding.tvLostItemStatus.visibility = View.GONE
                    binding.rvCivitasLostItems.visibility = View.VISIBLE
                }
                is LostItemUiState.Empty -> showLostItemStatus("Belum ada info kehilangan atau temuan.")
                is LostItemUiState.Error -> showLostItemStatus(state.error.message)
            }
        }

        viewModel.profilePhotoState.observe(this) { state ->
            when (state) {
                is ProfilePhotoUiState.Success -> {
                    state.url?.let { url ->
                        loadProfileImage(url)
                    }
                }
                else -> { /* Silent loading / no action on load error */ }
            }
        }
    }

    private fun showCategoryStatus(message: String) {
        categoryAdapter.updateData(emptyList())
        binding.rvCivitasCategories.visibility = View.GONE
        binding.tvCategoryStatus.text = message
        binding.tvCategoryStatus.visibility = View.VISIBLE
    }

    private fun showReportStatus(message: String) {
        progressAdapter.updateData(emptyList())
        binding.rvCivitasReportProgress.visibility = View.GONE
        binding.tvReportStatus.text = message
        binding.tvReportStatus.visibility = View.VISIBLE
    }

    private fun showLostItemStatus(message: String) {
        lostItemAdapter.updateData(emptyList())
        binding.rvCivitasLostItems.visibility = View.GONE
        binding.tvLostItemStatus.text = message
        binding.tvLostItemStatus.visibility = View.VISIBLE
    }

    private fun showRecentReports(reports: List<Report>) {
        progressAdapter.updateData(reports)
        binding.tvReportStatus.visibility = View.GONE
        binding.rvCivitasReportProgress.visibility = View.VISIBLE
    }

    private fun startActivitySafely(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "Halaman tujuan tidak ditemukan.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuka halaman tujuan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileImage(url: String) {
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    URL(url).openStream().use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CivitasHomeActivity", "Gagal memuat foto profil dari URL: ${e.message}", e)
                    null
                }
            }
            if (bitmap != null) {
                binding.imgCivitasProfile.imageTintList = null // Clear placeholder tint to show actual profile photo colors
                binding.imgCivitasProfile.setImageBitmap(bitmap)
            }
        }
    }
}
