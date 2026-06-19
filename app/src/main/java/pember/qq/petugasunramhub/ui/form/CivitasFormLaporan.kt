package pember.qq.petugasunramhub.ui.form

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pember.qq.petugasunramhub.databinding.CivitasFormLaporanBinding
import pember.qq.petugasunramhub.utils.SessionManager

class FormLaporanActivity : AppCompatActivity() {

    private lateinit var binding: CivitasFormLaporanBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CivitasFormLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupSpinners()
        autofillUserData()
        setupListeners()
    }

    private fun setupSpinners() {
        // Contoh data untuk dropdown (Spinner)
        val jenisPelapor = arrayOf("Silahkan Pilih Jenis Pelapor", "Mahasiswa", "Dosen", "Staff", "Lainnya")
        val jenisKejadian = arrayOf("Jenis Kejadian", "Pelecehan Seksual", "Kekerasan Fisik", "Perundungan (Bullying)", "Lainnya")
        val statusTerlapor = arrayOf("Status (mahasiswa/dosen/staff/orang luar)", "Mahasiswa", "Dosen", "Staff", "Orang Luar")

        binding.spinJenisPelapor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jenisPelapor)
        binding.spinJenisKejadian.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jenisKejadian)
        binding.spinStatusTerlapor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusTerlapor)
    }

    private fun autofillUserData() {
        // Otomatis mengisi Nama dan NIM jika user sudah login
        val currentUser = sessionManager.getUser()
        if (currentUser != null) {
            binding.etNamaPelapor.setText(currentUser.name)
            binding.etNimPelapor.setText(currentUser.nimNip)
        }
    }

    private fun setupListeners() {
        // Contoh aksi untuk waktu dan tanggal (Nantinya bisa diganti pakai TimePickerDialog/DatePickerDialog)
        binding.etWaktu.setOnClickListener {
            Toast.makeText(this, "Pilih Waktu", Toast.LENGTH_SHORT).show()
        }

        binding.etTanggal.setOnClickListener {
            Toast.makeText(this, "Pilih Tanggal", Toast.LENGTH_SHORT).show()
        }

        binding.btnKirimLaporan.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun validateAndSubmit() {
        // 1. Ambil nilai dari inputan
        val deskripsi = binding.etDeskripsi.text.toString().trim()
        val lokasi = binding.etLokasi.text.toString().trim()
        val namaPelapor = binding.etNamaPelapor.text.toString().trim()
        val nimPelapor = binding.etNimPelapor.text.toString().trim()
        val kontak = binding.etKontak.text.toString().trim()

        val posJenisPelapor = binding.spinJenisPelapor.selectedItemPosition
        val posJenisKejadian = binding.spinJenisKejadian.selectedItemPosition

        // 2. Cek apakah ada kolom wajib yang masih kosong atau dropdown belum dipilih (posisi 0)
        var isValid = true

        if (posJenisPelapor == 0) {
            Toast.makeText(this, "Pilih Jenis Pelapor!", Toast.LENGTH_SHORT).show()
            isValid = false
        } else if (posJenisKejadian == 0) {
            Toast.makeText(this, "Pilih Jenis Kejadian!", Toast.LENGTH_SHORT).show()
            isValid = false
        } else if (deskripsi.isEmpty()) {
            binding.etDeskripsi.error = "Deskripsi kejadian tidak boleh kosong"
            binding.etDeskripsi.requestFocus()
            isValid = false
        } else if (lokasi.isEmpty()) {
            binding.etLokasi.error = "Lokasi kejadian tidak boleh kosong"
            binding.etLokasi.requestFocus()
            isValid = false
        } else if (namaPelapor.isEmpty()) {
            binding.etNamaPelapor.error = "Nama pelapor tidak boleh kosong"
            binding.etNamaPelapor.requestFocus()
            isValid = false
        } else if (nimPelapor.isEmpty()) {
            binding.etNimPelapor.error = "NIM pelapor tidak boleh kosong"
            binding.etNimPelapor.requestFocus()
            isValid = false
        } else if (kontak.isEmpty()) {
            binding.etKontak.error = "Kontak tidak boleh kosong"
            binding.etKontak.requestFocus()
            isValid = false
        }

        // 3. Jika semua validasi lolos, jalankan proses pengiriman
        if (isValid) {
            Toast.makeText(this, "Laporan berhasil divalidasi dan siap dikirim!", Toast.LENGTH_LONG).show()
            // TODO: Eksekusi API atau logika simpan ke database di sini
        }
    }
}