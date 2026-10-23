package com.example.medwallet.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Listens to android.intent.action.SIM_STATE_CHANGED.
 * Registered in the manifest AND dynamically by [SimGuardService]: on recent Android versions
 * manifest delivery of this broadcast is not guaranteed, the running service is the reliable path.
 */
class SimChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SimGuard.ACTION_SIM_STATE_CHANGED) return
        // "ss" = ABSENT, NOT_READY, LOCKED, READY, IMSI, LOADED
        SimGuard.onSimState(context, intent.getStringExtra("ss"))
    }
}
