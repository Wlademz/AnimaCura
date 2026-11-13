package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.Expense
import com.example.data.local.entity.ForumComment
import com.example.data.local.entity.ForumPost
import com.example.data.local.entity.MedicalRecord
import com.example.data.local.entity.Medication
import com.example.data.local.entity.Pet
import com.example.data.local.entity.PetPhoto
import com.example.data.local.entity.Reminder
import com.example.data.local.entity.VetClinic
import com.example.data.local.entity.WeightLog
import kotlinx.coroutines.flow.Flow

class PetRepository(private val database: AppDatabase) {
    // Pets
    val allPets: Flow<List<Pet>> = database.petDao().getAllPets()
    fun getPetById(id: Long): Flow<Pet?> = database.petDao().getPetById(id)
    suspend fun insertPet(pet: Pet): Long = database.petDao().insertPet(pet)
    suspend fun updatePet(pet: Pet) = database.petDao().updatePet(pet)
    suspend fun deletePet(pet: Pet) {
        database.medicalRecordDao().deleteRecordsForPet(pet.id)
        database.medicationDao().deleteMedicationsForPet(pet.id)
        database.weightLogDao().deleteWeightLogsForPet(pet.id)
        database.reminderDao().deleteRemindersForPet(pet.id)
        database.petPhotoDao().deletePhotosForPet(pet.id)
        database.expenseDao().deleteExpensesForPet(pet.id)
        database.petDao().deletePet(pet)
    }

    // Medical Records
    fun getRecordsForPet(petId: Long): Flow<List<MedicalRecord>> = database.medicalRecordDao().getRecordsForPet(petId)
    val allMedicalRecords: Flow<List<MedicalRecord>> = database.medicalRecordDao().getAllRecords()
    suspend fun insertRecord(record: MedicalRecord): Long = database.medicalRecordDao().insertRecord(record)
    suspend fun updateRecord(record: MedicalRecord) = database.medicalRecordDao().updateRecord(record)
    suspend fun deleteRecord(record: MedicalRecord) = database.medicalRecordDao().deleteRecord(record)

    // Medications
    fun getMedicationsForPet(petId: Long): Flow<List<Medication>> = database.medicationDao().getMedicationsForPet(petId)
    val allActiveMedications: Flow<List<Medication>> = database.medicationDao().getAllActiveMedications()
    suspend fun insertMedication(medication: Medication): Long = database.medicationDao().insertMedication(medication)
    suspend fun updateMedication(medication: Medication) = database.medicationDao().updateMedication(medication)
    suspend fun deleteMedication(medication: Medication) = database.medicationDao().deleteMedication(medication)

    // Weight Logs
    fun getWeightLogsForPet(petId: Long): Flow<List<WeightLog>> = database.weightLogDao().getWeightLogsForPet(petId)
    suspend fun insertWeightLog(weightLog: WeightLog): Long = database.weightLogDao().insertWeightLog(weightLog)
    suspend fun updateWeightLog(weightLog: WeightLog) = database.weightLogDao().updateWeightLog(weightLog)
    suspend fun deleteWeightLog(weightLog: WeightLog) = database.weightLogDao().deleteWeightLog(weightLog)

    // Reminders
    fun getRemindersForPet(petId: Long): Flow<List<Reminder>> = database.reminderDao().getRemindersForPet(petId)
    val allReminders: Flow<List<Reminder>> = database.reminderDao().getAllReminders()
    suspend fun insertReminder(reminder: Reminder): Long = database.reminderDao().insertReminder(reminder)
    suspend fun updateReminder(reminder: Reminder) = database.reminderDao().updateReminder(reminder)
    suspend fun deleteReminder(reminder: Reminder) = database.reminderDao().deleteReminder(reminder)

    // Photos
    fun getPhotosForPet(petId: Long): Flow<List<PetPhoto>> = database.petPhotoDao().getPhotosForPet(petId)
    suspend fun insertPhoto(photo: PetPhoto): Long = database.petPhotoDao().insertPhoto(photo)
    suspend fun updatePhoto(photo: PetPhoto) = database.petPhotoDao().updatePhoto(photo)
    suspend fun deletePhoto(photo: PetPhoto) = database.petPhotoDao().deletePhoto(photo)

    // Vet Clinics
    val allClinics: Flow<List<VetClinic>> = database.vetClinicDao().getAllClinics()
    suspend fun insertClinic(clinic: VetClinic): Long = database.vetClinicDao().insertClinic(clinic)
    suspend fun updateClinic(clinic: VetClinic) = database.vetClinicDao().updateClinic(clinic)
    suspend fun deleteClinic(clinic: VetClinic) = database.vetClinicDao().deleteClinic(clinic)

    // Forum
    val allPosts: Flow<List<ForumPost>> = database.forumDao().getAllPosts()
    val allComments: Flow<List<ForumComment>> = database.forumDao().getAllComments()
    fun getCommentsForPost(postId: Long): Flow<List<ForumComment>> = database.forumDao().getCommentsForPost(postId)
    suspend fun insertPost(post: ForumPost): Long = database.forumDao().insertPost(post)
    suspend fun updatePost(post: ForumPost) = database.forumDao().updatePost(post)
    suspend fun insertComment(comment: ForumComment): Long = database.forumDao().insertComment(comment)
    suspend fun refreshForumDiscussions() = AppDatabase.ensureForumDiscussions(database)

    // Expenses
    fun getExpensesForPet(petId: Long): Flow<List<Expense>> = database.expenseDao().getExpensesForPet(petId)
    val allExpenses: Flow<List<Expense>> = database.expenseDao().getAllExpenses()
    suspend fun insertExpense(expense: Expense): Long = database.expenseDao().insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = database.expenseDao().deleteExpense(expense)
}
