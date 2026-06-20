package pember.qq.petugasunramhub.ui.home

import android.Manifest
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import pember.qq.petugasunramhub.databinding.ActivityPanicBinding
import pember.qq.petugasunramhub.utils.location.GPSLocationProvider

class PanicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPanicBinding
    
    private val viewModel: PanicViewModel by viewModels {
        PanicViewModelFactory(GPSLocationProvider(applicationContext))
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.requestLocation()
        } else {
            showError("Izin lokasi ditolak. Panic request tidak dapat disiapkan.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnRetry.setOnClickListener { checkPermissionsAndRequestLocation() }

        observeViewModel()
        
        checkPermissionsAndRequestLocation()
    }

    private fun checkPermissionsAndRequestLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            viewModel.requestLocation()
        } else {
            showLoading("Meminta izin dan mengambil lokasi darurat...")
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is PanicUiState.Idle -> {
                    // Do nothing
                }
                is PanicUiState.Loading -> {
                    showLoading(state.message)
                }
                is PanicUiState.Success -> {
                    showPayload(state.payloadText)
                }
                is PanicUiState.Error -> {
                    showError(state.error.message)
                }
            }
        }
    }

    private fun showLoading(message: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.tvStatus.text = message
        binding.tvStatus.visibility = android.view.View.VISIBLE
        binding.tvPayload.visibility = android.view.View.GONE
        binding.btnRetry.visibility = android.view.View.GONE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = android.view.View.GONE
        binding.tvStatus.text = message
        binding.tvStatus.visibility = android.view.View.VISIBLE
        binding.tvPayload.visibility = android.view.View.GONE
        binding.btnRetry.visibility = android.view.View.VISIBLE
    }

    private fun showPayload(payloadText: String) {
        binding.progressBar.visibility = android.view.View.GONE
        binding.tvStatus.text = "Payload darurat siap dikirim."
        binding.tvStatus.visibility = android.view.View.VISIBLE
        binding.tvPayload.text = payloadText
        binding.tvPayload.visibility = android.view.View.VISIBLE
        binding.btnRetry.visibility = android.view.View.VISIBLE
    }
}
