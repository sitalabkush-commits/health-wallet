package com.example.medwallet.security

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager

/** Shared entry point used by the manifest receiver, the dynamic receiver and the guard service. */
object SimGuard {
    const val ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED"
    private const val NOT_READY_CONFIRM_MS = 10_000L

    /** Current SIM state of slot 0 as the same strings the broadcast uses. */
    fun currentState(ctx: Context): String {
        val tm = ctx.getSystemService(TelephonyManager::class.java) ?: return "UNKNOWN"
        return when (tm.simState) {
            TelephonyManager.SIM_STATE_ABSENT -> "ABSENT"
            TelephonyManager.SIM_STATE_READY -> "READY"
            TelephonyManager.SIM_STATE_PIN_REQUIRED,
            TelephonyManager.SIM_STATE_PUK_REQUIRED,
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "LOCKED"
            TelephonyManager.SIM_STATE_NOT_READY -> "NOT_READY"
            else -> "UNKNOWN"
        }
    }

    fun onSimState(ctx: Context, state: String?, confirmed: Boolean = false) {
        val app = ctx.applicationContext
        val airplane = Settings.Global.getInt(app.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1

        when (SimStatePolicy.decide(state, GuardPrefs.isArmed(app), airplane, GuardPrefs.wasSimPresent(app))) {
            SimStatePolicy.Decision.MARK_PRESENT -> GuardPrefs.setSimPresent(app, true)
            SimStatePolicy.Decision.IGNORE -> Unit
            SimStatePolicy.Decision.ALARM ->
                if (state == "NOT_READY" && !confirmed) {
                    // NOT_READY can be a brief modem hiccup: re-check before screaming.
                    Handler(Looper.getMainLooper()).postDelayed(
                        { onSimState(app, currentState(app), confirmed = true) }, NOT_READY_CONFIRM_MS
                    )
                } else {
                    // Mark "gone" first: duplicate broadcasts (manifest + dynamic receiver) become no-ops.
                    GuardPrefs.setSimPresent(app, false)
                    AlarmController.trigger(app)
                }
        }
    }
}
