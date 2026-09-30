package cl.inacap.smartwatch

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cl.inacap.smartwatch.databinding.FragmentIotBinding
import kotlin.random.Random

/** Monitor ESP32 simulado. En C09 se conecta Firestore en vivo (ver comentario). */
class IotFragment : Fragment() {

    private var _b: FragmentIotBinding? = null
    private val b get() = _b!!
    private val h = Handler(Looper.getMainLooper())
    private var t = 22.5
    private var hh = 55.0
    private var led = false

    private val loop = object : Runnable {
        override fun run() {
            t = (t + Random.nextDouble(-0.3, 0.3)).coerceIn(15.0, 35.0)
            hh = (hh + Random.nextDouble(-1.0, 1.0)).coerceIn(20.0, 90.0)
            _b?.tvTemp?.text = "🌡️ %.1f °C".format(t)
            _b?.tvHum?.text = "💧 %.0f %%".format(hh)
            h.postDelayed(this, 2000)

            // C09 — telemetría real desde Firestore (requiere google-services.json):
            // FirebaseFirestore.getInstance().collection("telemetria")
            //     .orderBy("ts", Query.Direction.DESCENDING).limit(1)
            //     .addSnapshotListener { snap, _ ->
            //         val d = snap?.documents?.firstOrNull() ?: return@addSnapshotListener
            //         tvTemp.text = "🌡️ ${d.getDouble("temp")} °C"
            //         tvHum.text = "💧 ${d.getDouble("hum")} %"
            //     }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentIotBinding.inflate(inflater, container, false)
        b.btnLed.setOnClickListener {
            led = !led
            b.btnLed.text = "💡 LED: ${if (led) "ON" else "OFF"}"
        }
        return b.root
    }

    override fun onResume() {
        super.onResume()
        h.post(loop)
    }

    override fun onPause() {
        super.onPause()
        h.removeCallbacks(loop)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        h.removeCallbacks(loop)
        _b = null
    }
}
