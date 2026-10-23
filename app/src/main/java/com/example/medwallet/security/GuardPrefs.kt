package com.example.medwallet.security

import android.content.Context

/** Non-sensitive guard flags (plain prefs are fine: they hold no medical data). */
object GuardPrefs {
    private fun prefs(ctx: Context) =
        ctx.applicationContext.getSharedPreferences("guard_prefs", Context.MODE_PRIVATE)

    fun isArmed(ctx: Context) = prefs(ctx).getBoolean("armed", false)
    fun setArmed(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("armed", v).apply()

    fun wasSimPresent(ctx: Context) = prefs(ctx).getBoolean("sim_present", false)
    fun setSimPresent(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("sim_present", v).apply()
}
