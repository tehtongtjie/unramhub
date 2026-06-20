package pember.qq.petugasunramhub.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import pember.qq.petugasunramhub.databinding.ActivityLostItemsBinding

class LostItemsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLostItemsBinding
    private val viewModel: LostItemsViewModel by viewModels()
    private lateinit var adapter: CivitasLostItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.loadLostItems()
    }

    private fun setupRecyclerView() {
        binding.rvLostItems.layoutManager = LinearLayoutManager(this)
        adapter = CivitasLostItemAdapter(emptyList()) { item ->
            Toast.makeText(this, "Melihat detail barang: ${item.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvLostItems.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnRetry.setOnClickListener { viewModel.loadLostItems() }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is LostItemsUiState.Loading -> showLoading()
                is LostItemsUiState.Success -> {
                    adapter.updateData(state.items)
                    binding.rvLostItems.visibility = View.VISIBLE
                    binding.tvStatus.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                }
                is LostItemsUiState.Empty -> showStatus("Belum ada info kehilangan atau temuan.", showRetry = false)
                is LostItemsUiState.Error -> showStatus(state.error.message, showRetry = true)
            }
        }
    }

    private fun showLoading() {
        adapter.updateData(emptyList())
        binding.rvLostItems.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.visibility = View.VISIBLE
        binding.tvStatus.text = "Memuat daftar kehilangan dan temuan..."
        binding.btnRetry.visibility = View.GONE
    }

    private fun showStatus(message: String, showRetry: Boolean) {
        adapter.updateData(emptyList())
        binding.rvLostItems.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.tvStatus.visibility = View.VISIBLE
        binding.tvStatus.text = message
        binding.btnRetry.visibility = if (showRetry) View.VISIBLE else View.GONE
    }
}
