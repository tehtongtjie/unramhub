package pember.qq.petugasunramhub.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import pember.qq.petugasunramhub.R
import pember.qq.petugasunramhub.databinding.CivitasHomeBinding

class CivitasHomeActivity : AppCompatActivity() {
    private lateinit var binding: CivitasHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menggunakan ViewBinding untuk menghubungkan ke layout civitas_home.xml
        binding = CivitasHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupCategoriesRecyclerView()
        setupLostItemsRecyclerView()
    }

    private fun setupListeners() {
        // Aksi Klik Tombol Panic
        binding.btnCivitasPanic.setOnClickListener {
            Toast.makeText(this, "🚨 Panic Button Aktif! Mengirim koordinat darurat...", Toast.LENGTH_LONG).show()
        }

        // Banner Pelaporan Utama
        binding.btnCivitasLaporBanner.setOnClickListener {
            Toast.makeText(this, "Mengarahkan ke Formulir Pelaporan PPKS", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, FormLaporanAnonimActivity::class.java)
            startActivity(intent)
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
            Toast.makeText(this, "Membuat laporan: ${category.label.replace("\n", " ")}", Toast.LENGTH_SHORT).show()
            // DI SINI: Nanti bisa diarahkan ke halaman pembuatan laporan sesuai kategori yang dipilih
        }
    }

    private fun setupLostItemsRecyclerView() {
        // Data Dummy Barang Hilang sesuai gambar MainMenu.png (Kunci Motor Honda Vario)
        // Kita gunakan icon launcher sebagai gambar sementara agar tidak error saat dicoba pertama kali.
        // Silakan ganti R.mipmap.ic_launcher dengan drawable gambar kunci aslimu nanti.
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
            // DI SINI: Nanti bisa diarahkan ke halaman detail barang hilang/temuan
        }
    }
}