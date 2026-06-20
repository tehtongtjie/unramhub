package pember.qq.petugasunramhub.ui.form

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import pember.qq.petugasunramhub.databinding.CivitasFormLaporanBinding
import pember.qq.petugasunramhub.utils.SessionManager
import java.util.Calendar
import java.util.Locale

class FormLaporanActivity : AppCompatActivity() {

    private lateinit var binding: CivitasFormLaporanBinding
    private lateinit var sessionManager: SessionManager
    private var categoryId: Int = -1
    private var isAnonymous: Boolean = false
    private var selectedImageUri: Uri? = null

    // Register modern ActivityResultLauncher to pick an image
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.tvUploadBukti.text = "Bukti: Terpilih (Tap untuk ganti)"
            Toast.makeText(this, "Bukti berhasil dipilih!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CivitasFormLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Read extras passed from home activity
        categoryId = intent.getIntExtra("CATEGORY_ID", -1)
        isAnonymous = intent.getBooleanExtra("EXTRA_IS_ANONYMOUS", false)
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Form Laporan"

        // Set Title
        title = categoryName
        supportActionBar?.title = categoryName

        setupSpinners()
        autofillUserData()
        applyCategoryVisibility()
        setupListeners()
    }

    private fun setupSpinners() {
        val jenisPelapor = arrayOf("Silahkan Pilih Jenis Pelapor", "Mahasiswa", "Dosen", "Staff", "Lainnya")
        binding.spinJenisPelapor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jenisPelapor)
        
        // Hide spinJenisKejadian as category is chosen from home screen
        binding.spinJenisKejadian.visibility = View.GONE
    }

    private fun autofillUserData() {
        val currentUser = sessionManager.getUser()
        if (currentUser != null && !isAnonymous) {
            binding.etNamaPelapor.setText(currentUser.name)
            binding.etNimPelapor.setText(currentUser.nimNip)
        }
    }

    private fun applyCategoryVisibility() {
        // Handle anonymity
        if (isAnonymous) {
            binding.tvLabelIdentitasPelapor.visibility = View.GONE
            binding.etNamaPelapor.visibility = View.GONE
            binding.etNimPelapor.visibility = View.GONE
        } else {
            binding.tvLabelIdentitasPelapor.visibility = View.VISIBLE
            binding.etNamaPelapor.visibility = View.VISIBLE
            binding.etNimPelapor.visibility = View.VISIBLE
        }

        // Handle category specific configurations
        // categoryId: 1 = Kekerasan/Pelecehan, 2 = Kerusakan Fasilitas, 3 = Bencana/Darurat, 4 = Barang Hilang/Temuan, 5 = Lainnya
        when (categoryId) {
            1 -> {
                binding.spinJenisPelapor.visibility = View.VISIBLE
                binding.layoutWaktuTanggal.visibility = View.VISIBLE
                binding.etLokasi.visibility = View.VISIBLE
                binding.tvUploadBukti.visibility = View.VISIBLE
            }
            2 -> {
                binding.spinJenisPelapor.visibility = View.GONE
                binding.layoutWaktuTanggal.visibility = View.GONE
                binding.etLokasi.visibility = View.VISIBLE
                binding.tvUploadBukti.visibility = View.VISIBLE
            }
            3 -> {
                binding.spinJenisPelapor.visibility = View.GONE
                binding.layoutWaktuTanggal.visibility = View.GONE
                binding.etLokasi.visibility = View.VISIBLE
                binding.tvUploadBukti.visibility = View.GONE
            }
            4 -> {
                binding.spinJenisPelapor.visibility = View.GONE
                binding.layoutWaktuTanggal.visibility = View.VISIBLE
                binding.etLokasi.visibility = View.VISIBLE
                binding.tvUploadBukti.visibility = View.VISIBLE
            }
            else -> {
                binding.spinJenisPelapor.visibility = View.GONE
                binding.layoutWaktuTanggal.visibility = View.VISIBLE
                binding.etLokasi.visibility = View.VISIBLE
                binding.tvUploadBukti.visibility = View.VISIBLE
            }
        }
    }

    private fun setupListeners() {
        // 1. Time Picker
        binding.etWaktu.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                binding.etWaktu.setText(formattedTime)
            }, hour, minute, true)

            timePickerDialog.show()
        }

        // 2. Date Picker
        binding.etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.etTanggal.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.show()
        }

        // 3. Location Picker (Mock GPS coordinates on Click)
        binding.etLokasi.setOnClickListener {
            // Simulasi deteksi GPS otomatis
            binding.etLokasi.setText("Gedung FT Unram (-8.5833, 116.0969)")
            Toast.makeText(this, "Lokasi terdeteksi otomatis via GPS", Toast.LENGTH_SHORT).show()
        }

        // 4. Evidence Picker
        binding.tvUploadBukti.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnKirimLaporan.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun validateAndSubmit() {
        val deskripsi = binding.etDeskripsi.text.toString().trim()
        val lokasi = binding.etLokasi.text.toString().trim()
        val namaPelapor = binding.etNamaPelapor.text.toString().trim()
        val nimPelapor = binding.etNimPelapor.text.toString().trim()
        val kontak = binding.etKontak.text.toString().trim()

        var isValid = true

        // Validate spinJenisPelapor (only if visible)
        if (binding.spinJenisPelapor.visibility == View.VISIBLE) {
            val posJenisPelapor = binding.spinJenisPelapor.selectedItemPosition
            if (posJenisPelapor == 0) {
                Toast.makeText(this, "Pilih Jenis Pelapor!", Toast.LENGTH_SHORT).show()
                isValid = false
                return
            }
        }

        // Validate Description (always required)
        if (deskripsi.isEmpty()) {
            binding.etDeskripsi.error = "Deskripsi kejadian tidak boleh kosong"
            binding.etDeskripsi.requestFocus()
            isValid = false
            return
        }

        // Validate Location (only if visible)
        if (binding.etLokasi.visibility == View.VISIBLE && lokasi.isEmpty()) {
            binding.etLokasi.error = "Lokasi kejadian tidak boleh kosong"
            binding.etLokasi.requestFocus()
            isValid = false
            return
        }

        // Validate Reporter Identity (only if visible / not anonymous)
        if (binding.etNamaPelapor.visibility == View.VISIBLE) {
            if (namaPelapor.isEmpty()) {
                binding.etNamaPelapor.error = "Nama pelapor tidak boleh kosong"
                binding.etNamaPelapor.requestFocus()
                isValid = false
                return
            }
            if (nimPelapor.isEmpty()) {
                binding.etNimPelapor.error = "NIM pelapor tidak boleh kosong"
                binding.etNimPelapor.requestFocus()
                isValid = false
                return
            }
        }

        // Validate Contact (always required)
        if (kontak.isEmpty()) {
            binding.etKontak.error = "Kontak tidak boleh kosong"
            binding.etKontak.requestFocus()
            isValid = false
            return
        }

        if (isValid) {
            Toast.makeText(this, "Laporan berhasil divalidasi dan siap dikirim!", Toast.LENGTH_LONG).show()
        }
    }
}