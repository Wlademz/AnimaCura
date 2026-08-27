package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.VeterinaryDrug
import com.example.data.local.VeterinaryDrugDatabase
import com.example.data.local.entity.MedicalRecord
import com.example.data.local.entity.Medication
import com.example.data.local.entity.Pet
import com.example.data.local.entity.WeightLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddPetDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        species: String,
        breed: String,
        birthDate: String,
        gender: String,
        chip: String,
        passport: String,
        vetName: String,
        vetClinic: String,
        vetPhone: String,
        vetEmail: String,
        targetWeight: Double,
        colorHex: Long,
        adoptedFrom: String,
        adoptedBy: String,
        adoptionDate: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val speciesOptions = listOf("Pes", "Mačka", "Králik", "Hlodavec", "Vták", "Iné")
    var selectedSpecies by remember { mutableStateOf("Pes") }
    var breed by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Samec") }
    var chip by remember { mutableStateOf("") }
    var passport by remember { mutableStateOf("") }
    var vetName by remember { mutableStateOf("") }
    var vetClinic by remember { mutableStateOf("") }
    var vetPhone by remember { mutableStateOf("") }
    var vetEmail by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    var adoptedFrom by remember { mutableStateOf("") }
    var adoptedBy by remember { mutableStateOf("") }
    var adoptionDate by remember { mutableStateOf("") }

    val colorPalette = listOf(
        0xFF0D9488, 0xFFE07A5F, 0xFF3B82F6, 0xFF8B5CF6, 0xFF10B981, 0xFFF59E0B
    )
    var selectedColor by remember { mutableStateOf(colorPalette[0]) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nové Zvieratko",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavrieť")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Meno zvieratka *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pet_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Druh zvieratka:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    speciesOptions.forEach { sp ->
                        FilterChip(
                            selected = selectedSpecies == sp,
                            onClick = { selectedSpecies = sp },
                            label = { Text(sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Plemeno (napr. Labrador)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text("Dátum nar. (DD.MM.RRRR)") },
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = { targetWeight = it },
                        label = { Text("Cieľ. váha (kg)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Samec", "Samica", "Kastrovaný").forEach { g ->
                        FilterChip(
                            selected = gender == g,
                            onClick = { gender = g },
                            label = { Text(g) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Oficiálne Identifikátory:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = chip,
                    onValueChange = { chip = it },
                    label = { Text("Číslo mikročipu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = passport,
                    onValueChange = { passport = it },
                    label = { Text("Číslo Pet Pasu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ošetrujúci Veterinár:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = vetName,
                    onValueChange = { vetName = it },
                    label = { Text("Meno veterinára (napr. MVDr. Novák)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = vetClinic,
                    onValueChange = { vetClinic = it },
                    label = { Text("Klinika / Ambulancia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = vetEmail,
                    onValueChange = { vetEmail = it },
                    label = { Text("E-mail veterinára (pre export záznamov)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Pôvod & Adopcia:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = adoptedFrom,
                    onValueChange = { adoptedFrom = it },
                    label = { Text("Odkiaľ adoptovaný / Pôvod") },
                    placeholder = { Text("napr. Útulok Sloboda zvierat, Chovná stanica, Ulica") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pet_adopted_from_input")
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = adoptedBy,
                    onValueChange = { adoptedBy = it },
                    label = { Text("Komu adoptovaný (Osvojiteľ / Majiteľ)") },
                    placeholder = { Text("napr. Vladimír M. & rodina") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pet_adopted_by_input")
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = adoptionDate,
                    onValueChange = { adoptionDate = it },
                    label = { Text("Dátum adopcie / príchodu (DD.MM.RRRR)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Farba profilu:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorPalette.forEach { colorVal ->
                        val isSelected = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorVal }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Zrušiť")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    name.trim(),
                                    selectedSpecies,
                                    breed.trim(),
                                    birthDate.trim(),
                                    gender,
                                    chip.trim(),
                                    passport.trim(),
                                    vetName.trim(),
                                    vetClinic.trim(),
                                    vetPhone.trim(),
                                    vetEmail.trim(),
                                    targetWeight.toDoubleOrNull() ?: 0.0,
                                    selectedColor,
                                    adoptedFrom.trim(),
                                    adoptedBy.trim(),
                                    adoptionDate.trim()
                                )
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("submit_add_pet_button")
                    ) {
                        Text("Uložiť Zvieratko")
                    }
                }
            }
        }
    }
}

@Composable
fun EditPetDialog(
    pet: Pet,
    onDismiss: () -> Unit,
    onConfirm: (Pet) -> Unit,
    onDeleteRequest: () -> Unit
) {
    var name by remember { mutableStateOf(pet.name) }
    val speciesOptions = listOf("Pes", "Mačka", "Králik", "Hlodavec", "Vták", "Iné")
    var selectedSpecies by remember { mutableStateOf(pet.species) }
    var breed by remember { mutableStateOf(pet.breed) }
    var birthDate by remember { mutableStateOf(pet.birthDate) }
    var gender by remember { mutableStateOf(pet.gender) }
    var chip by remember { mutableStateOf(pet.chipNumber) }
    var passport by remember { mutableStateOf(pet.passportNumber) }
    var vetName by remember { mutableStateOf(pet.vetName) }
    var vetClinic by remember { mutableStateOf(pet.vetClinic) }
    var vetPhone by remember { mutableStateOf(pet.vetPhone) }
    var vetEmail by remember { mutableStateOf(pet.vetEmail) }
    var targetWeight by remember { mutableStateOf(if (pet.targetWeightKg > 0) pet.targetWeightKg.toString() else "") }
    var adoptedFrom by remember { mutableStateOf(pet.adoptedFrom) }
    var adoptedBy by remember { mutableStateOf(pet.adoptedBy) }
    var adoptionDate by remember { mutableStateOf(pet.adoptionDate) }

    val colorPalette = listOf(
        0xFF0D9488, 0xFFE07A5F, 0xFF3B82F6, 0xFF8B5CF6, 0xFF10B981, 0xFFF59E0B
    )
    var selectedColor by remember { mutableStateOf(pet.avatarColorHex) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upraviť Údaje (${pet.name})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavrieť")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Meno zvieratka *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_pet_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Druh zvieratka:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    speciesOptions.forEach { sp ->
                        FilterChip(
                            selected = selectedSpecies == sp,
                            onClick = { selectedSpecies = sp },
                            label = { Text(sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Plemeno (napr. Labrador, Britská mačka)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text("Dátum nar. (DD.MM.RRRR)") },
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = { targetWeight = it },
                        label = { Text("Cieľ. váha (kg)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Samec", "Samica", "Samec (kastrovaný)", "Samica (kastrovaná)").forEach { g ->
                        FilterChip(
                            selected = gender == g,
                            onClick = { gender = g },
                            label = { Text(g) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Oficiálne Identifikátory:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = chip,
                    onValueChange = { chip = it },
                    label = { Text("Číslo mikročipu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = passport,
                    onValueChange = { passport = it },
                    label = { Text("Číslo Pet Pasu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ošetrujúci Veterinár:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = vetName,
                    onValueChange = { vetName = it },
                    label = { Text("Meno veterinára") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = vetClinic,
                    onValueChange = { vetClinic = it },
                    label = { Text("Klinika / Ambulancia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = vetPhone,
                    onValueChange = { vetPhone = it },
                    label = { Text("Telefón veterinára") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = vetEmail,
                    onValueChange = { vetEmail = it },
                    label = { Text("E-mail veterinára (pre export)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Pôvod & Adopcia:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = adoptedFrom,
                    onValueChange = { adoptedFrom = it },
                    label = { Text("Odkiaľ adoptovaný / Pôvod") },
                    placeholder = { Text("napr. Útulok Sloboda zvierat, Chovná stanica, Ulica") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_pet_adopted_from_input")
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = adoptedBy,
                    onValueChange = { adoptedBy = it },
                    label = { Text("Komu adoptovaný (Osvojiteľ / Majiteľ)") },
                    placeholder = { Text("napr. Vladimír M. & rodina") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_pet_adopted_by_input")
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = adoptionDate,
                    onValueChange = { adoptionDate = it },
                    label = { Text("Dátum adopcie / príchodu (DD.MM.RRRR)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Farba profilu:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorPalette.forEach { colorVal ->
                        val isSelected = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorVal }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Delete Pet Action Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDeleteRequest() }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Zmazať profil",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Zmazať zvieratko (${pet.name})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Odstráni zviera vrátane všetkých jeho záznamov a liekov",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Zrušiť")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    pet.copy(
                                        name = name.trim(),
                                        species = selectedSpecies,
                                        breed = breed.trim(),
                                        birthDate = birthDate.trim(),
                                        gender = gender,
                                        chipNumber = chip.trim(),
                                        passportNumber = passport.trim(),
                                        vetName = vetName.trim(),
                                        vetClinic = vetClinic.trim(),
                                        vetPhone = vetPhone.trim(),
                                        vetEmail = vetEmail.trim(),
                                        targetWeightKg = targetWeight.toDoubleOrNull() ?: 0.0,
                                        avatarColorHex = selectedColor,
                                        adoptedFrom = adoptedFrom.trim(),
                                        adoptedBy = adoptedBy.trim(),
                                        adoptionDate = adoptionDate.trim()
                                    )
                                )
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("submit_edit_pet_button")
                    ) {
                        Text("Uložiť Zmeny")
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmDeletePetDialog(
    petName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Zmazať zvieratko $petName?",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                text = "Naozaj chcete natrvalo vymazať zvieratko $petName? Táto akcia vymaže aj všetky jeho priradené zdravotné záznamy, očkovania, predpísané lieky, váženia a pripomienky."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_delete_pet_btn")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Áno, zmazať")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Zrušiť")
            }
        }
    )
}

@Composable
fun AddMedicalRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        type: String,
        title: String,
        date: String,
        clinic: String,
        diagnosis: String,
        cost: Double,
        notes: String,
        nextDueDate: String
    ) -> Unit
) {
    val types = listOf(
        "Očkovanie",
        "Vyšetrenie",
        "Operácia / Zákrok",
        "Rozbor moču",
        "Krvné testy",
        "RTG / Sono",
        "Odčervenie",
        "Zuby",
        "Alergia / Diagnóza",
        "Iné"
    )
    var selectedType by remember { mutableStateOf(types[0]) }
    var title by remember { mutableStateOf("") }
    val currentDateStr = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()) }
    var date by remember { mutableStateOf(currentDateStr) }
    var clinic by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var nextDueDate by remember { mutableStateOf("") }

    val titlePlaceholder = when (selectedType) {
        "Operácia / Zákrok" -> "napr. Kastrácia, Šitie rany, Odstránenie cysty"
        "Rozbor moču" -> "napr. Preventívny rozbor moču, Sediment & pH, Test na struvity"
        "Očkovanie" -> "napr. Nobivac DHPPi + L4, Purevax RCP, Besnota"
        "Krvné testy" -> "napr. Biochemický a hematologický profil"
        "RTG / Sono" -> "napr. Sono brušnej dutiny, RTG kĺbov"
        "Odčervenie" -> "napr. Milprazon, Drontal"
        "Zuby" -> "napr. Ultrazvukové čistenie zubného kameňa"
        else -> "napr. Vstupné vyšetrenie, Kontrola uší"
    }

    val diagnosisPlaceholder = when (selectedType) {
        "Operácia / Zákrok" -> "napr. Anestézia bez komplikácií, stehy vybrať o 10-12 dní, pooperačný oblek/golier"
        "Rozbor moču" -> "napr. Špecifická hustota SG 1.030, pH 6.5, leukocyty negat., bez prítomnosti kryštálov"
        "Krvné testy" -> "napr. Pečeňové a obličkové parametre v norme, ALT 85 U/L"
        else -> "napr. Nález lekára, stav a odporúčaná terapia"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nový Zdravotný Záznam",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavrieť")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Typ záznamu:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Názov úkonu / Vakcíny *") },
                    placeholder = { Text(titlePlaceholder) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("record_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Dátum úkonu") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cost,
                        onValueChange = { cost = it },
                        label = { Text("Cena (€)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = clinic,
                    onValueChange = { clinic = it },
                    label = { Text("Veterinárna klinika / Lekár") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text("Diagnóza / Výsledok vyšetrenia") },
                    placeholder = { Text(diagnosisPlaceholder) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = nextDueDate,
                    onValueChange = { nextDueDate = it },
                    label = { Text("Najbližší termín / Preočkovanie (DD.MM.RRRR)") },
                    placeholder = { Text("Automaticky vytvorí pripomienku") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Poznámky, reakcie, odporúčania") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Zrušiť") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(
                                    selectedType,
                                    title.trim(),
                                    date.trim(),
                                    clinic.trim(),
                                    diagnosis.trim(),
                                    cost.toDoubleOrNull() ?: 0.0,
                                    notes.trim(),
                                    nextDueDate.trim()
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("submit_record_button")
                    ) {
                        Text("Uložiť Záznam")
                    }
                }
            }
        }
    }
}

@Composable
fun AddMedicationDialog(
    initialDrug: VeterinaryDrug? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        dosage: String,
        frequency: String,
        form: String,
        startDate: String,
        endDate: String,
        totalDoses: Int,
        timingNotes: String,
        prescribingVet: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialDrug?.name ?: "") }
    var dosage by remember { mutableStateOf(initialDrug?.dosageGuideline ?: "1 tableta") }
    var frequency by remember { mutableStateOf(if (initialDrug != null) "Podľa potreby / letáku" else "2x denne (ráno a večer)") }
    val forms = listOf("Tabletka", "Žuvacia tableta", "Sirup / Kvapky", "Injekcia", "Masť / Gél", "Spot-on pipeta", "Kĺbová výživa / Doplnok")
    var selectedForm by remember {
        mutableStateOf(
            if (initialDrug != null && forms.contains(initialDrug.form)) initialDrug.form else forms[0]
        )
    }

    val currentDateStr = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()) }
    var startDate by remember { mutableStateOf(currentDateStr) }
    var endDate by remember { mutableStateOf(if (initialDrug?.prescriptionRequired == true) "1 mesiac" else "Dlhodobo") }
    var totalDoses by remember { mutableStateOf(if (initialDrug != null) "10" else "20") }
    var timingNotes by remember { mutableStateOf(initialDrug?.administrationMethod ?: "Po kŕmení") }
    var prescribingVet by remember { mutableStateOf("") }

    val quickDrugs = remember {
        listOf("Bravecto", "NexGard", "Apoquel", "Simparica", "Milpro", "Clavaseptin", "Meloxidyl", "Vetmedin", "Surolan", "Alaptid")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pridať Liek & Dávkovanie",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavrieť")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick database suggestions
                Text(
                    text = "⚡ Rýchly výber z databázy:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickDrugs.forEach { drugName ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable {
                                    val matchedDrug = VeterinaryDrugDatabase.drugs.firstOrNull { it.name.contains(drugName, ignoreCase = true) }
                                    if (matchedDrug != null) {
                                        name = matchedDrug.name
                                        selectedForm = if (forms.contains(matchedDrug.form)) matchedDrug.form else forms[0]
                                        dosage = matchedDrug.dosageGuideline
                                        timingNotes = matchedDrug.administrationMethod
                                    } else {
                                        name = drugName
                                    }
                                }
                        ) {
                            Text(
                                text = "+ $drugName",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Názov lieku *") },
                    placeholder = { Text("napr. Apoquel 16mg, Amoksiklav, Bravecto") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("medication_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Forma lieku:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    forms.forEach { f ->
                        FilterChip(
                            selected = selectedForm == f,
                            onClick = { selectedForm = f },
                            label = { Text(f) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dávka (množstvo)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalDoses,
                        onValueChange = { totalDoses = it },
                        label = { Text("Počet dávok") },
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text("Frekvencia podávania") },
                    placeholder = { Text("napr. 2x denne, Každých 12 hodín") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Začiatok") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Koniec / Trvanie") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = timingNotes,
                    onValueChange = { timingNotes = it },
                    label = { Text("Pokyny k podávaniu") },
                    placeholder = { Text("napr. Nalačno, S jedlom, Zmiešať s vodou") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = prescribingVet,
                    onValueChange = { prescribingVet = it },
                    label = { Text("Predpísal veterinár") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Zrušiť") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    name.trim(),
                                    dosage.trim(),
                                    frequency.trim(),
                                    selectedForm,
                                    startDate.trim(),
                                    endDate.trim(),
                                    totalDoses.toIntOrNull() ?: 20,
                                    timingNotes.trim(),
                                    prescribingVet.trim()
                                )
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("submit_medication_button")
                    ) {
                        Text("Uložiť Liek")
                    }
                }
            }
        }
    }
}

@Composable
fun AddWeightDialog(
    onDismiss: () -> Unit,
    onConfirm: (weight: Double, date: String, notes: String) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    val currentDateStr = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()) }
    var date by remember { mutableStateOf(currentDateStr) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zaznamenať Hmotnosť", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Hmotnosť v kg *") },
                    placeholder = { Text("napr. 31.4") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_input_field")
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Dátum váženia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Poznámka (napr. Po rannom kŕmení)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weight.toDoubleOrNull()
                    if (w != null && w > 0) {
                        onConfirm(w, date.trim(), notes.trim())
                    }
                },
                enabled = weight.toDoubleOrNull() != null,
                modifier = Modifier.testTag("submit_weight_button")
            ) {
                Text("Uložiť")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Zrušiť") }
        }
    )
}

@Composable
fun AddPhotoDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, date: String, description: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    val categories = listOf("Hojenie rany", "Zuby / Ústna dutina", "Kožný nález", "Lekárska správa / Recept", "Portrét")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    val currentDateStr = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()) }
    var date by remember { mutableStateOf(currentDateStr) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pridať Zdravotnú Fotografiu", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Názov / Predmet foto *") },
                    placeholder = { Text("napr. Kontrola šitia, Stav ucha po kvapkách") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Kategória dokumentácie:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Dátum") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Popis stavu, príznaky, poznámky") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), selectedCategory, date.trim(), description.trim())
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Pridať do Galérie")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Zrušiť") }
        }
    )
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, type: String, dueDate: String, dueTime: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    val types = listOf("Dávka lieku", "Očkovanie", "Odčervenie", "Kontrola u veta", "Strihanie pazúrikov / Hygiena")
    var selectedType by remember { mutableStateOf(types[0]) }
    val currentDateStr = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()) }
    var dueDate by remember { mutableStateOf(currentDateStr) }
    var dueTime by remember { mutableStateOf("08:00") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nová Pripomienka", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Čo pripomenúť? *") },
                    placeholder = { Text("napr. Ranná tabletka Apoquel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Dátum") },
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Čas") },
                        modifier = Modifier.weight(0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Poznámka") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), selectedType, dueDate.trim(), dueTime.trim(), notes.trim())
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Uložiť Pripomienku")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Zrušiť") }
        }
    )
}

