package pember.qq.petugasunramhub.ui.tugas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pember.qq.petugasunramhub.databinding.PetugasFragmentTugasBinding
import pember.qq.petugasunramhub.databinding.PetugasItemReportBinding
import pember.qq.petugasunramhub.data.network.RetrofitClient
import pember.qq.petugasunramhub.data.model.Report
import pember.qq.petugasunramhub.utils.SessionManager

class TugasFragment : Fragment() {

    private var _binding: PetugasFragmentTugasBinding? = null
    private val binding get() = _binding!!

    private val sessionManager by lazy { SessionManager(requireContext()) }

    // Variabel lokal untuk menampung seluruh data tugas yang ditarik dari Supabase
    private var allReports: List<Report> = emptyList()

    // Menyimpan status filter yang sedang aktif (default: semua)
    private var currentFilter: String = "semua"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PetugasFragmentTugasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwipeRefresh()
        setupFilterChips()
        fetchTasksFromSupabase()
    }

    private fun setupSwipeRefresh() {
        // Mengatur warna indikator berputar agar senada dengan identitas Unram HUB
        binding.swipeRefresh.setColorSchemeColors(
            resources.getColor(android.R.color.holo_blue_dark, requireContext().theme)
        )

        // Listener saat petugas menarik layar ke bawah untuk memperbarui data
        binding.swipeRefresh.setOnRefreshListener {
            fetchTasksFromSupabase()
        }
    }

    private fun setupFilterChips() {
        // Listener saat petugas berpindah pilihan tab filter status
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilter = when (checkedIds.firstOrNull()) {
                binding.chipAssigned.id -> "assigned"
                binding.chipProcessing.id -> "processing"
                binding.chipCompleted.id -> "completed"
                else -> "semua"
            }
            // Tampilkan kembali daftar tugas yang sesuai dengan kriteria filter tanpa hit API lagi
            displayFilteredTasks()
        }
    }

    private fun fetchTasksFromSupabase() {
        val userId = sessionManager.getUserId()
        if (userId <= 0L) {
            showEmptyState(true)
            binding.swipeRefresh.isRefreshing = false
            return
        }

        // Tampilkan progress bar utama jika swipeRefresh tidak sedang berjalan
        if (!binding.swipeRefresh.isRefreshing) {
            setLoadingState(true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Tarik data laporan berdasarkan ID petugas yang sedang login
                val reports = RetrofitClient.instance.getMyReports("eq.$userId")
                allReports = reports // Simpan ke list lokal

                displayFilteredTasks()
            } catch (e: Exception) {
                allReports = emptyList()
                showEmptyState(true)
            } finally {
                setLoadingState(false)
                binding.swipeRefresh.isRefreshing = false // Matikan animasi loading putar
            }
        }
    }

    private fun displayFilteredTasks() {
        // Kosongkan kontainer list tugas terlebih dahulu sebelum merender ulang
        binding.reportContainer.removeAllViews()

        // FILTER ELEMINASI: Singkirkan semua tugas yang statusnya sudah 'pending' (artinya telah di-unassigned oleh admin)
        val activeReports = allReports.filter { !it.status.equals("pending", ignoreCase = true) }

        // Saring list data lokal berdasarkan filter status pilihan tab
        val filteredList = if (currentFilter == "semua") {
            activeReports
        } else {
            activeReports.filter { it.status.equals(currentFilter, ignoreCase = true) }
        }

        // Jika hasil filter kosong, tampilkan ilustrasi empty state
        if (filteredList.isEmpty()) {
            showEmptyState(true)
            return
        }

        showEmptyState(false)
        val layoutInflater = LayoutInflater.from(requireContext())

        // Render data tugas ke dalam view kontainer secara dinamis
        filteredList.forEach { report ->
            val itemBinding = PetugasItemReportBinding.inflate(
                layoutInflater,
                binding.reportContainer,
                false
            )

            itemBinding.tvTitle.text = report.title
            itemBinding.tvCategory.text = report.categories?.name ?: "-"
            itemBinding.tvStatus.text = report.status.uppercase()

            // Alur klik item list menuju halaman Detail Laporan Tugas
            itemBinding.root.setOnClickListener {
                val intent = Intent(requireContext(), DetailLaporanActivity::class.java).apply {
                    putExtra("REPORT_ID", report.id)
                }
                startActivity(intent)
            }

            binding.reportContainer.addView(itemBinding.root)
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        if (isLoading) {
            binding.tvEmpty.isVisible = false
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.tvEmpty.isVisible = isEmpty
        // Mengontrol visibilitas SwipeRefreshLayout pembungkus list agar struktur scroll tetap aman
        binding.swipeRefresh.isVisible = !isEmpty
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}