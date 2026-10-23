package com.example.medwallet.security

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Loud looping alarm + vibration, played on the ALARM stream so it ignores silent mode. */
object AlarmController {
    private val _active = MutableStateFlow(false)
    val active: StateFlow<Boolean> = _active.asStateFlow()

    private var player: MediaPlayer? = null
    private val alarmAttrs = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    @Synchronized
    fun trigger(context: Context) {
        if (_active.value) return
        val app = context.applicationContext
        _active.value = true
        startSound(app)
        startVibration(app)
        AlertNotifier.showAlert(app) // full-screen intent -> EmergencyAlertActivity over the lock screen
        runCatching { SimGuardService.start(app) } // keep the process alive while the alarm runs
    }

    @Synchronized
    fun stop(context: Context) {
        val app = context.applicationContext
        player?.let { p ->
            runCatching { p.stop() }
            p.release()
        }
        player = null
        vibrator(app).cancel()
        AlertNotifier.cancelAlert(app)
        _active.value = false
    }

    private fun startSound(app: Context) {
        runCatching {
            val audio = app.getSystemService(AudioManager::class.java)
            audio.setStreamVolume(AudioManager.STREAM_ALARM, audio.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0)
        }
        runCatching {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            player = MediaPlayer().apply {
                setAudioAttributes(alarmAttrs)
                setDataSource(app, uri)
                isLooping = true
                prepare()
                start()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun startVibration(app: Context) {
        val effect = VibrationEffect.createWaveform(longArrayOf(0, 800, 400, 800, 400), 0) // 0 = repeat
        runCatching { vibrator(app).vibrate(effect, alarmAttrs) }
    }

    @Suppress("DEPRECATION")
    private fun vibrator(app: Context): Vibrator =
        if (Build.VERSION.SDK_INT >= 31) {
            (app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            app.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
}
