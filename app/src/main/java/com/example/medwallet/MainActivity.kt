package com.example.medwallet

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medwallet.ui.DocumentsScreen
import com.example.medwallet.ui.EmergencyScreen
import com.example.medwallet.ui.MedWalletTheme
import com.example.medwallet.ui.ProfileScreen
import com.example.medwallet.ui.WalletViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide medical data from screenshots and the recent-apps preview.
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContent { MedWalletTheme { WalletApp() } }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    EMERGENCY("Emergency", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.Person),
    DOCUMENTS("Documents", Icons.Default.AccountBox)
}

@Composable
fun WalletApp(vm: WalletViewModel = viewModel()) {
    var tabIndex by rememberSaveable { mutableStateOf(0) }
    val tab = Tab.values()[tabIndex]
    val profile by vm.profile.collectAsStateWithLifecycle()
    val docs by vm.documents.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.values().forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = i == tabIndex,
                        onClick = { tabIndex = i },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                Tab.EMERGENCY -> EmergencyScreen(profile, vm.guardArmed, vm::setGuard)
                Tab.PROFILE -> ProfileScreen(profile, vm::saveProfile)
                Tab.DOCUMENTS -> DocumentsScreen(docs, vm)
            }
        }
    }
}
