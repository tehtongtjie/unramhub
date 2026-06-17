package pember.qq.petugasunramhub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
//import pember.qq.petugasunramhub.ui.beranda.BerandaFragment
import pember.qq.petugasunramhub.ui.login.LoginActivity
//import pember.qq.petugasunramhub.ui.profil.ProfilFragment
//import pember.qq.petugasunramhub.ui.tugas.TugasFragment
import pember.qq.petugasunramhub.utils.SessionManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager(this)
        if (!session.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        } else {
            // Jika sudah login, langsung arahkan ke CivitasHomeActivity
            startActivity(Intent(this, pember.qq.petugasunramhub.ui.home.CivitasHomeActivity::class.java))
            finish()
            return
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}