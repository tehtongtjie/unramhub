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
        }

        setContentView(R.layout.activity_main)

//        loadFragment(BerandaFragment())

        findViewById<BottomNavigationView>(R.id.bottomNav).setOnItemSelectedListener { item ->
            when (item.itemId) {
//                R.id.nav_beranda -> loadFragment(BerandaFragment())
//                R.id.nav_tugas   -> loadFragment(TugasFragment())
//                R.id.nav_profil  -> loadFragment(ProfilFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}