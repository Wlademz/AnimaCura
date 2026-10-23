package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.VeterinaryDrug
import com.example.data.local.VeterinaryDrugDatabase
import com.example.util.QrCodeUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BarcodeScannerDialog(
    petName: String,
    onDismiss: () -> Unit,
    onAddMedication: (VeterinaryDrug, dosage: String, frequency: String) -> Unit,
    onAddExpense: (VeterinaryDrug, priceEur: Double) -> Unit
) {
    val context = LocalContext.current
    var manualBarcode by remember { mutableStateOf("") }
    var recognizedDrug by remember { mutableStateOf<VeterinaryDrug?>(null) }
    var scanErrorMessage by remember { mutableStateOf<String?>(null) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var expenseAmountInput by remember { mutableStateOf("32.50") }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val decoded = QrCodeUtils.decodeBarcodeFromBitmap(bitmap)
            if (decoded != null && decoded.isNotBlank()) {
                manualBarcode = decoded
                val drug = VeterinaryDrugDatabase.findDrugByBarcode(decoded)
                if (drug != null) {
                    recognizedDrug = drug
                    scanErrorMessage = null
                } else {
                    recognizedDrug = null
                    scanErrorMessage = "Naskenovaný kód: $decoded. Tento produkt nie je v databáze, môžete ho vyhľadať alebo zaevidovať."
                }
            } else {
                scanErrorMessage = "Nepodarilo sa rozpoznať čiarový kód na snímke. Skúste lepšie svetlo alebo manuálny kód."
            }
        }
    }

    // Gallery launcher
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
                    if (decoded != null && decoded.isNotBlank()) {
                        manualBarcode = decoded
                        val drug = VeterinaryDrugDatabase.findDrugByBarcode(decoded)
                        if (drug != null) {
                            recognizedDrug = drug
                            scanErrorMessage = null
                        } else {
                            recognizedDrug = null
                            scanErrorMessage = "Naskenovaný kód: $decoded (nie je v databáze)."
                        }
                    } else {
                        scanErrorMessage = "Na fotke nebol nájdený žiadny čiarový kód."
                    }
                }
            } catch (e: Exception) {
                scanErrorMessage = "Chyba načítania snímky: ${e.message}"
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
                .testTag("barcode_scanner_dialog"),
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
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Skener čiarových kódov",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Lieky a výživové doplnky",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavrieť")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scan Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scan_camera_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Odfotiť EAN")
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scan_gallery_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Z galérie")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual Barcode Entry
                OutlinedTextField(
                    value = manualBarcode,
                    onValueChange = { input ->
                        manualBarcode = input
                        val drug = VeterinaryDrugDatabase.findDrugByBarcode(input)
                        recognizedDrug = drug
                        if (drug != null) {
                            scanErrorMessage = null
                        }
                    },
                    label = { Text("Zadať EAN kód alebo hľadať") },
                    placeholder = { Text("Napr. 8713184138126 (Bravecto)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (manualBarcode.isNotEmpty()) {
                            IconButton(onClick = {
                                manualBarcode = ""
                                recognizedDrug = null
                                scanErrorMessage = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Vymazať")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("barcode_text_field"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Quick sample barcode chips
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Alebo vyskúšajte overené produkty:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val sampleItems = listOf(
                        "Bravecto" to "8713184138126",
                        "Apoquel" to "5414736029580",
                        "NexGard Spectra" to "3661103046776",
                        "Alavis Maxima" to "8594191410014",
                        "Lososový olej Brit" to "8595602521197",
                        "Canvit Biotin" to "8595602509126",
                        "Vetomune" to "5902414201018"
                    )

                    sampleItems.forEach { (name, code) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (manualBarcode == code) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                manualBarcode = code
                                recognizedDrug = VeterinaryDrugDatabase.findDrugByBarcode(code)
                                scanErrorMessage = null
                            }
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (manualBarcode == code) FontWeight.Bold else FontWeight.Normal,
                                color = if (manualBarcode == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Error Message if any
                if (scanErrorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = scanErrorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Recognized Drug Result Card
                AnimatedVisibility(visible = recognizedDrug != null) {
                    recognizedDrug?.let { drug ->
                        Spacer(modifier = Modifier.height(16.dp))
                        ElevatedCard(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(drug.badgeColorHex)
                                    ) {
                                        Text(
                                            text = if (drug.isSupplement) "🌱 VÝŽIVOVÝ DOPLNOK" else "💊 VETERINÁRNY LIEK",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Text(
                                        text = "EAN: ${drug.eanBarcode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = drug.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Liečivo: ${drug.activeSubstance}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Dávkovanie: ${drug.dosageGuideline}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = drug.indications,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Action Buttons: Add to Meds or Add to Expenses
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onAddMedication(drug, drug.dosageGuideline, "1x denne")
                                            Toast.makeText(context, "${drug.name} bol pridaný medzi lieky pre $petName!", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pridať do liekov")
                                    }

                                    OutlinedButton(
                                        onClick = { showExpenseDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Euro, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Zaevidovať výdavok")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Sub-dialog to record expense with price
    if (showExpenseDialog && recognizedDrug != null) {
        AlertDialog(
            onDismissRequest = { showExpenseDialog = false },
            title = { Text("Zaznamenať výdavok") },
            text = {
                Column {
                    Text("Produkt: ${recognizedDrug!!.name}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expenseAmountInput,
                        onValueChange = { expenseAmountInput = it },
                        label = { Text("Cena v €") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = expenseAmountInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                        onAddExpense(recognizedDrug!!, amount)
                        Toast.makeText(context, "Výdavok $amount € zaznamenaný!", Toast.LENGTH_SHORT).show()
                        showExpenseDialog = false
                        onDismiss()
                    }
                ) {
                    Text("Uložiť výdavok")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpenseDialog = false }) {
                    Text("Zrušiť")
                }
            }
        )
    }
}
