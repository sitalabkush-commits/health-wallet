package com.example.medwallet.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medwallet.data.MedicalProfile

/** Opens the dialer pre-filled (no CALL_PHONE permission needed). */
fun dialNumber(ctx: Context, phone: String) {
    ctx.startActivity(
        Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

@Composable
fun EmergencyScreen(profile: MedicalProfile?, guardArmed: Boolean, onGuardToggle: (Boolean) -> Unit) {
    val ctx = LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Emergency Info", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        val empty = profile == null || (profile.fullName.isBlank() && profile.bloodGroup.isBlank())
        if (empty || profile == null) {
            Card(Modifier.fillMaxWidth()) {
                Text("No medical profile yet. Open the Profile tab to add your details.", Modifier.padding(16.dp))
            }
        } else {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(profile.fullName.ifBlank { "Name not set" }, style = MaterialTheme.typography.titleLarge)
                    Text("Blood group", style = MaterialTheme.typography.labelLarge)
                    Text(
                        profile.bloodGroup.ifBlank { "—" },
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            InfoCard("Allergies", profile.allergies)
            InfoCard("Chronic illnesses", profile.chronicIllnesses)
            InfoCard("Organ donor", if (profile.organDonor) "Yes" else "No")

            if (profile.emergencyContactPhone.isNotBlank()) {
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Emergency contact", style = MaterialTheme.typography.labelLarge)
                            Text(profile.emergencyContactName.ifBlank { "Contact" }, fontWeight = FontWeight.SemiBold)
                            Text(profile.emergencyContactPhone)
                        }
                        Button(onClick = { dialNumber(ctx, profile.emergencyContactPhone) }) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Text(" Call")
                        }
                    }
                }
            }
        }

        ProtectionCard(armed = guardArmed, onToggle = onGuardToggle)
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value.ifBlank { "None listed" }, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
