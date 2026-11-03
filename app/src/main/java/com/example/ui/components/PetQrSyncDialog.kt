package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.Pet
import com.example.util.PetSyncExport
import com.example.util.QrCodeUtils

@Composable
fun PetQrSyncDialog(
    selectedPet: Pet?,
    getQrBitmap: () -> Bitmap?,
    getSyncPackage: () -> PetSyncExport?,
    onImportSyncData: (PetSyncExport, (Boolean, String) -> Unit) -> Unit,
    onPrintPdf: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Generovať QR, 1: Načítať QR
    val qrBitmap = remember { getQrBitmap() }
    val syncPackage = remember { getSyncPackage() }

    // Import tab states
    var rawInputText by remember { mutableStateOf("") }
    var decodedSyncData by remember { mutableStateOf<PetSyncExport?>(null) }
    var scanErrorMessage by remember { mutableStateOf<String?>(null) }
    var isImportSuccess by remember { mutableStateOf(false) }

    // Camera launcher to scan QR
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val decoded = QrCodeUtils.decodeBarcodeFromBitmap(bitmap)
            if (decoded != null) {
                rawInputText = decoded
                val parsed = QrCodeUtils.fromJson(decoded)
                if (parsed != null) {
                    decodedSyncData = parsed
                    scanErrorMessage = null
                } else {
                    scanErrorMessage = "QR kód bol prečítaný, ale neobsahuje platné AnimaCura_SK dáta."
                }
            } else {
                scanErrorMessage = "Nepodarilo sa rozpoznať QR kód. Skúste lepšie osvetlenie."
            }
        }
    }

    val launchCameraSafe: () -> Unit = {
        try {
            cameraLauncher.launch(null)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Nepodarilo sa spustiť fotoaparát. Skúste vybrať QR kód z galérie.", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCameraSafe()
        } else {
            Toast.makeText(context, "Pre skenovanie QR kódu povoľte fotoaparát v nastaveniach.", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery launcher to pick QR image
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val decoded = QrCodeUtils.decodeBarcodeFromBitmap(bitmap)
                    if (decoded != null) {
                        rawInputText = decoded
                        val parsed = QrCodeUtils.fromJson(decoded)
                        if (parsed != null) {
                            decodedSyncData = parsed
                            scanErrorMessage = null
                        } else {
                            scanErrorMessage = "Snímka neobsahuje platný AnimaCura_SK balíček."
                        }
                    } else {
                        scanErrorMessage = "Na vybratej fotke nebol nájdený QR kód."
                    }
                }
            } catch (e: Exception) {
                scanErrorMessage = "Chyba načítania: ${e.message}"
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("pet_qr_sync_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "QR Synchronizácia",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Prenos záznamov bez ručného písania",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavrieť")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Generovať vs Načítať
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Vytvoriť QR kód") },
                        icon = { Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Načítať QR z mobilu") },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // TAB 0: GENERATE QR
                    if (selectedPet == null) {
                        Text("Najprv vyberte zvieratko na vytvorenie QR kódu.")
                    } else {
                        Text(
                            text = "Tento QR kód obsahuje kompletnú kartu zvieratka ${selectedPet.name} (čip, očkovania, lieky, váhu). V druhom telefóne ho stačí naskenovať.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // QR Code Bitmap container
                        if (qrBitmap != null) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = androidx.compose.ui.graphics.Color.White,
                                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .size(240.dp)
                                    .padding(8.dp)
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "QR kód záznamov zvieratka",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Text("Nepodarilo sa vygenerovať QR kód.")
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // QR summary preview
                        syncPackage?.let { sync ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Obsah QR kódu:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("• Zviera: ${sync.petName} (${sync.breed.ifBlank { sync.species }})", style = MaterialTheme.typography.bodySmall)
                                    Text("• Čip / Pas: ${sync.chipNumber.ifBlank { "–" }} / ${sync.passportNumber.ifBlank { "–" }}", style = MaterialTheme.typography.bodySmall)
                                    Text("• Hmotnosť: ${sync.latestWeight} kg", style = MaterialTheme.typography.bodySmall)
                                    Text("• Aktívne lieky: ${sync.activeMedications.size} ks", style = MaterialTheme.typography.bodySmall)
                                    Text("• Posledné vakcíny: ${sync.recentVaccinations.size} ks", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Print PDF with QR or Share JSON text
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onPrintPdf()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tlačiť / PDF")
                            }

                            OutlinedButton(
                                onClick = {
                                    syncPackage?.let { sync ->
                                        val json = QrCodeUtils.toJson(sync)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "AnimaCura_SK QR Balíček - ${sync.petName}")
                                            putExtra(Intent.EXTRA_TEXT, json)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Zdieľať dáta zvieratka"))
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Zdieľať")
                            }
                        }
                    }
                } else {
                    // TAB 1: SCAN / READ QR
                    Text(
                        text = "Namierte kameru na QR kód v druhom telefóne alebo vyberte fotku s QR kódom:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    launchCameraSafe()
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("scan_qr_camera_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kamera")
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("scan_qr_gallery_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Galéria")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = rawInputText,
                        onValueChange = { text ->
                            rawInputText = text
                            val parsed = QrCodeUtils.fromJson(text)
                            decodedSyncData = parsed
                            if (parsed != null) scanErrorMessage = null
                        },
                        label = { Text("Alebo vložte kód / JSON balíček") },
                        placeholder = { Text("{\"app\":\"AnimaCura_SK\",\"name\":\"Bobi\"...}") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 3
                    )

                    if (scanErrorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(scanErrorMessage!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }

                    // Decoded Data Preview & Import button
                    AnimatedVisibility(visible = decodedSyncData != null) {
                        decodedSyncData?.let { sync ->
                            Spacer(modifier = Modifier.height(14.dp))
                            ElevatedCard(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Rozpoznané zvieratko:",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${sync.petName} (${sync.breed.ifBlank { sync.species }})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Číslo mikročipu: ${sync.chipNumber.ifBlank { "Neevidované" }}", style = MaterialTheme.typography.bodySmall)
                                    Text("Aktuálna hmotnosť: ${sync.latestWeight} kg", style = MaterialTheme.typography.bodySmall)
                                    if (sync.activeMedications.isNotEmpty()) {
                                        Text("Lieky: ${sync.activeMedications.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            onImportSyncData(sync) { success, msg ->
                                                isImportSuccess = success
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                                if (success) {
                                                    onDismiss()
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("import_sync_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Importovať a zapísať zmeny bez písania")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
