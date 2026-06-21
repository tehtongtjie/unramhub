package pember.qq.petugasunramhub.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pember.qq.petugasunramhub.databinding.ActivityEditProfileBinding
import java.io.File
import java.net.URL

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.imgProfilePhoto.imageTintList = null // Clear placeholder tint to show actual image colors
            binding.imgProfilePhoto.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply a grey tint for the default placeholder person icon
        binding.imgProfilePhoto.imageTintList = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor("#A0AEC0")
        )

        setupListeners()
        observeViewModel()

        viewModel.loadInitialData()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            
            var imageFile: File? = null
            selectedImageUri?.let { uri ->
                try {
                    imageFile = getFileFromUri(this, uri)
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal memproses gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            viewModel.saveProfile(name, email, imageFile)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is EditProfileUiState.Loading -> {
                    setLoading(true)
                }
                is EditProfileUiState.Success -> {
                    setLoading(false)
                    binding.etName.setText(state.user.name)
                    binding.etNimNip.setText(state.user.nimNip)
                    binding.etEmail.setText(state.user.email)

                    state.profilePhotoUrl?.let { url ->
                        loadProfilePhoto(url)
                    }
                }
                is EditProfileUiState.Error -> {
                    setLoading(false)
                    Toast.makeText(this, state.error.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.profilePhotoUrl.observe(this) { url ->
            url?.let { loadProfilePhoto(it) }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSaveProfile.isEnabled = !isLoading
    }

    private fun loadProfilePhoto(url: String) {
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    URL(url).openStream().use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("EditProfileActivity", "Gagal memuat foto dari URL: ${e.message}")
                    null
                }
            }
            if (bitmap != null && selectedImageUri == null) {
                binding.imgProfilePhoto.imageTintList = null // Clear placeholder tint to show actual image colors
                binding.imgProfilePhoto.setImageBitmap(bitmap)
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File {
        val tempFile = File(context.cacheDir, "temp_profile_${System.currentTimeMillis()}.jpg")
        
        var rawSize = 0L
        try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                rawSize = afd.length
            }
        } catch (e: Exception) {
            android.util.Log.w("EditProfileActivity", "Gagal membaca descriptor ukuran gambar: ${e.message}")
        }
        
        val maxBytes = 2 * 1024 * 1024 // 2 MB
        
        if (rawSize in 1..maxBytes) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return tempFile
        }
        
        val bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: throw Exception("Gagal memecah data gambar")
        
        var quality = 90
        var streamSize = Long.MAX_VALUE
        while (streamSize > maxBytes && quality > 10) {
            val bos = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, bos)
            val bytes = bos.toByteArray()
            streamSize = bytes.size.toLong()
            if (streamSize <= maxBytes) {
                tempFile.writeBytes(bytes)
                break
            }
            quality -= 15
        }
        
        if (streamSize > maxBytes) {
            val bos = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 10, bos)
            tempFile.writeBytes(bos.toByteArray())
        }
        
        bitmap.recycle()
        return tempFile
    }
}
