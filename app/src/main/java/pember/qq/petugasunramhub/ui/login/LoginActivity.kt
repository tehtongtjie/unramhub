package pember.qq.petugasunramhub.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import pember.qq.petugasunramhub.databinding.ActivityLoginBinding
import pember.qq.petugasunramhub.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val viewModel: LoginViewModel by viewModels()
    private val sessionManager by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (sessionManager.isLoggedIn()) {
            goToMain()
            return
        }

        setContentView(binding.root)

        setupActionListeners()
        setupInputFieldWatchers()
        observeLoginState()
    }

    private fun setupActionListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etNimNip.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(username, password)) {
                viewModel.login(username, password)
            }
        }
    }

    private fun setupInputFieldWatchers() {
        // Cukup akses langsung via binding
        binding.etNimNip.addTextChangedListener {
            binding.tilNimNip.error = null
        }
        binding.etPassword.addTextChangedListener {
            binding.tilPassword.error = null
        }
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(this) { state ->
            binding.progressBar.isVisible = state is LoginState.Loading
            binding.btnLogin.isEnabled = state !is LoginState.Loading

            // Perhatikan: Karena Anda sudah pakai TextInputLayout,
            // kita gunakan setError pada TIL, bukan set text pada TextView error lama
            binding.etNimNip.isEnabled = state !is LoginState.Loading
            binding.etPassword.isEnabled = state !is LoginState.Loading

            when (state) {
                is LoginState.Success -> {
                    sessionManager.saveUser(state.user)
                    goToMain()
                }
                is LoginState.Error -> {
                    // Tampilkan error langsung di bawah field password atau field yang relevan
                    binding.tilPassword.error = state.message
                }
                else -> { }
            }
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        // Reset error
        binding.tilNimNip.error = null
        binding.tilPassword.error = null

        var isValid = true

        if (username.isEmpty()) {
            binding.tilNimNip.error = "NIM/NIP tidak boleh kosong"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            isValid = false
        }

        return isValid
    }

    private fun goToMain() {
        // Ubah MainActivity menjadi CivitasHomeActivity
        val intent = Intent(this, pember.qq.petugasunramhub.ui.home.CivitasHomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}