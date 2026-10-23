package com.example.medwallet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.medwallet.data.MedicalProfile

private val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

@Composable
fun ProfileScreen(profile: MedicalProfile?, onSave: (MedicalProfile) -> Unit) {
    var form by remember(profile) { mutableStateOf(profile ?: MedicalProfile.empty()) }
    var menuOpen by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    fun edit(change: (MedicalProfile) -> MedicalProfile) {
        form = change(form); saved = false
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Medical Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = form.fullName, onValueChange = { v -> edit { it.copy(fullName = v) } },
            label = { Text("Full name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )

        Box {
            OutlinedButton(onClick = { menuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Blood group: " + form.bloodGroup.ifBlank { "select" })
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                BLOOD_GROUPS.forEach { g ->
                    DropdownMenuItem(text = { Text(g) }, onClick = {
                        edit { it.copy(bloodGroup = g) }; menuOpen = false
                    })
                }
            }
        }

        OutlinedTextField(
            value = form.allergies, onValueChange = { v -> edit { it.copy(allergies = v) } },
            label = { Text("Allergies (e.g. penicillin, peanuts)") }, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = form.chronicIllnesses, onValueChange = { v -> edit { it.copy(chronicIllnesses = v) } },
            label = { Text("Chronic illnesses (e.g. diabetes, asthma)") }, modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Organ donor", Modifier.weight(1f))
            Switch(checked = form.organDonor, onCheckedChange = { v -> edit { it.copy(organDonor = v) } })
        }

        OutlinedTextField(
            value = form.emergencyContactName, onValueChange = { v -> edit { it.copy(emergencyContactName = v) } },
            label = { Text("Primary emergency contact name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = form.emergencyContactPhone, onValueChange = { v -> edit { it.copy(emergencyContactPhone = v) } },
            label = { Text("Primary emergency contact phone") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Show on home-screen widget")
                Text(
                    "Anyone can read the widget without unlocking the phone.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(checked = form.showOnWidget, onCheckedChange = { v -> edit { it.copy(showOnWidget = v) } })
        }

        Button(
            onClick = { onSave(form); saved = true },
            enabled = form.fullName.isNotBlank() && form.emergencyContactPhone.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save") }
        if (saved) Text("Saved on this device (encrypted).", color = MaterialTheme.colorScheme.primary)
    }
}
