package cl.inacap.smartwatch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cl.inacap.smartwatch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        AuthStore.restore(this)

        if (savedInstanceState == null) show(WatchFaceFragment())

        b.bottomNav.setOnItemSelectedListener { item ->
            val f: Fragment = when (item.itemId) {
                R.id.nav_watch -> WatchFaceFragment()
                R.id.nav_clima -> ClimaFragment()
                R.id.nav_alarma -> AlarmaFragment()
                R.id.nav_pasos -> PasosFragment()
                R.id.nav_iot -> IotFragment()
                else -> return@setOnItemSelectedListener false
            }
            show(f)
            true
        }
    }

    private fun show(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, f)
            .commit()
    }

    fun openAuth() = show(AuthFragment())
}
