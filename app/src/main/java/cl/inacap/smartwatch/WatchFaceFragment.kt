package cl.inacap.smartwatch

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cl.inacap.smartwatch.databinding.FragmentWatchBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Esfera base: hora viva + fecha + avatar de sesión. */
class WatchFaceFragment : Fragment() {

    private var _b: FragmentWatchBinding? = null
    private val b get() = _b!!
    private val h = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            val n = Date()
            _b?.tvTime?.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(n)
            _b?.tvDate?.text = SimpleDateFormat("EEE d MMM", Locale("es", "CL"))
                .format(n).uppercase()
            h.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentWatchBinding.inflate(inflater, container, false)
        b.btnAvatar.setOnClickListener {
            (activity as? MainActivity)?.openAuth()
        }
        return b.root
    }

    override fun onResume() {
        super.onResume()
        h.post(tick)
        b.btnAvatar.text = AuthStore.initials()
        b.tvUser.text = AuthStore.email?.let {
            "Sesión: $it (${if (AuthStore.cloud) "Firebase" else "demo local"})"
        } ?: "Sin sesión"
    }

    override fun onPause() {
        super.onPause()
        h.removeCallbacks(tick)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        h.removeCallbacks(tick)
        _b = null
    }
}
