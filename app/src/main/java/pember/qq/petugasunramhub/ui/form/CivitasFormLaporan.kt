package pember.qq.petugasunramhub.ui.form

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import pember.qq.petugasunramhub.databinding.CivitasFormLaporanBinding
import pember.qq.petugasunramhub.utils.SessionManager
import java.io.File
import java.util.Calendar
import java.util.Locale

class FormLaporanActivity : AppCompatActivity() {

    private lateinit var binding: CivitasFormLaporanBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel: FormLaporanViewModel by viewModels()
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
        setupListeners()
        observeViewModel()

        // Fetch category form configuration asynchronously
        viewModel.loadConfiguration(categoryId)
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

    private fun applyConfiguration(config: CategoryFormConfig) {
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

        // Apply dynamic visibility based on configuration
        binding.spinJenisPelapor.visibility = if (config.showReporterType) View.VISIBLE else View.GONE
        binding.layoutWaktuTanggal.visibility = if (config.showDateTime) View.VISIBLE else View.GONE
        binding.etLokasi.visibility = if (config.showLocation) View.VISIBLE else View.GONE
        binding.tvUploadBukti.visibility = if (config.showEvidence) View.VISIBLE else View.GONE
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
            triggerReportSubmission()
        }
    }

    private fun triggerReportSubmission() {
        val input = FormInput(
            description = binding.etDeskripsi.text.toString().trim(),
            location = binding.etLokasi.text.toString().trim(),
            reporterName = binding.etNamaPelapor.text.toString().trim(),
            reporterNim = binding.etNimPelapor.text.toString().trim(),
            contact = binding.etKontak.text.toString().trim(),
            reporterTypePosition = binding.spinJenisPelapor.selectedItemPosition,
            reporterTypeSelectedValue = binding.spinJenisPelapor.selectedItem?.toString() ?: "",
            eventTime = binding.etWaktu.text.toString().trim(),
            eventDate = binding.etTanggal.text.toString().trim()
        )

        var imageFile: File? = null
        val uri = selectedImageUri
        if (uri != null) {
            try {
                imageFile = getFileFromUri(this, uri)
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal memproses gambar bukti: ${e.message}", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Form Laporan"
        val userId = sessionManager.getUserId()

        viewModel.submitReport(
            input = input,
            categoryId = categoryId,
            categoryName = categoryName,
            isAnonymous = isAnonymous,
            imageFile = imageFile,
            userId = userId
        )
    }

    private fun observeViewModel() {
        // 1. Observe dynamic configurations
        viewModel.formConfig.observe(this) { config ->
            applyConfiguration(config)
        }

        // 2. Observe overall Screen UI Loading state
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is FormUiState.LoadingConfig -> {
                    binding.btnKirimLaporan.isEnabled = false
                    binding.btnKirimLaporan.text = "MEMUAT KONFIGURASI..."
                }
                is FormUiState.Ready -> {
                    binding.btnKirimLaporan.isEnabled = true
                    binding.btnKirimLaporan.text = "KIRIM LAPORAN"
                }
                is FormUiState.Error -> {
                    Toast.makeText(this, "Gagal memuat: ${state.error.message}", Toast.LENGTH_LONG).show()
                    binding.btnKirimLaporan.isEnabled = true
                    binding.btnKirimLaporan.text = "KIRIM LAPORAN"
                }
            }
        }

        // 3. Observe report submission outcomes
        viewModel.submissionState.observe(this) { state ->
            when (state) {
                is FormSubmissionState.Idle -> {
                    // Do nothing
                }
                is FormSubmissionState.Loading -> {
                    binding.btnKirimLaporan.isEnabled = false
                    binding.btnKirimLaporan.text = "MENGIRIM..."
                    Toast.makeText(this, "Sedang mengirim laporan...", Toast.LENGTH_SHORT).show()
                }
                is FormSubmissionState.ValidationError -> {
                    Toast.makeText(this, state.error.message, Toast.LENGTH_LONG).show()
                    viewModel.resetSubmissionState()
                }
                is FormSubmissionState.UploadError -> {
                    Toast.makeText(this, state.error.message, Toast.LENGTH_LONG).show()
                    viewModel.resetSubmissionState()
                    finish()
                }
                is FormSubmissionState.NetworkError -> {
                    Toast.makeText(this, state.error.message, Toast.LENGTH_LONG).show()
                    binding.btnKirimLaporan.isEnabled = true
                    binding.btnKirimLaporan.text = "KIRIM LAPORAN"
                    viewModel.resetSubmissionState()
                }
                is FormSubmissionState.Success -> {
                    Toast.makeText(this, "Laporan berhasil dikirim!", Toast.LENGTH_LONG).show()
                    viewModel.resetSubmissionState()
                    finish()
                }
            }
        }
    }

    private fun getFileFromUri(context: android.content.Context, uri: Uri): File {
        val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    }
}