package pember.qq.petugasunramhub.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pember.qq.petugasunramhub.databinding.ActivityDashboardBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Civitas_Home : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up ViewBinding
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // Klik tombol Lapor Sekarang di Banner
        binding.btnLaporSekarang.setOnClickListener {
            showReportTypeDialog("Umum")
        }

        // Klik Panic Button
        binding.btnPanic.setOnClickListener {
            Toast.makeText(this, "Panic Button Ditekan!", Toast.LENGTH_SHORT).show()
        }

        // 1. Kategori Kekerasan
        binding.menuKekerasan.setOnClickListener {
            showReportTypeDialog("Kekerasan/Pelecehan")
        }

        // 2. Kategori Fasilitas
        binding.menuFasilitas.setOnClickListener {
            showReportTypeDialog("Kerusakan Fasilitas")
        }

        // 3. Kategori Bencana
        binding.menuBencana.setOnClickListener {
            showReportTypeDialog("Bencana/Darurat")
        }

        // 4. Kategori Barang Hilang
        binding.menuBarang.setOnClickListener {
            showReportTypeDialog("Barang Hilang/Temuan")
        }

        // 5. Kategori Lainnya
        binding.menuLainnya.setOnClickListener {
            showReportTypeDialog("Lainnya")
        }

        // Klik Tombol Selengkapnya pada Tracker Progress
        binding.btnDetailProgress.setOnClickListener {
            Toast.makeText(this, "Membuka detail perkembangan laporan...", Toast.LENGTH_SHORT).show()
            // Nanti di sini bisa kamu arahkan (Intent) ke halaman detail tracking laporan
        }
    }

    // Fungsi Pop-Up Pilihan Mode Pelapor
    private fun showReportTypeDialog(kategori: String) {
        val options = arrayOf(
            "Lapor secara Terbuka (Menggunakan Identitas Asli)",
            "Lapor sebagai Anonim (Identitas Dirahasiakan)",
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Pilih Mode Pelaporan")
            .setMessage("Kamu akan melaporkan: $kategori")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // User pilih Lapor Terbuka
                        Toast.makeText(this, "Melapor Terbuka untuk: $kategori", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, FormLaporanAnonimActivity::class.java)
                        startActivity(intent)
                    }
                    1 -> {
                        // User pilih Lapor Anonim
                        Toast.makeText(this, "Melapor Anonim untuk: $kategori", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, FormLaporanAnonimActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
