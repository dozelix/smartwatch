package cl.inacap.smartwatch

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cl.inacap.smartwatch.databinding.FragmentPasosBinding
import kotlin.math.roundToInt
import kotlin.random.Random

/** Podómetro: sensor real si existe, si no simulación (emulador). Meta 10.000. */
class PasosFragment : Fragment(), SensorEventListener {

    private var _b: FragmentPasosBinding? = null
    private val b get() = _b!!
    private val h = Handler(Looper.getMainLooper())

    private var sm: SensorManager? = null
    private var baseline: Float? = null
    private var sensorSteps = 0
    private var simSteps = 0
    private var walking = false
    private val walk = object : Runnable {
        override fun run() {
            simSteps += Random.nextInt(5, 25)
            paint()
            if (walking) h.postDelayed(this, 800)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentPasosBinding.inflate(inflater, container, false)
        sm = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val hasSensor = sm?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
        b.tvMode.text = if (hasSensor) "Sensor de pasos del dispositivo" else "Modo simulado (sin sensor)"

        b.btnWalk.setOnClickListener {
            walking = !walking
            b.btnWalk.text = if (walking) "⏸ Pausar" else "▶ Simular caminata"
            h.removeCallbacks(walk)
            if (walking) h.post(walk)
        }
        b.btnStep.setOnClickListener { simSteps += 100; paint() }
        b.btnReset.setOnClickListener { simSteps = 0; sensorSteps = 0; baseline = null; paint() }
        paint()
        return b.root
    }

    override fun onResume() {
        super.onResume()
        sm?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?.let {
            sm?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sm?.unregisterListener(this)
        h.removeCallbacks(walk)
    }

    override fun onSensorChanged(e: SensorEvent) {
        if (baseline == null) baseline = e.values[0]
        sensorSteps = (e.values[0] - (baseline ?: e.values[0])).roundToInt().coerceAtLeast(0)
        paint()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun paint() {
        val total = sensorSteps + simSteps
        _b?.tvSteps?.text = "$total"
        _b?.pbSteps?.progress = total.coerceAtMost(10000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        h.removeCallbacks(walk)
        _b = null
    }
}
