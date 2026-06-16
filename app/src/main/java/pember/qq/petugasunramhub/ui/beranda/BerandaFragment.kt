package pember.qq.petugasunramhub.ui.beranda

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pember.qq.petugasunramhub.databinding.PetugasFragmentBerandaBinding
import pember.qq.petugasunramhub.data.network.RetrofitClient
import pember.qq.petugasunramhub.utils.SessionManager
import java.util.Calendar

class BerandaFragment : Fragment() {

    // UBAH TIPE DATA BINDING MENJADI PetugasFragmentBerandaBinding
    private var _binding: PetugasFragmentBerandaBinding? = null
    private val binding get() = _binding!!

    private val sessionManager by lazy { SessionManager(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // INFLATE MENGGUNAKAN CLASS YANG BARU
        _binding = PetugasFragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()
        setupSwipeRefresh()
        fetchReportStatistics()
    }

    private fun setupHeader() {
        val salamWaktu = getGreetingBasedOnTime()
        val userName = sessionManager.getName().orEmpty().ifBlank { "Petugas" }
        binding.tvNama.text = "$salamWaktu, $userName 👋"
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            resources.getColor(android.R.color.holo_blue_dark, requireContext().theme)
        )
        binding.swipeRefresh.setOnRefreshListener {
            fetchReportStatistics()
        }
    }

    private fun fetchReportStatistics() {
        val userId = sessionManager.getUserId()

        if (userId <= 0L) {
            showErrorState("User ID tidak valid, silakan login kembali")
            binding.swipeRefresh.isRefreshing = false
            return
        }

        if (!binding.swipeRefresh.isRefreshing) {
            setLoadingState(true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val reports = RetrofitClient.instance.getMyReports("eq.$userId")

                val assignedCount = reports.count { it.status.equals("assigned", ignoreCase = true) }
                val processingCount = reports.count { it.status.equals("processing", ignoreCase = true) }
                val completedCount = reports.count { it.status.equals("completed", ignoreCase = true) }

                binding.tvAssigned.text = assignedCount.toString()
                binding.tvProcessing.text = processingCount.toString()
                binding.tvCompleted.text = completedCount.toString()

            } catch (e: Exception) {
                Log.e("BerandaFragment", "Gagal mengambil statistik: ${e.message}", e)
                showErrorState("Gagal memperbarui data dari server")
            } finally {
                setLoadingState(false)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.tvAssigned.isVisible = !isLoading
        binding.tvProcessing.isVisible = !isLoading
        binding.tvCompleted.isVisible = !isLoading
    }

    private fun showErrorState(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        binding.tvAssigned.text = "-"
        binding.tvProcessing.text = "-"
        binding.tvCompleted.text = "-"
    }

    private fun getGreetingBasedOnTime(): String {
        val jamSaatIni = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (jamSaatIni) {
            in 4..11 -> "Selamat Pagi"
            in 12..14 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}