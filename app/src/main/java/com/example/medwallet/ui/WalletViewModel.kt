package com.example.medwallet.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medwallet.data.AppDatabase
import com.example.medwallet.data.DocumentStore
import com.example.medwallet.data.MedicalDocument
import com.example.medwallet.data.MedicalProfile
import com.example.medwallet.security.GuardPrefs
import com.example.medwallet.security.SimGuardService
import com.example.medwallet.widget.EmergencyWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class WalletViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val store = DocumentStore(app)

    val profile: StateFlow<MedicalProfile?> = db.profileDao().observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val documents: StateFlow<List<MedicalDocument>> = db.documentDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var guardArmed by mutableStateOf(GuardPrefs.isArmed(app))
        private set

    fun saveProfile(p: MedicalProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            db.profileDao().upsert(p.copy(id = MedicalProfile.SINGLETON_ID))
            EmergencyWidget().updateAll(getApplication<Application>())
        }
    }

    /** [tempFile] = camera capture in cache; deleted once the encrypted copy exists. */
    fun addDocument(uri: Uri, title: String, type: String, tempFile: File? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val name = store.saveFrom(uri)
                db.documentDao().insert(
                    MedicalDocument(
                        title = title.ifBlank { "Untitled" },
                        type = type,
                        fileName = name,
                        addedAt = System.currentTimeMillis()
                    )
                )
            }
            tempFile?.delete()
        }
    }

    fun deleteDocument(doc: MedicalDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            store.delete(doc.fileName)
            db.documentDao().delete(doc)
        }
    }

    fun loadBitmap(fileName: String, maxSide: Int): Bitmap? = store.loadBitmap(fileName, maxSide)

    fun setGuard(enabled: Boolean) {
        val app = getApplication<Application>()
        if (enabled) SimGuardService.arm(app) else SimGuardService.disarm(app)
        guardArmed = enabled
    }
}