@Composable
fun ExportVetDialog(
    pet: Pet,
    medications: List<Medication>,
    records: List<MedicalRecord>,
    weightLogs: List<WeightLog>,
    onDismiss: () -> Unit,
    onSendEmail: (customNote: String) -> Unit
) {
    var customNote by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export Záznamu Veterinárovi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavrieť")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Aplikácia pripraví kompletnú zdravotnú správu pre pacienta ${pet.name} (${pet.species} - ${pet.breed}) vrátane zoznamu liekov, dávkovaní, očkovaní, nálezov a váhového trendu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val recipient = if (pet.vetEmail.isNotBlank()) pet.vetEmail else "E-mail veterinára (doplní sa v e-maily)"
                        Text(
                            text = "Príjemca: $recipient",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Obsah exportu: ${medications.size} liekov, ${records.size} záznamov/očkovaní, ${weightLogs.size} vážení.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = customNote,
                    onValueChange = { customNote = it },
                    label = { Text("Sprievodná správa / Otázka pre lekára") },
                    placeholder = { Text("napr. Posielam aktuálny zdravotný záznam pred plánovanou kontrolou...") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_note_field")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Zrušiť") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onSendEmail(customNote)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("confirm_export_email_button")
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Odoslať do E-mailu")
                    }
                }
            }
        }
    }
}

