package com.example.medwallet.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.medwallet.MainActivity
import com.example.medwallet.data.AppDatabase
import com.example.medwallet.data.MedicalProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Home-screen widget showing vital info without unlocking the phone.
 * Update it with EmergencyWidget().updateAll(context) after the profile changes.
 */
class EmergencyWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Read before composing; runCatching so a not-yet-unlocked device shows the placeholder.
        val profile = withContext(Dispatchers.IO) {
            runCatching { AppDatabase.get(context).profileDao().get() }.getOrNull()
        }
        val openApp = Intent(context, MainActivity::class.java)
        provideContent { WidgetContent(profile, openApp) }
    }
}

@Composable
private fun WidgetContent(profile: MedicalProfile?, openApp: Intent) {
    val bg = ColorProvider(Color(0xFFB71C1C))
    val fg = ColorProvider(Color.White)
    val visible = profile != null && profile.showOnWidget &&
        (profile.bloodGroup.isNotBlank() || profile.emergencyContactPhone.isNotBlank())

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bg)
            .cornerRadius(16.dp)
            .padding(12.dp)
            .clickable(actionStartActivity(openApp))
    ) {
        Text(
            "EMERGENCY MEDICAL INFO",
            style = TextStyle(color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        )
        if (!visible || profile == null) {
            Text("Tap to set up your emergency info", style = TextStyle(color = fg, fontSize = 14.sp))
            return@Column
        }
        if (profile.fullName.isNotBlank()) {
            Text(profile.fullName, style = TextStyle(color = fg, fontSize = 14.sp))
        }
        Text(
            "Blood: ${profile.bloodGroup.ifBlank { "—" }}",
            style = TextStyle(color = fg, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        )
        if (profile.allergies.isNotBlank()) {
            Text(
                "Allergies: ${profile.allergies}",
                style = TextStyle(color = fg, fontSize = 13.sp),
                maxLines = 2
            )
        }
        if (profile.emergencyContactPhone.isNotBlank()) {
            val dial = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", profile.emergencyContactPhone, null))
            Text(
                "Call ${profile.emergencyContactName.ifBlank { "contact" }}: ${profile.emergencyContactPhone}",
                style = TextStyle(color = fg, fontSize = 15.sp, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.fillMaxWidth().padding(top = 6.dp).clickable(actionStartActivity(dial))
            )
        }
    }
}

class EmergencyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EmergencyWidget()
}
