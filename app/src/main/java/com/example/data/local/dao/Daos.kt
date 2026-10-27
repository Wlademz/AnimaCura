package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface PetDao {
    @Query("SELECT * FROM pets ORDER BY id ASC")
    fun getAllPets(): Flow<List<Pet>>

    @Query("SELECT * FROM pets WHERE id = :id LIMIT 1")
    fun getPetById(id: Long): Flow<Pet?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: Pet): Long

    @Update
    suspend fun updatePet(pet: Pet)

    @Delete
    suspend fun deletePet(pet: Pet)
}

@Dao
interface MedicalRecordDao {
    @Query("SELECT * FROM medical_records WHERE petId = :petId ORDER BY id DESC")
    fun getRecordsForPet(petId: Long): Flow<List<MedicalRecord>>

    @Query("SELECT * FROM medical_records ORDER BY id DESC")
    fun getAllRecords(): Flow<List<MedicalRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MedicalRecord): Long

    @Update
    suspend fun updateRecord(record: MedicalRecord)

    @Delete
    suspend fun deleteRecord(record: MedicalRecord)

    @Query("DELETE FROM medical_records WHERE petId = :petId")
    suspend fun deleteRecordsForPet(petId: Long)
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE petId = :petId ORDER BY isActive DESC, id DESC")
    fun getMedicationsForPet(petId: Long): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY id DESC")
    fun getAllActiveMedications(): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    @Query("DELETE FROM medications WHERE petId = :petId")
    suspend fun deleteMedicationsForPet(petId: Long)
}

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_logs WHERE petId = :petId ORDER BY timestamp ASC")
    fun getWeightLogsForPet(petId: Long): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weightLog: WeightLog): Long

    @Delete
    suspend fun deleteWeightLog(weightLog: WeightLog)

    @Query("DELETE FROM weight_logs WHERE petId = :petId")
    suspend fun deleteWeightLogsForPet(petId: Long)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE petId = :petId ORDER BY isCompleted ASC, id DESC")
    fun getRemindersForPet(petId: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, id DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE petId = :petId")
    suspend fun deleteRemindersForPet(petId: Long)
}

@Dao
interface PetPhotoDao {
    @Query("SELECT * FROM pet_photos WHERE petId = :petId ORDER BY id DESC")
    fun getPhotosForPet(petId: Long): Flow<List<PetPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PetPhoto): Long

    @Delete
    suspend fun deletePhoto(photo: PetPhoto)

    @Query("DELETE FROM pet_photos WHERE petId = :petId")
    suspend fun deletePhotosForPet(petId: Long)
}

@Dao
interface VetClinicDao {
    @Query("SELECT * FROM vet_clinics ORDER BY is24hEmergency DESC, rating DESC")
    fun getAllClinics(): Flow<List<VetClinic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinic(clinic: VetClinic): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinics(clinics: List<VetClinic>)
}

@Dao
interface ForumDao {
    @Query("SELECT * FROM forum_posts ORDER BY id DESC")
    fun getAllPosts(): Flow<List<ForumPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: ForumPost): Long

    @Update
    suspend fun updatePost(post: ForumPost)

    @Query("SELECT * FROM forum_comments WHERE postId = :postId ORDER BY id ASC")
    fun getCommentsForPost(postId: Long): Flow<List<ForumComment>>

    @Query("SELECT * FROM forum_comments ORDER BY id ASC")
    fun getAllComments(): Flow<List<ForumComment>>

    @Query("SELECT COUNT(*) FROM forum_posts")
    suspend fun getPostsCount(): Int

    @Query("SELECT COUNT(*) FROM forum_comments")
    suspend fun getCommentsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: ForumComment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<ForumPost>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<ForumComment>)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE petId = :petId ORDER BY id DESC")
    fun getExpensesForPet(petId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE petId = :petId")
    suspend fun deleteExpensesForPet(petId: Long)
}
