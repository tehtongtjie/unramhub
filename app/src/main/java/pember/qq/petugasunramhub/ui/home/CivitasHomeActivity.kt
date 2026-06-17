package pember.qq.petugasunramhub.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import pember.qq.petugasunramhub.databinding.CivitasHomeBinding

class CivitasHomeActivity : AppCompatActivity() {

    private lateinit var binding: CivitasHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Menggunakan ViewBinding untuk menghubungkan ke layout civitas_home.xml
        binding = CivitasHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aksi Klik Tombol
        binding.btnCivitasPanic.setOnClickListener {
            Toast.makeText(this, "Panic Button Aktif! Mengirim koordinat darurat...", Toast.LENGTH_SHORT).show()
        }

        binding.btnCivitasLaporBanner.setOnClickListener {
            Toast.makeText(this, "Mengarahkan ke Formulir Pelaporan PPKS", Toast.LENGTH_SHORT).show()
            // Contoh navigasi ke Form Laporan
            val intent = Intent(this, FormLaporanAnonimActivity::class.java)
            startActivity(intent)
        }

        binding.btnCivitasSelengkapnyaProgress.setOnClickListener {
            Toast.makeText(this, "Membuka Riwayat Status Laporan Anda", Toast.LENGTH_SHORT).show()
        }

        // Pengaturan Layout RecyclerView Kategori (Horizontal)
        binding.rvCivitasCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Pengaturan Layout RecyclerView Barang Hilang (Horizontal)
        binding.rvCivitasLostItems.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        
        // Catatan: Anda perlu menyetel adapter untuk RecyclerView di atas agar data muncul.
        // Contoh: binding.rvCivitasCategories.adapter = CategoryAdapter(listKategori)
    }
}
