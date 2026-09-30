package cl.inacap.smartwatch

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import cl.inacap.smartwatch.databinding.FragmentAlarmaBinding

/** Alarma: pide POST_NOTIFICATIONS solo al programar; suena aunque esté denegado (en pantalla). */
class AlarmaFragment : Fragment() {

    private var _b: FragmentAlarmaBinding? = null
    private val b get() = _b!!
    private val h = Handler(Looper.getMainLooper())
    private var countdown: Runnable? = null
    private val delays = listOf(10, 60, 300, 600)

    private val permNotif =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            schedule() // se programa igual; el receiver avisa en pantalla si no hay permiso
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentAlarmaBinding.inflate(inflater, container, false)
        b.spDelay.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("en 10 seg (demo)", "en 1 min", "en 5 min", "en 10 min")
        )
        b.btnProgramar.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else schedule()
        }
        b.btnCancelar.setOnClickListener { cancel() }
        return b.root
    }

    private fun pending(): PendingIntent {
        val i = Intent(requireContext(), AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            requireContext(), 0, i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun schedule() {
        val secs = delays.getOrElse(b.spDelay.selectedItemPosition) { 60 }
        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + secs * 1000L, pending()
        )
        val end = System.currentTimeMillis() + secs * 1000L
        countdown?.let { h.removeCallbacks(it) }
        countdown = object : Runnable {
            override fun run() {
                val left = ((end - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                _b?.tvState?.text = "Alarma en $left s… (sonará como notificación)"
                if (left > 0) h.postDelayed(this, 1000)
            }
        }
        h.post(countdown!!)
    }

    private fun cancel() {
        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending())
        countdown?.let { h.removeCallbacks(it) }
        b.tvState.text = "Sin alarma programada."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countdown?.let { h.removeCallbacks(it) }
        _b = null
    }
}
