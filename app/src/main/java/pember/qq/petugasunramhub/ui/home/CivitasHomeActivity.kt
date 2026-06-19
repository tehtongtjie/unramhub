package pember.qq.petugasunramhub.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import pember.qq.petugasunramhub.R
import pember.qq.petugasunramhub.databinding.CivitasHomeBinding
import pember.qq.petugasunramhub.utils.SessionManager // Pastikan ini di-import

class CivitasHomeActivity : AppCompatActivity() {
    private lateinit var binding: CivitasHomeBinding
    private lateinit var sessionManager: SessionManager // Tambahkan properti sessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menggunakan ViewBinding untuk menghubungkan ke layout civitas_home.xml
        binding = CivitasHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi SessionManager
        sessionManager = SessionManager(this)

        // Panggil fungsi untuk menampilkan data user
        displayUserProfile()

        setupListeners()
        setupCategoriesRecyclerView()
        setupLostItemsRecyclerView()
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
            Toast.makeText(this, "🚨 Panic Button Aktif! Mengirim koordinat darurat...", Toast.LENGTH_LONG).show()
        }

        // Banner Pelaporan Utama
        binding.btnCivitasLaporBanner.setOnClickListener {
            Toast.makeText(this, "Mengarahkan ke Formulir Pelaporan PPKS", Toast.LENGTH_SHORT).show()
        }

        // Laporan Sedang Ditinjau (Progress)
        binding.btnCivitasSelengkapnyaProgress.setOnClickListener {
            Toast.makeText(this, "Membuka detail pelaporan [Kerusakan Fasilitas] Anda", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCategoriesRecyclerView() {
        // Data Dummy Kategori sesuai rancangan gambar kamu
        val dummyCategories = listOf(
            CivitasCategory(1, "Kekerasan/\nPelecehan", android.R.drawable.ic_menu_agenda),
            CivitasCategory(2, "Kerusakan\nFasilitas", android.R.drawable.ic_menu_manage),
            CivitasCategory(3, "Bencana/\nDarurat", android.R.drawable.ic_dialog_alert),
            CivitasCategory(4, "Barang Hilang\n/Temuan", android.R.drawable.ic_menu_search),
            CivitasCategory(5, "Lainnya", android.R.drawable.ic_menu_more)
        )

        // Konfigurasi RecyclerView Kategori secara Horizontal
        binding.rvCivitasCategories.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.rvCivitasCategories.adapter = CivitasCategoryAdapter(dummyCategories) { category ->
            // Panggil fungsi pop-up dialog di sini
            showReportingTypeDialog(category)
        }
    }

    private fun showReportingTypeDialog(category: CivitasCategory) {
        val options = arrayOf("Anonim", "User Biasa")
        val cleanCategoryName = category.label.replace("\n", " ")

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Pilih Metode Pelaporan")
            .setMessage("Kategori: $cleanCategoryName")
            .setItems(options) { dialog, which ->
                val intent = Intent(this, pember.qq.petugasunramhub.ui.form.FormLaporanActivity::class.java).apply {
                    putExtra("CATEGORY_ID", category.id)
                    putExtra("CATEGORY_NAME", cleanCategoryName)
                    putExtra("IS_ANONYMOUS", which == 0)
                }
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupLostItemsRecyclerView() {
        // Data Dummy Barang Hilang
        val dummyLostItems = listOf(
            CivitasLostItem(
                id = 1,
                title = "Telah Hilang Kunci Motor Honda Vario",
                date = "21/04/2026",
                timeAgo = "Dilaporkan 2 jam lalu",
                imageResId = R.mipmap.ic_launcher
            ),
            CivitasLostItem(
                id = 2,
                title = "Telah Hilang Kunci Motor Honda Vario",
                date = "21/04/2026",
                timeAgo = "Dilaporkan 2 jam lalu",
                imageResId = R.mipmap.ic_launcher
            ),
            CivitasLostItem(
                id = 3,
                title = "Ditemukan Dompet Hitam di Parkiran",
                date = "21/04/2026",
                timeAgo = "Dilaporkan 5 jam lalu",
                imageResId = R.mipmap.ic_launcher
            )
        )

        // Konfigurasi RecyclerView Barang Hilang secara Horizontal
        binding.rvCivitasLostItems.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.rvCivitasLostItems.adapter = CivitasLostItemAdapter(dummyLostItems) { item ->
            Toast.makeText(this, "Melihat detail barang: ${item.title}", Toast.LENGTH_SHORT).show()
        }
    }
}