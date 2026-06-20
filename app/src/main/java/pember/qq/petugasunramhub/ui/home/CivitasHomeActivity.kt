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
    }

    private fun setupCategoriesRecyclerView() {
        binding.rvCivitasCategories.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        categoryAdapter = CivitasCategoryAdapter(emptyList()) { category ->
            showReportingTypeDialog(category)
        }
        binding.rvCivitasCategories.adapter = categoryAdapter
    }

    private fun showCategorySelectionDialog(categories: List<CivitasCategory>) {
        val categoryNames = categories.map { it.label.replace("\n", " ") }.toTypedArray()

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Pilih Kategori Laporan")
        builder.setItems(categoryNames) { dialog, which ->
            val selectedCategory = categories[which]
            showReportingTypeDialog(selectedCategory)
            dialog.dismiss()
        }
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun showReportingTypeDialog(category: CivitasCategory) {
        val options = arrayOf("Laporkan sebagai Anonim", "Laporkan sebagai User Biasa")
        val cleanCategoryName = category.label.replace("\n", " ")

        // Pakai AppCompat AlertDialog yang tahan banting
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)

        // Gabungin info kategori ke Title, JANGAN pakai .setMessage()
        builder.setTitle("Pilih Metode Pelaporan\n(Kategori: $cleanCategoryName)")

        // setItems dijamin muncul sekarang
        builder.setItems(options) { dialog, which ->
            // Siapkan intent menuju form laporan yang ada di folder ui.form
            val intentKeForm = Intent(this, pember.qq.petugasunramhub.ui.form.FormLaporanActivity::class.java).apply {
                putExtra("CATEGORY_ID", category.id)
                putExtra("CATEGORY_NAME", cleanCategoryName)

                // index 0 = Anonim (true), index 1 = User Biasa (false)
                putExtra("EXTRA_IS_ANONYMOUS", which == 0)
            }

            startActivitySafely(intentKeForm)
            dialog.dismiss()
        }

        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
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
}
