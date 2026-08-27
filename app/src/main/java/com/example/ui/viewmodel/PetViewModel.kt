package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ForumComment
import com.example.data.local.entity.ForumPost
import com.example.data.local.entity.MedicalRecord
import com.example.data.local.entity.Medication
import com.example.data.local.entity.Pet
import com.example.data.local.entity.PetPhoto
import com.example.data.local.entity.Reminder
import com.example.data.local.entity.VetClinic
import com.example.data.local.entity.WeightLog
import com.example.data.repository.PetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PetRepository

    val allPets: StateFlow<List<Pet>>
    private val _selectedPetId = MutableStateFlow<Long?>(null)
    val selectedPetId: StateFlow<Long?> = _selectedPetId.asStateFlow()

    val selectedPet: StateFlow<Pet?>
    val medicalRecords: StateFlow<List<MedicalRecord>>
    val medications: StateFlow<List<Medication>>
    val weightLogs: StateFlow<List<WeightLog>>
    val reminders: StateFlow<List<Reminder>>
    val photos: StateFlow<List<PetPhoto>>
    val vetClinics: StateFlow<List<VetClinic>>
    val forumPosts: StateFlow<List<ForumPost>>

    private val _selectedPostId = MutableStateFlow<Long?>(null)
    val selectedPostId: StateFlow<Long?> = _selectedPostId.asStateFlow()
    val postComments: StateFlow<List<ForumComment>>

    // Search and filter states
    val clinicSearchQuery = MutableStateFlow("")
    val onlyEmergencyFilter = MutableStateFlow(false)
    val forumTagFilter = MutableStateFlow("Všetky")

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = PetRepository(db)

        allPets = repository.allPets.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            allPets.collect { pets ->
                if (_selectedPetId.value == null && pets.isNotEmpty()) {
                    _selectedPetId.value = pets.first().id
                }
            }
        }

        selectedPet = _selectedPetId.flatMapLatest { petId ->
            if (petId != null) repository.getPetById(petId) else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        medicalRecords = _selectedPetId.flatMapLatest { petId ->
            if (petId != null) repository.getRecordsForPet(petId) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        medications = _selectedPetId.flatMapLatest { petId ->
            if (petId != null) repository.getMedicationsForPet(petId) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        weightLogs = _selectedPetId.flatMapLatest { petId ->
            if (petId != null) repository.getWeightLogsForPet(petId) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        reminders = _selectedPetId.flatMapLatest { petId ->
            if (petId != null) repository.getRemindersForPet(petId) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        photos = _selectedPetId.flatMapLatest { petId ->
            if (petId != null) repository.getPhotosForPet(petId) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        vetClinics = repository.allClinics.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        forumPosts = repository.allPosts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        postComments = _selectedPostId.flatMapLatest { postId ->
            if (postId != null) repository.getCommentsForPost(postId) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun selectPet(petId: Long) {
        _selectedPetId.value = petId
    }

    fun selectPost(postId: Long?) {
        _selectedPostId.value = postId
    }

    // PET OPERATIONS
    fun addPet(
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
        colorHex: Long = 0xFF0D9488,
        adoptedFrom: String = "",
        adoptedBy: String = "",
        adoptionDate: String = "",
        imageUri: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = repository.insertPet(
                Pet(
                    name = name,
                    species = species,
                    breed = breed,
                    birthDate = birthDate,
                    gender = gender,
                    chipNumber = chip,
                    passportNumber = passport,
                    vetName = vetName,
                    vetClinic = vetClinic,
                    vetPhone = vetPhone,
                    vetEmail = vetEmail,
                    targetWeightKg = targetWeight,
                    avatarColorHex = colorHex,
                    adoptedFrom = adoptedFrom,
                    adoptedBy = adoptedBy,
                    adoptionDate = adoptionDate,
                    imageUri = imageUri
                )
            )
            _selectedPetId.value = newId
        }
    }

    fun updatePetPhoto(pet: Pet, imageUri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePet(pet.copy(imageUri = imageUri))
        }
    }

    fun updatePet(pet: Pet) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePet(pet)
        }
    }

    fun deletePet(pet: Pet) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePet(pet)
            val remaining = allPets.value.filter { it.id != pet.id }
            _selectedPetId.value = remaining.firstOrNull()?.id
        }
    }

    // MEDICAL RECORD OPERATIONS
    fun addMedicalRecord(
        type: String,
        title: String,
        date: String,
        clinic: String,
        diagnosis: String,
        cost: Double,
        notes: String,
        nextDueDate: String
    ) {
        val currentPetId = _selectedPetId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRecord(
                MedicalRecord(
                    petId = currentPetId,
                    type = type,
                    title = title,
                    date = date,
                    vetClinic = clinic,
                    diagnosis = diagnosis,
                    costEur = cost,
                    notes = notes,
                    nextDueDate = nextDueDate
                )
            )
            // If next due date is provided, create automatic reminder
            if (nextDueDate.isNotBlank()) {
                repository.insertReminder(
                    Reminder(
                        petId = currentPetId,
                        title = "Termín: $title",
                        type = if (type.contains("Očkovanie")) "Očkovanie" else if (type.contains("Odčervenie")) "Odčervenie" else "Kontrola u veta",
                        dueDate = nextDueDate,
                        dueTime = "09:00",
                        notes = "Kontrolná návšteva / preočkovanie"
                    )
                )
            }
        }
    }

    fun deleteMedicalRecord(record: MedicalRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecord(record)
        }
    }

    // MEDICATION OPERATIONS
    fun addMedication(
        name: String,
        dosage: String,
        frequency: String,
        form: String,
        startDate: String,
        endDate: String,
        totalDoses: Int,
        timingNotes: String,
        prescribingVet: String
    ) {
        val currentPetId = _selectedPetId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMedication(
                Medication(
                    petId = currentPetId,
                    name = name,
                    dosage = dosage,
                    frequency = frequency,
                    form = form,
                    startDate = startDate,
                    endDate = endDate,
                    isActive = true,
                    totalDoses = totalDoses,
                    takenDoses = 0,
                    timingNotes = timingNotes,
                    prescribingVet = prescribingVet
                )
            )
            // Also create a dosage reminder
            repository.insertReminder(
                Reminder(
                    petId = currentPetId,
                    title = "Užiť $name ($dosage)",
                    type = "Dávka lieku",
                    dueDate = "Dnes",
                    dueTime = "08:00",
                    notes = "$frequency - $timingNotes"
                )
            )
        }
    }

    fun markDoseTaken(medication: Medication) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedTaken = medication.takenDoses + 1
            val updatedActive = if (medication.totalDoses > 0 && updatedTaken >= medication.totalDoses && medication.endDate != "Dlhodobo") false else medication.isActive
            repository.updateMedication(
                medication.copy(
                    takenDoses = updatedTaken,
                    isActive = updatedActive
                )
            )
        }
    }

    fun toggleMedicationActive(medication: Medication) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMedication(
                medication.copy(isActive = !medication.isActive)
            )
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMedication(medication)
        }
    }

    // WEIGHT LOG OPERATIONS
    fun addWeightLog(weight: Double, date: String, notes: String) {
        val currentPetId = _selectedPetId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertWeightLog(
                WeightLog(
                    petId = currentPetId,
                    date = date,
                    timestamp = System.currentTimeMillis(),
                    weightKg = weight,
                    notes = notes
                )
            )
        }
    }

    fun deleteWeightLog(weightLog: WeightLog) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWeightLog(weightLog)
        }
    }

    // REMINDER OPERATIONS
    fun addReminder(title: String, type: String, dueDate: String, dueTime: String, notes: String) {
        val currentPetId = _selectedPetId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertReminder(
                Reminder(
                    petId = currentPetId,
                    title = title,
                    type = type,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    isCompleted = false,
                    notes = notes
                )
            )
        }
    }

    fun toggleReminderCompleted(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateReminder(
                reminder.copy(isCompleted = !reminder.isCompleted)
            )
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReminder(reminder)
        }
    }

    // PHOTO OPERATIONS
    fun addPhoto(title: String, category: String, date: String, description: String) {
        val currentPetId = _selectedPetId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPhoto(
                PetPhoto(
                    petId = currentPetId,
                    title = title,
                    category = category,
                    date = date,
                    description = description,
                    drawableResName = "pet_med_icon_1787650018098"
                )
            )
        }
    }

    fun deletePhoto(photo: PetPhoto) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePhoto(photo)
        }
    }

    // FORUM OPERATIONS
    fun addForumPost(title: String, content: String, author: String, tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPost(
                ForumPost(
                    authorName = if (author.isNotBlank()) author else "Majiteľ zvieratka",
                    petTypeTag = tag,
                    title = title,
                    content = content,
                    dateStr = "Práve teraz",
                    likesCount = 0,
                    commentsCount = 0
                )
            )
        }
    }

    fun toggleLikePost(post: ForumPost) {
        viewModelScope.launch(Dispatchers.IO) {
            val isLiked = !post.isLikedByUser
            val newCount = if (isLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
            repository.updatePost(
                post.copy(
                    isLikedByUser = isLiked,
                    likesCount = newCount
                )
            )
        }
    }

    fun addForumComment(postId: Long, content: String, author: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertComment(
                ForumComment(
                    postId = postId,
                    authorName = if (author.isNotBlank()) author else "Ja",
                    content = content,
                    dateStr = "Práve teraz"
                )
            )
            // Update comments count on post
            val post = forumPosts.value.find { it.id == postId }
            if (post != null) {
                repository.updatePost(post.copy(commentsCount = post.commentsCount + 1))
            }
        }
    }

    // EXPORT MEDICAL RECORD FOR VET VIA EMAIL
    fun exportMedicalRecordToVet(context: Context, customNote: String = "") {
        val pet = selectedPet.value ?: return
        val records = medicalRecords.value
        val meds = medications.value
        val weights = weightLogs.value

        val latestWeight = weights.lastOrNull()?.weightKg?.toString() ?: "Neuvedená"

        val reportBuilder = StringBuilder()
        reportBuilder.append("=========================================\n")
        reportBuilder.append("🐾 ZDRAVOTNÝ ZÁZNAM ZVIERATKA - PetMed\n")
        reportBuilder.append("=========================================\n\n")

        val chipStr = if (pet.chipNumber.isNotBlank()) pet.chipNumber else "Neuvedené"
        val passportStr = if (pet.passportNumber.isNotBlank()) pet.passportNumber else "Neuvedené"

        reportBuilder.append("📌 ZÁKLADNÉ ÚDAJE O PACIENTOVI:\n")
        reportBuilder.append("• Meno: ${pet.name}\n")
        reportBuilder.append("• Druh / Plemeno: ${pet.species} - ${pet.breed}\n")
        reportBuilder.append("• Dátum narodenia: ${pet.birthDate}\n")
        reportBuilder.append("• Pohlavie: ${pet.gender}\n")
        if (pet.adoptedFrom.isNotBlank()) {
            reportBuilder.append("• Pôvod / Odkiaľ adoptovaný: ${pet.adoptedFrom}\n")
        }
        if (pet.adoptedBy.isNotBlank()) {
            val adoptDateSuffix = if (pet.adoptionDate.isNotBlank()) " (Dátum adopcie: ${pet.adoptionDate})" else ""
            reportBuilder.append("• Komu adoptovaný / Osvojiteľ: ${pet.adoptedBy}$adoptDateSuffix\n")
        }
        reportBuilder.append("• Číslo čipu: $chipStr\n")
        reportBuilder.append("• Číslo pasu: $passportStr\n")
        reportBuilder.append("• Aktuálna hmotnosť: $latestWeight kg (Cieľová: ${pet.targetWeightKg} kg)\n")
        reportBuilder.append("• Ošetrujúci veterinár: ${pet.vetName} (${pet.vetClinic})\n\n")

        if (customNote.isNotBlank()) {
            reportBuilder.append("💬 POZNÁMKA MAJITEĽA:\n$customNote\n\n")
        }

        reportBuilder.append("💊 AKTUÁLNE A PREDCHÁDZAJÚCE LIEKY:\n")
        if (meds.isEmpty()) {
            reportBuilder.append("Žiadne evidované lieky.\n")
        } else {
            meds.forEach { med ->
                val status = if (med.isActive) "AKTÍVNY" else "DOUŽÍVANÝ"
                reportBuilder.append("• [${status}] ${med.name} (${med.form})\n")
                reportBuilder.append("  - Dávkovanie: ${med.dosage}, ${med.frequency}\n")
                reportBuilder.append("  - Trvanie: ${med.startDate} až ${med.endDate}\n")
                reportBuilder.append("  - Pokyny: ${med.timingNotes}\n")
                reportBuilder.append("  - Predpísal: ${med.prescribingVet}\n")
            }
        }
        reportBuilder.append("\n")

        reportBuilder.append("📋 HISTÓRIA VYŠETRENÍ A OČKOVANÍ:\n")
        if (records.isEmpty()) {
            reportBuilder.append("Žiadne zaznamenané vyšetrenia.\n")
        } else {
            records.forEach { rec ->
                reportBuilder.append("• [${rec.date}] ${rec.type.uppercase()}: ${rec.title}\n")
                if (rec.diagnosis.isNotBlank()) reportBuilder.append("  - Nález/Diagnóza: ${rec.diagnosis}\n")
                if (rec.notes.isNotBlank()) reportBuilder.append("  - Poznámka: ${rec.notes}\n")
                if (rec.nextDueDate.isNotBlank()) reportBuilder.append("  - Najbližší termín: ${rec.nextDueDate}\n")
            }
        }
        reportBuilder.append("\n")

        reportBuilder.append("⚖️ HISTÓRIA VÁŽENIA:\n")
        if (weights.isEmpty()) {
            reportBuilder.append("Žiadne záznamy váženia.\n")
        } else {
            weights.takeLast(5).forEach { w ->
                reportBuilder.append("• ${w.date}: ${w.weightKg} kg (${w.notes})\n")
            }
        }

        reportBuilder.append("\n-----------------------------------------\n")
        reportBuilder.append("Vygenerované aplikáciou PetMed Zdravotný Denník\n")
        reportBuilder.append("Vytvoril: Wlademz Mlck & Wiki Pup (vladoacidoza@gmail.com)\n")

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            if (pet.vetEmail.isNotBlank()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(pet.vetEmail))
            }
            putExtra(Intent.EXTRA_SUBJECT, "Zdravotný záznam: ${pet.name} (${pet.species} - ${pet.breed})")
            putExtra(Intent.EXTRA_TEXT, reportBuilder.toString())
        }

        try {
            val chooser = Intent.createChooser(emailIntent, "Odoslať zdravotný záznam veterinárovi")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Nepodarilo sa otvoriť e-mailového klienta.", Toast.LENGTH_SHORT).show()
        }
    }
}
