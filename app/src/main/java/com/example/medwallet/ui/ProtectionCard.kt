package com.example.medwallet.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/** Toggle for the SIM-removal alarm, with the runtime permissions it needs. */
@Composable
fun ProtectionCard(armed: Boolean, onToggle: (Boolean) -> Unit) {
    val ctx = LocalContext.current
    // Notification permission only affects the lock-screen pop-up; the alarm sound works without it.
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        onToggle(true)
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Anti-theft SIM protection", fontWeight = FontWeight.Bold)
                    Text(
                        "Sounds a loud alarm and shows a lock-screen alert if the SIM card is removed. " +
                            "Stopping it needs your screen lock.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = armed, onCheckedChange = { on ->
                    val needsNotifPermission = on && Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                    if (needsNotifPermission) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else onToggle(on)
                })
            }
            if (armed && Build.VERSION.SDK_INT >= 34 &&
                !ctx.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
            ) {
                TextButton(onClick = {
                    ctx.startActivity(
                        Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:${ctx.packageName}"))
                    )
                }) { Text("Allow full-screen alerts (needed on Android 14+)") }
            }
        }
    }
}
