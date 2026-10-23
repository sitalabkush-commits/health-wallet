package com.example.medwallet.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Restarts the guard after reboot / app update (if the user armed it). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!GuardPrefs.isArmed(context)) return
        runCatching { SimGuardService.start(context) }
    }
}
