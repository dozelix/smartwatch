package cl.inacap.smartwatch

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat

/** Dispara la alarma: notificación + beep + vibración. */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(ctx: Context, intent: Intent) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel("alarma", "Alarmas", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val n = NotificationCompat.Builder(ctx, "alarma")
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle("⏰ Smartwatch")
            .setContentText("Tu alarma programada sonó.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        try {
            nm.notify(1, n)
        } catch (_: SecurityException) {
            // Sin permiso POST_NOTIFICATIONS: igual se deja el aviso en pantalla al volver.
        }
        try {
            ToneGenerator(AudioManager.STREAM_ALARM, 100)
                .startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1500)
        } catch (_: Exception) { }
        try {
            @Suppress("DEPRECATION")
            val v = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) { }
    }
}
