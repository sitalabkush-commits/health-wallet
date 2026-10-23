package com.example.medwallet.security

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.medwallet.ui.EmergencyAlertActivity

object AlertNotifier {
    private const val CH_GUARD = "guard"
    private const val CH_ALERT = "sim_alert"
    const val ID_GUARD = 2000
    private const val ID_ALERT = 2001

    fun ensureChannels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CH_GUARD, "Anti-theft protection", NotificationManager.IMPORTANCE_LOW)
        )
        nm.createNotificationChannel(
            NotificationChannel(CH_ALERT, "SIM removal alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null) // the alarm is played by AlarmController
            }
        )
    }

    fun guardNotification(ctx: Context): Notification =
        NotificationCompat.Builder(ctx, CH_GUARD)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentTitle("Anti-theft protection active")
            .setContentText("An alarm will sound if the SIM card is removed.")
            .setOngoing(true)
            .build()

    fun showAlert(ctx: Context) {
        ensureChannels(ctx)
        val intent = Intent(ctx, EmergencyAlertActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pi = PendingIntent.getActivity(
            ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(ctx, CH_ALERT)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("SIM card removed!")
            .setContentText("Anti-theft alarm is active")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setContentIntent(pi)
            .setFullScreenIntent(pi, true)
            .build()
        ctx.getSystemService(NotificationManager::class.java).notify(ID_ALERT, n)
    }

    fun cancelAlert(ctx: Context) {
        ctx.getSystemService(NotificationManager::class.java).cancel(ID_ALERT)
    }
}