@Composable
fun AddForumPostDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, author: String, tag: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    val tags = listOf("Liečba & Lieky", "Výživa", "Očkovanie", "Správanie", "Všeobecné")
    var selectedTag by remember { mutableStateOf(tags[0]) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nový Príspevok do Fóra",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavrieť")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Téma / Otázka *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { t ->
                        FilterChip(
                            selected = selectedTag == t,
                            onClick = { selectedTag = t },
                            label = { Text(t) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Vaše meno / Prezývka") },
                    placeholder = { Text("napr. Peter & Rocky") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Text príspevku / Skúsenosť *") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Zrušiť") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onConfirm(title.trim(), content.trim(), author.trim(), selectedTag)
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank()
                    ) {
                        Text("Zverejniť")
                    }
                }
            }
        }
    }
}

@Composable
fun AboutAppDialog(
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = "PetMed Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "PetMed Zdravotný Denník",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Verzia 1.0.0",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Aplikácia pre komplexnú starostlivosť, evidenciu zdravia, dávkovanie liekov, vyšetrenia, operácie a rozbory pre domácich miláčikov.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Autor & Vývoj",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Wlademz Mlck & Wiki Pup",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                            data = android.net.Uri.parse("mailto:vladoacidoza@gmail.com")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore if no mail client
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "vladoacidoza@gmail.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zavrieť")
                }
            }
        }
    }
}
