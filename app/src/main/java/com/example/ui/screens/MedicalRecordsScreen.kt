package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.util.ImageStorageHelper
import java.io.File
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.VeterinaryDrug
import com.example.data.local.entity.MedicalRecord
import com.example.data.local.entity.Medication
import com.example.data.local.entity.Pet
import com.example.data.local.entity.PetPhoto
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.PillPurple
import com.example.ui.theme.VaccineBlue

@Composable
fun MedicalRecordsScreen(
    pet: Pet?,
    medicalRecords: List<MedicalRecord>,
    medications: List<Medication>,
    photos: List<PetPhoto> = emptyList(),
    latestWeight: Double? = null,
    onAddRecordClick: () -> Unit,
    onEditRecordClick: (MedicalRecord) -> Unit = {},
    onAddMedicationClick: () -> Unit,
    onAddPhotoClick: () -> Unit = {},
    onEditPhotoClick: (PetPhoto) -> Unit = {},
    onDeleteRecord: (MedicalRecord) -> Unit,
    onDeleteMedication: (Medication) -> Unit,
    onDeletePhoto: (PetPhoto) -> Unit = {},
    onMarkDoseTaken: (Medication) -> Unit,
    onToggleMedActive: (Medication) -> Unit,
    onExportClick: () -> Unit,
    onAssignDrugToPet: (VeterinaryDrug) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Vyšetrenie", "Moje Lieky", "Databáza Liekov", "Fotogaléria & Hojenie", "Export")

    val categories = listOf("Všetky", "Očkovanie", "Vyšetrenie", "Operácie", "Rozbor moču", "Krvné testy", "RTG / Sono", "Odčervenie", "Zuby")
    var selectedCategoryFilter by remember { mutableStateOf("Všetky") }

    val photoCategories = listOf("Všetky", "Hojenie rany", "Zuby / Ústna dutina", "Kožný nález", "Lekárska správa / Recept", "Portrét")
    var selectedPhotoCategory by remember { mutableStateOf("Všetky") }

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            fontSize = 12.5.sp
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // MEDICAL RECORDS & VACCINATIONS
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Zdravotná História (${medicalRecords.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = onAddRecordClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_record_screen_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pridať Záznam", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(cat) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredRecords = if (selectedCategoryFilter == "Všetky") {
                        medicalRecords
                    } else {
                        medicalRecords.filter { record ->
                            when (selectedCategoryFilter) {
                                "Operácie" -> record.type.contains("Operácia", ignoreCase = true) || record.type.contains("Zákrok", ignoreCase = true)
                                "Rozbor moču" -> record.type.contains("moč", ignoreCase = true)
                                else -> record.type.contains(selectedCategoryFilter, ignoreCase = true)
                            }
                        }
                    }

                    if (filteredRecords.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Žiadne záznamy v tejto kategórii",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredRecords, key = { it.id }) { record ->
                                MedicalRecordCard(
                                    record = record,
                                    onEdit = { onEditRecordClick(record) },
                                    onDelete = { onDeleteRecord(record) }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // MEDICATIONS & DOSAGE
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Evidencia Liekov (${medications.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = onAddMedicationClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PillPurple),
                                modifier = Modifier.testTag("add_medication_screen_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pridať", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Shortcut to Drug Database
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTab = 2 }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Otvoriť Databázu Liekov & Letáky",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Bravecto, Apoquel, Meloxikam, NexGard, dávkovanie...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (medications.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Medication,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Zatiaľ neevidujete žiadne lieky.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(medications, key = { it.id }) { med ->
                                DetailedMedicationCard(
                                    medication = med,
                                    onTakeDose = { onMarkDoseTaken(med) },
                                    onToggleActive = { onToggleMedActive(med) },
                                    onDelete = { onDeleteMedication(med) }
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // VETERINARY DRUG DATABASE & LEAFLETS
                VeterinaryDrugDatabaseScreen(
                    selectedPet = pet,
                    latestWeight = latestWeight,
                    onAssignDrugToPet = { drug ->
                        onAssignDrugToPet(drug)
                    }
                )
            }

            3 -> {
                // PHOTO GALLERY & HEALING TRACKER
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Zdravotná Fotogaléria (${photos.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dokumentácia rán, zubov a lekárskych správ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = onAddPhotoClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_photo_screen_button")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pridať Foto", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        photoCategories.forEach { cat ->
                            FilterChip(
                                selected = selectedPhotoCategory == cat,
                                onClick = { selectedPhotoCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredPhotos = if (selectedPhotoCategory == "Všetky") {
                        photos
                    } else {
                        photos.filter { it.category == selectedPhotoCategory }
                    }

                    if (filteredPhotos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Žiadne fotografie v tejto kategórii",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredPhotos, key = { it.id }) { photo ->
                                PhotoItemCard(
                                    photo = photo,
                                    onEdit = { onEditPhotoClick(photo) },
                                    onDelete = { onDeletePhoto(photo) }
                                )
                            }
                        }
                    }
                }
            }

            4 -> {
                // DIRECT EXPORT CARD
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Export Záznamu pre Veterinára",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Kompletná anamnéza priamo do e-mailu",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Lekári často nemajú prístup ku kompletnej histórii, ak navštívite pohotovosť alebo zmeníte kliniku. Tento export vygeneruje prehľadný súhrn:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "✓ Všetky podávané lieky a presné dávkovania", style = MaterialTheme.typography.bodySmall)
                            Text(text = "✓ Vakcinácie DHPPi, L4, besnota, odčervenia", style = MaterialTheme.typography.bodySmall)
                            Text(text = "✓ Nálezy z krvi, sono a RTG vyšetrení", style = MaterialTheme.typography.bodySmall)
                            Text(text = "✓ Vývoj a grafy hmotnosti", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = onExportClick,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_export_sheet_button")
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Odoslať Zdravotný Záznam")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalRecordCard(
    record: MedicalRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val typeColor = when {
        record.type.contains("Očkovanie") -> VaccineBlue
        record.type.contains("Krvné") -> Color(0xFFDC2626)
        record.type.contains("RTG") || record.type.contains("Sono") -> Color(0xFF7C3AED)
        record.type.contains("Odčervenie") -> MedicalGreen
        record.type.contains("Operácia") || record.type.contains("Zákrok") -> Color(0xFFE11D48)
        record.type.contains("moč", ignoreCase = true) -> Color(0xFFD97706)
        record.type.contains("Zuby") -> Color(0xFF0284C7)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = typeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = record.type.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = typeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.date,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .testTag("edit_record_${record.id}")
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Upraviť záznam",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .testTag("delete_record_${record.id}")
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Zmazať",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = record.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (record.vetClinic.isNotBlank()) {
                Text(
                    text = "Klinika: ${record.vetClinic}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (record.diagnosis.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Diagnóza / Nález: ${record.diagnosis}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (record.nextDueDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Event,
                        contentDescription = null,
                        tint = VaccineBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Najbližší termín: ${record.nextDueDate}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VaccineBlue
                    )
                }
            }

            if (record.costEur > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cena: ${record.costEur} €",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ATTACHMENT / PHOTO SECTION
            if (record.photoUri.isNotBlank() || record.attachmentUri.isNotBlank()) {
                val context = LocalContext.current
                var showFullImage by remember { mutableStateOf(false) }

                if (showFullImage && record.photoUri.isNotBlank()) {
                    androidx.compose.ui.window.Dialog(onDismissRequest = { showFullImage = false }) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = record.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { showFullImage = false }) {
                                        Icon(Icons.Default.Check, contentDescription = "Zavrieť")
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(if (record.photoUri.startsWith("/")) File(record.photoUri) else record.photoUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Zväčšená fotografia",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (record.photoUri.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable { showFullImage = true }
                                .padding(4.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(if (record.photoUri.startsWith("/")) File(record.photoUri) else record.photoUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Náhľad fotografie",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Snímka",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }

                    if (record.attachmentUri.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDC2626).copy(alpha = 0.1f),
                            modifier = Modifier
                                .clickable { ImageStorageHelper.openAttachment(context, record.attachmentUri) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = record.attachmentName.ifBlank { "Príloha (PDF)" },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedMedicationCard(
    medication: Medication,
    onTakeDose: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (medication.isActive) PillPurple.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onToggleActive() }
                ) {
                    Text(
                        text = if (medication.isActive) "AKTÍVNY" else "DOUŽÍVANÝ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (medication.isActive) PillPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${medication.startDate} - ${medication.endDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Zmazať",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = medication.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Forma: ${medication.form} • Dávka: ${medication.dosage}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Interval: ${medication.frequency}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (medication.timingNotes.isNotBlank()) {
                Text(
                    text = "Pokyny: ${medication.timingNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (medication.prescribingVet.isNotBlank()) {
                Text(
                    text = "Predpísal: ${medication.prescribingVet}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (medication.isActive) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Podaných: ${medication.takenDoses} / ${medication.totalDoses}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onTakeDose,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PillPurple),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Zaznamenať Dávku", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoItemCard(
    photo: PetPhoto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showFullScreenImage by remember { mutableStateOf(false) }
    val isPdf = photo.imageUri.endsWith(".pdf", ignoreCase = true)

    if (showFullScreenImage && photo.imageUri.isNotBlank() && !isPdf) {
        Dialog(onDismissRequest = { showFullScreenImage = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = photo.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${photo.category} • ${photo.date}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showFullScreenImage = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Zavrieť")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photo.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = photo.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    if (photo.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = photo.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = photo.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = photo.date,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .testTag("edit_photo_${photo.id}")
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Upraviť záznam",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .testTag("delete_photo_${photo.id}")
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Zmazať",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Photo or PDF representation
            if (photo.imageUri.isNotBlank()) {
                if (isPdf) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDC2626).copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { ImageStorageHelper.openAttachment(context, photo.imageUri) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF správa",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = photo.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Lekárska správa / PDF (kliknutím otvoríte)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { ImageStorageHelper.openAttachment(context, photo.imageUri) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Otvoriť", fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showFullScreenImage = true }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photo.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = photo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = photo.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Zväčšiť",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = photo.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (photo.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = photo.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
