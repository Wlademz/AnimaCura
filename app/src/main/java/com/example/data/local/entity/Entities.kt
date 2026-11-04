package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val species: String, // Pes, Mačka, Králik, Hlodavec, Vták, Iné
    val breed: String,
    val birthDate: String, // e.g. "12.04.2021"
    val gender: String, // Samec, Samica, Kastrovaný
    val chipNumber: String = "",
    val passportNumber: String = "",
    val vetName: String = "",
    val vetClinic: String = "",
    val vetPhone: String = "",
    val vetEmail: String = "",
    val avatarColorHex: Long = 0xFF0D9488,
    val targetWeightKg: Double = 0.0,
    val adoptedFrom: String = "", // Odkiaľ bol adoptovaný / Pôvod (útulok, chovateľ, ulica...)
    val adoptedBy: String = "",   // Komu bol adoptovaný / Osvojiteľ / Majiteľ
    val adoptionDate: String = "", // Dátum adopcie / príchodu
    val imageUri: String = ""     // Fotografia zvieratka (cesta k súboru / URI)
)

@Entity(tableName = "medical_records")
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val type: String, // Očkovanie, Vyšetrenie, Operácia / Zákrok, Krvné testy, RTG / Sono, Odčervenie, Alergia / Diagnóza, Zuby, Iné
    val title: String,
    val date: String, // DD.MM.YYYY
    val vetClinic: String = "",
    val diagnosis: String = "",
    val costEur: Double = 0.0,
    val notes: String = "",
    val nextDueDate: String = "", // Pre vakcíny a opakované kontroly
    val photoUri: String = "",
    val attachmentUri: String = "",
    val attachmentName: String = ""
)

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val name: String,
    val dosage: String, // e.g. "1 tableta", "2.5 ml", "1 kvapka"
    val frequency: String, // e.g. "2x denne (ráno a večer)", "1x denne s jedlom", "Každých 8 hodín"
    val form: String, // Tabletka, Sirup / Kvapky, Injekcia, Masť / Gél, Spot-on pipeta, Kĺbová výživa / Doplnok
    val startDate: String, // DD.MM.YYYY
    val endDate: String = "", // DD.MM.YYYY alebo "Dlhodobo"
    val isActive: Boolean = true,
    val totalDoses: Int = 20,
    val takenDoses: Int = 0,
    val timingNotes: String = "Po jedle",
    val prescribingVet: String = ""
)

@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val date: String, // DD.MM.YYYY
    val timestamp: Long = System.currentTimeMillis(),
    val weightKg: Double,
    val notes: String = ""
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val title: String,
    val type: String, // Dávka lieku, Očkovanie, Odčervenie, Kontrola u veta, Strihanie pazúrikov / Hygiena
    val dueDate: String, // DD.MM.YYYY
    val dueTime: String = "08:00",
    val isCompleted: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "pet_photos")
data class PetPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val title: String,
    val category: String, // Hojenie rany, Zuby / Ústna dutina, Kožný nález, Lekárska správa / Recept, Portrét
    val date: String,
    val description: String = "",
    val imageUri: String = "",
    val drawableResName: String = "pet_med_icon_1787650018098"
)

@Entity(tableName = "vet_clinics")
data class VetClinic(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val city: String,
    val address: String,
    val phone: String,
    val email: String = "",
    val is24hEmergency: Boolean,
    val openingHours: String,
    val rating: Double = 4.8,
    val emergencyFeeNote: String = "Nonstop pohotovostný príjem",
    val latitude: Double = 48.1486,
    val longitude: Double = 17.1077,
    val userRating: Int = 0, // 0 = nehodnotené používateľom, 1..5 = hviezdičky
    val userReview: String = "",
    val clinicType: String = "Pohotovosť" // "Pohotovosť 24/7", "Veterinárna ambulancia", "Veterinárna klinika"
)

@Entity(tableName = "forum_posts")
data class ForumPost(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorName: String,
    val petTypeTag: String, // Pes, Mačka, Liečba & Lieky, Výživa, Všeobecné
    val title: String,
    val content: String,
    val dateStr: String,
    val likesCount: Int = 0,
    val isLikedByUser: Boolean = false,
    val commentsCount: Int = 0
)

@Entity(tableName = "forum_comments")
data class ForumComment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val postId: Long,
    val authorName: String,
    val content: String,
    val dateStr: String
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val title: String,
    val category: String, // Krmivo & Maškrty, Veterinár & Lieky, Hračky & Výbava, Salón & Hygiena, Poistenie & Poplatky, Iné
    val amountEur: Double,
    val date: String, // DD.MM.YYYY
    val notes: String = ""
)
