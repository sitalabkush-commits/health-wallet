package com.example.medwallet.ui

import android.app.Activity
import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.medwallet.data.AppDatabase
import com.example.medwallet.data.MedicalProfile
import com.example.medwallet.security.AlarmController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Full-screen alert shown over the lock screen when the SIM is removed. */
class EmergencyAlertActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent { MedWalletTheme { AlertScreen(onClose = { finish() }) } }
    }
}

@Composable
private fun AlertScreen(onClose: () -> Unit) {
    val ctx = LocalContext.current
    val active by AlarmController.active.collectAsStateWithLifecycle()
    LaunchedEffect(active) { if (!active) onClose() }
    BackHandler { /* back does not silence the alarm */ }

    // Shown to whoever finds the phone. May be unavailable before the first unlock after boot.
    val profile by produceState<MedicalProfile?>(null) {
        value = withContext(Dispatchers.IO) {
            runCatching { AppDatabase.get(ctx).profileDao().get() }.getOrNull()
        }
    }

    // Stopping the alarm requires the device PIN / pattern / password / biometrics.
    val credential = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) AlarmController.stop(ctx)
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFFB71C1C)).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "SIM CARD REMOVED", color = Color.White, textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold
        )
        Text(
            "This phone is protected by an anti-theft alarm.",
            color = Color.White, textAlign = TextAlign.Center
        )

        profile?.takeIf { it.emergencyContactPhone.isNotBlank() }?.let { p ->
            Text(
                "If you found this phone, please call ${p.emergencyContactName.ifBlank { "the owner" }}:",
                color = Color.White, textAlign = TextAlign.Center
            )
            Button(
                onClick = { dialNumber(ctx, p.emergencyContactPhone) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFB71C1C)),
                modifier = Modifier.fillMaxWidth()
            ) { Text(p.emergencyContactPhone, fontWeight = FontWeight.Bold) }
        }

        Button(
            onClick = {
                val km = ctx.getSystemService(KeyguardManager::class.java)
                val intent = km?.createConfirmDeviceCredentialIntent(
                    "Stop alarm", "Confirm your screen lock to stop the alarm"
                )
                // No screen lock set -> nothing to verify against, so just stop.
                if (intent != null) credential.launch(intent) else AlarmController.stop(ctx)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Stop alarm (owner)") }
    }
}
