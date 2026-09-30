package cl.inacap.smartwatch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import cl.inacap.smartwatch.databinding.FragmentClimaBinding
import com.google.android.gms.location.LocationServices
import java.net.URL

/**
 * Clima: pide GPS con explicación previa; si lo niegan, ciudad manual.
 * Usa Open-Meteo (sin API key).
 */
class ClimaFragment : Fragment() {

    private var _b: FragmentClimaBinding? = null
    private val b get() = _b!!

    private val permGeo =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) locate() else out("❌ Permiso denegado. No pasa nada: escribe la ciudad abajo.")
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentClimaBinding.inflate(inflater, container, false)
        b.btnGeo.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> locate()
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ->
                    out("El sistema te pedirá ubicación: es solo para el clima local.")
                        .also { permGeo.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                else -> permGeo.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        b.btnCity.setOnClickListener {
            val q = b.etCity.text.toString().trim()
            if (q.isEmpty()) { out("Escribe una ciudad primero."); return@setOnClickListener }
            out("Buscando “$q”…")
            Thread {
                try {
                    val j = URL(
                        "https://geocoding-api.open-meteo.com/v1/search?name=" +
                            java.net.URLEncoder.encode(q, "UTF-8") +
                            "&count=1&language=es"
                    ).readText()
                    val lat = Regex("\"latitude\":([\\d.\\-]+)").find(j)?.groupValues?.get(1)
                    val lon = Regex("\"longitude\":([\\d.\\-]+)").find(j)?.groupValues?.get(1)
                    val name = Regex("\"name\":\"([^\"]+)\"").find(j)?.groupValues?.get(1)
                    val country = Regex("\"country\":\"([^\"]+)\"").find(j)?.groupValues?.get(1)
                    if (lat == null || lon == null) ui("Ciudad no encontrada. Prueba otra.")
                    else fetch(lat, lon, "$name, ${country ?: ""}")
                } catch (e: Exception) {
                    ui("Sin conexión al geocodificador.")
                }
            }.start()
        }
        return b.root
    }

    private fun locate() {
        out("Obteniendo ubicación…")
        LocationServices.getFusedLocationProviderClient(requireActivity())
            .lastLocation
            .addOnSuccessListener { loc ->
                if (!isAdded) return@addOnSuccessListener
                if (loc == null) {
                    out("Sin señal GPS ahora. Escribe la ciudad a mano.")
                    return@addOnSuccessListener
                }
                Thread {
                    fetch(loc.latitude.toString(), loc.longitude.toString(), "${loc.latitude}, ${loc.longitude}")
                }.start()
            }
            .addOnFailureListener { ui("Error de ubicación. Escribe la ciudad a mano.") }
    }

    private fun fetch(lat: String, lon: String, label: String) {
        ui("Consultando Open-Meteo…")
        try {
            val j = URL(
                "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,weather_code&timezone=auto"
            ).readText()
            val t = Regex("\"temperature_2m\":([\\d.\\-]+)").find(j)?.groupValues?.get(1) ?: "--"
            val code = Regex("\"weather_code\":(\\d+)").find(j)?.groupValues?.get(1)?.toInt() ?: -1
            val time = Regex("\"time\":\"([^\"]+)\"").find(j)?.groupValues?.get(1) ?: ""
            ui("${wmo(code)}\n🌡️ $t °C\n📍 $label\n$time")
        } catch (e: Exception) {
            ui("Sin conexión al API de clima. Revisa internet.")
        }
    }

    private fun wmo(code: Int): String = when (code) {
        0 -> "☀️ Despejado"
        1, 2, 3 -> "⛅ Nublado parcial"
        45, 48 -> "🌫️ Niebla"
        in 51..57 -> "🌦️ Llovizna"
        in 61..67 -> "🌧️ Lluvia"
        in 71..77 -> "❄️ Nieve"
        in 80..82 -> "🌧️ Chubascos"
        else -> if (code >= 95) "⛈️ Tormenta" else "🌡️ Código $code"
    }

    private fun out(s: String): ClimaFragment { ui(s); return this }
    private fun ui(s: String) {
        activity?.runOnUiThread { _b?.tvOut?.text = s }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
