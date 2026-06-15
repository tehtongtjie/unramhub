package pember.qq.petugasunramhub.ui.profil

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import pember.qq.petugasunramhub.databinding.FragmentProfilBinding
import pember.qq.petugasunramhub.ui.login.LoginActivity
import pember.qq.petugasunramhub.utils.SessionManager

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

    private val sessionManager by lazy { SessionManager(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProfileData()
        setupActionListeners()
    }

    private fun setupProfileData() {
        val namaUser = sessionManager.getName()
        binding.tvProfilNama.text = if (!namaUser.isNullOrEmpty()) namaUser else "Petugas Unram"

        val roleUser = sessionManager.getRole()
        binding.tvProfilRole.text = if (!roleUser.isNullOrEmpty()) "PETUGAS TIM ${roleUser.uppercase()}" else "PETUGAS"
    }

    private fun setupActionListeners() {
        // Listener Tombol Keluar Aplikasi
        binding.btnProfilLogout.setOnClickListener {
            sessionManager.logout()
            navigateToLogin()
        }

        // Listener Baris "Tentang UnramHUB" (Langsung panggil ID dari XML baru)
        binding.itemMenuTentang.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "UnramHUB v1.0\nPlatform Pelaporan Komunitas Universitas Mataram",
                Toast.LENGTH_LONG
            ).show()
        }

        // Listener Baris "Hubungi Admin" (Langsung panggil ID dari XML baru)
        binding.itemMenuHubungi.setOnClickListener {
            hubungiAdminUniversitas()
        }
    }

    private fun hubungiAdminUniversitas() {
        try {
            val nomorAdmin = "081234567890" // Sesuaikan nomor admin Unram nanti di sini
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$nomorAdmin")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Tidak dapat membuka aplikasi telepon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}