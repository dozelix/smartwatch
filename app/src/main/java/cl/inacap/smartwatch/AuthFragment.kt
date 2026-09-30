package cl.inacap.smartwatch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cl.inacap.smartwatch.databinding.FragmentAuthBinding

/** Login/registro: Firebase real si hay backend, si no demo local. */
class AuthFragment : Fragment() {

    private var _b: FragmentAuthBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentAuthBinding.inflate(inflater, container, false)
        refresh()
        b.btnLogin.setOnClickListener { go(register = false) }
        b.btnReg.setOnClickListener { go(register = true) }
        b.btnOut.setOnClickListener {
            AuthStore.signOut(requireContext())
            refresh()
        }
        return b.root
    }

    private fun go(register: Boolean) {
        val em = b.etEmail.text.toString().trim()
        val pw = b.etPass.text.toString()
        b.tvMsg.text = "Procesando…"
        Thread {
            val r = AuthStore.signIn(requireContext(), em, pw, register)
            activity?.runOnUiThread {
                _b?.tvMsg?.text = (if (r.ok) "✅ " else "❌ ") + r.msg
                if (r.ok) refresh()
            }
        }.start()
    }

    private fun refresh() {
        _b?.btnOut?.visibility = if (AuthStore.email != null) View.VISIBLE else View.GONE
        _b?.tvMsg?.text = AuthStore.email?.let {
            "Sesión: $it (${if (AuthStore.cloud) "Firebase" else "demo local"})"
        } ?: "Valida email y clave ≥ 6. Sin google-services.json usa demo local."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
