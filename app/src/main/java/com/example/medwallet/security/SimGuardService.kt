package com.example.medwallet.security

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat

/**
 * Foreground service that keeps the process alive and registers [SimChangeReceiver]
 * at runtime. Being a foreground service is also what lets the alarm start reliably.
 */
class SimGuardService : Service() {
    private val receiver = SimChangeReceiver()

    override fun onCreate() {
        super.onCreate()
        AlertNotifier.ensureChannels(this)
        ServiceCompat.startForeground(
            this, AlertNotifier.ID_GUARD, AlertNotifier.guardNotification(this),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        ContextCompat.registerReceiver(
            this, receiver, IntentFilter(SimGuard.ACTION_SIM_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        // Catches a SIM removed while the phone was off / service not running.
        SimGuard.onSimState(this, SimGuard.currentState(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onDestroy() {
        runCatching { unregisterReceiver(receiver) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(ctx: Context) {
            ContextCompat.startForegroundService(ctx, Intent(ctx, SimGuardService::class.java))
        }

        /** Called from the UI when the user turns protection on. */
        fun arm(ctx: Context) {
            val app = ctx.applicationContext
            GuardPrefs.setArmed(app, true)
            // Baseline: remember whether a SIM is in right now, so the first removal is detected.
            val state = SimGuard.currentState(app)
            GuardPrefs.setSimPresent(app, state == "READY" || state == "LOCKED")
            start(app)
        }

        fun disarm(ctx: Context) {
            val app = ctx.applicationContext
            GuardPrefs.setArmed(app, false)
            AlarmController.stop(app)
            app.stopService(Intent(app, SimGuardService::class.java))
        }
    }
}
