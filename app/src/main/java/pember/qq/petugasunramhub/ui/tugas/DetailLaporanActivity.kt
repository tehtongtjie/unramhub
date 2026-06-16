package pember.qq.petugasunramhub.ui.tugas

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pember.qq.petugasunramhub.data.network.RetrofitClient
import pember.qq.petugasunramhub.data.repository.StorageRepository
import pember.qq.petugasunramhub.data.repository.TaskRepository
import pember.qq.petugasunramhub.databinding.PetugasActivityDetailLaporanBinding
import pember.qq.petugasunramhub.utils.SessionManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DetailLaporanActivity : AppCompatActivity() {

    private val binding by lazy { PetugasActivityDetailLaporanBinding.inflate(layoutInflater) }
    private val sessionManager by lazy { SessionManager(this) }
    private val taskRepository by lazy { TaskRepository() }
    private val storageRepository by lazy { StorageRepository() }

    private var fotoFile: File? = null
    private var currentReportId: Long = -1

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            fotoFile?.let { file ->
                lifecycleScope.launch {
                    val compressed = withContext(Dispatchers.IO) { compressImage(file) }
                    if (compressed != null) {
                        fotoFile = compressed
                        binding.ivPreviewFoto.setImageBitmap(BitmapFactory.decodeFile(compressed.absolutePath))
                        binding.ivPreviewFoto.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) bukaKamera() else Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        currentReportId = intent.getLongExtra("REPORT_ID", -1)
        if (currentReportId == -1L) {
            finish()
            return
        }

        setupActionBar()
        loadDetails(currentReportId)
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Laporan"
    }

    private fun loadDetails(id: Long) {
        lifecycleScope.launch {
            try {
                val list = RetrofitClient.instance.getReportDetail("eq.$id")
                val item = list.firstOrNull() ?: return@launch

                binding.tvDetailTitle.text = item.title
                binding.tvDetailKategori.text = item.categories?.name ?: "-"
                binding.tvDetailStatus.text = item.status.uppercase()
                binding.tvDetailDeskripsi.text = item.description
                binding.tvDetailPelapor.text = if (item.isAnonymous) "Anonim" else "${item.users?.name} (${item.users?.nimNip})"

                setupButtons(id, item.status)
            } catch (e: Exception) {
                Toast.makeText(this@DetailLaporanActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButtons(id: Long, status: String) {
        val (nextStatus, nextLabel) = when (status) {
            "assigned" -> "processing" to "Mulai Proses"
            "processing" -> "completed" to "Selesaikan"
            else -> null to null
        }

        if (nextStatus != null) {
            binding.tilCatatan.visibility = View.VISIBLE
            binding.btnUpdateStatus.visibility = View.VISIBLE
            binding.btnUpdateStatus.text = nextLabel

            if (status == "processing") {
                binding.btnAmbilFoto.visibility = View.VISIBLE
                binding.tilCatatan.hint = "Tambahkan Catatan Hasil Pekerjaan (Wajib)"
            }

            binding.btnAmbilFoto.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) bukaKamera()
                else permissionLauncher.launch(Manifest.permission.CAMERA)
            }

            binding.btnUpdateStatus.setOnClickListener { executeStatusUpdate(id, status, nextStatus) }
        }
    }

    private fun bukaKamera() {
        val file = File.createTempFile("bukti_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        fotoFile = file
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        cameraLauncher.launch(uri)
    }

    private fun compressImage(file: File): File? {
        return try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val compressed = File(file.parent, "compressed_${file.name}")
            var quality = 90
            var size: Long
            do {
                FileOutputStream(compressed).use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, it) }
                size = compressed.length()
                quality -= 10
            } while (size > 2 * 1024 * 1024 && quality > 10)
            compressed
        } catch (e: IOException) { null }
    }

    private fun executeStatusUpdate(id: Long, oldStatus: String, nextStatus: String) {
        val catatan = binding.etCatatan.text.toString().trim()
        if (nextStatus == "completed" && (catatan.isEmpty() || fotoFile == null)) {
            Toast.makeText(this, "Catatan dan Foto bukti wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnUpdateStatus.isEnabled = false
        lifecycleScope.launch {
            var fotoUrl: String? = null
            fotoFile?.let {
                val result = withContext(Dispatchers.IO) { storageRepository.uploadFoto(it, id) }
                fotoUrl = result.getOrNull()
            }

            val result = taskRepository.updateStatus(id, nextStatus, oldStatus, sessionManager.getUserId(), catatan, fotoUrl)
            if (result.isSuccess) {
                Toast.makeText(this@DetailLaporanActivity, "Update berhasil", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                binding.btnUpdateStatus.isEnabled = true
                Toast.makeText(this@DetailLaporanActivity, "Gagal update", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}