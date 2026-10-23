package com.example.medwallet.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.medwallet.data.DocTypes
import com.example.medwallet.data.MedicalDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private val dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(docs: List<MedicalDocument>, vm: WalletViewModel) {
    val ctx = LocalContext.current
    var pendingUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingTempPath by rememberSaveable { mutableStateOf<String?>(null) }
    var capturePath by rememberSaveable { mutableStateOf<String?>(null) }
    var viewing by remember { mutableStateOf<MedicalDocument?>(null) }
    var deleting by remember { mutableStateOf<MedicalDocument?>(null) }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { pendingUri = uri; pendingTempPath = null }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val f = capturePath?.let { File(it) }
        if (ok && f != null) {
            pendingUri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", f)
            pendingTempPath = f.absolutePath
        } else f?.delete()
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Prescriptions & Reports", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Images are encrypted and stored in the app's private storage.",
            style = MaterialTheme.typography.bodySmall
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val f = File(ctx.cacheDir, "capture/${UUID.randomUUID()}.jpg").also { it.parentFile?.mkdirs() }
                capturePath = f.absolutePath
                camera.launch(FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", f))
            }) { Icon(Icons.Default.Add, null); Text(" Camera") }
            OutlinedButton(onClick = {
                gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text("Gallery") }
        }

        if (docs.isEmpty()) {
            Text("No documents yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(docs, key = { it.id }) { doc ->
                    Card(Modifier.fillMaxWidth().clickable { viewing = doc }) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            DocImage(vm, doc.fileName, 300, Modifier.size(72.dp), ContentScale.Crop)
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(doc.title, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${doc.type} • " +
                                        Instant.ofEpochMilli(doc.addedAt).atZone(ZoneId.systemDefault()).format(dateFmt),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { deleting = doc }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    // --- name + type dialog after picking/capturing ---
    pendingUri?.let { uri ->
        var title by remember { mutableStateOf("") }
        var type by remember { mutableStateOf(DocTypes.PRESCRIPTION) }
        fun discard() {
            pendingTempPath?.let { File(it).delete() }
            pendingUri = null; pendingTempPath = null
        }
        AlertDialog(
            onDismissRequest = { discard() },
            title = { Text("Save document") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("Title (e.g. Dr. Rao - Jan 2026)") }, singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DocTypes.all.forEach { t ->
                            FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.addDocument(uri, title, type, pendingTempPath?.let { File(it) })
                    pendingUri = null; pendingTempPath = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { discard() }) { Text("Cancel") } }
        )
    }

    // --- delete confirmation ---
    deleting?.let { doc ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete \"${doc.title}\"?") },
            text = { Text("The encrypted file will be permanently removed from this phone.") },
            confirmButton = { TextButton(onClick = { vm.deleteDocument(doc); deleting = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } }
        )
    }

    // --- full-size viewer ---
    viewing?.let { doc ->
        Dialog(onDismissRequest = { viewing = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black).clickable { viewing = null }) {
                DocImage(vm, doc.fileName, 2048, Modifier.fillMaxSize(), ContentScale.Fit)
            }
        }
    }
}

/** Decrypts + decodes off the main thread. */
@Composable
private fun DocImage(vm: WalletViewModel, fileName: String, maxSide: Int, modifier: Modifier, scale: ContentScale) {
    val bmp by produceState<Bitmap?>(null, fileName) {
        value = withContext(Dispatchers.IO) { vm.loadBitmap(fileName, maxSide) }
    }
    val b = bmp
    if (b != null) Image(b.asImageBitmap(), contentDescription = null, modifier = modifier, contentScale = scale)
    else Box(modifier.background(Color.LightGray))
}
