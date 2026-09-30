package cl.inacap.smartwatch

import android.content.Context
import android.util.Base64
import com.google.android.gms.tasks.Tasks

/** Sesión: Firebase real si hay backend, si no demo local (igual que la versión web). */
object AuthStore {

    data class Result(val ok: Boolean, val msg: String)

    var email: String? = null
        private set
    var cloud: Boolean = false
        private set

    fun initials(): String = email?.take(2)?.uppercase() ?: "?"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences("sw_demo", Context.MODE_PRIVATE)

    fun signIn(ctx: Context, em: String, pw: String, register: Boolean): Result {
        if (!Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+").matches(em))
            return Result(false, "Email inválido.")
        if (pw.length < 6)
            return Result(false, "Clave mínima 6 caracteres.")
        // 1. ¿Hay backend Firebase? (requiere google-services.json, ver README)
        try {
            com.google.firebase.FirebaseApp.getInstance()
        } catch (e: Exception) {
            return demo(ctx, em, pw, register)
        }
        // 2. Backend presente: Auth real (bloqueante, llamar fuera del hilo UI)
        return try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val task = if (register) auth.createUserWithEmailAndPassword(em, pw)
                       else auth.signInWithEmailAndPassword(em, pw)
            val res = Tasks.await(task)
            email = res.user?.email
            cloud = true
            Result(true, "Sesión Firebase: $email")
        } catch (e: Exception) {
            Result(false, "Firebase: ${e.message}")
        }
    }

    private fun demo(ctx: Context, em: String, pw: String, register: Boolean): Result {
        val p = prefs(ctx)
        val key = "u_$em"
        val hash = Base64.encodeToString(pw.toByteArray(), Base64.NO_WRAP)
        if (register) {
            if (p.contains(key)) return Result(false, "Ya existe. Inicia sesión.")
            p.edit().putString(key, hash).apply()
        } else {
            if (p.getString(key, null) != hash)
                return Result(false, "Credenciales demo incorrectas (o regístrate primero).")
        }
        email = em
        cloud = false
        p.edit().putString("sw_user", em).apply()
        return Result(true, "Sesión demo local: $em")
    }

    fun restore(ctx: Context) {
        email = prefs(ctx).getString("sw_user", null)
        cloud = false
    }

    fun signOut(ctx: Context) {
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) { /* sin backend */ }
        email = null
        cloud = false
        prefs(ctx).edit().remove("sw_user").apply()
    }
}
