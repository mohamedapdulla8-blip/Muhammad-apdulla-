package com.example.data.db

import androidx.room.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkItem)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    suspend fun isBookmarked(id: String): Boolean

    // Medications
    @Query("SELECT * FROM medications ORDER BY id DESC")
    fun getAllMedications(): Flow<List<MedicationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationItem): Long

    @Query("UPDATE medications SET isTakenToday = :isTaken WHERE id = :id")
    suspend fun updateMedicationTakenStatus(id: Long, isTaken: Boolean)

    @Query("UPDATE medications SET notificationEnabled = :enabled WHERE id = :id")
    suspend fun updateMedicationNotificationEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE medications SET reminderHour = :hour, reminderMinute = :minute, timeOfDay = :timeOfDay WHERE id = :id")
    suspend fun updateMedicationTime(id: Long, hour: Int, minute: Int, timeOfDay: String)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedication(id: Long)

    // Water Tracker
    @Query("SELECT * FROM water_logs WHERE dateString = :dateStr LIMIT 1")
    fun getWaterLog(dateStr: String): Flow<WaterLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWaterLog(waterLog: WaterLog)

    // Patient Records & Follow-up
    @Query("SELECT * FROM patient_records ORDER BY id DESC")
    fun getAllPatients(): Flow<List<PatientRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientRecord)

    @Query("DELETE FROM patient_records WHERE id = :id")
    suspend fun deletePatient(id: Long)

    @Query("SELECT * FROM patient_logs WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getPatientLogs(patientId: Long): Flow<List<PatientLogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatientLog(log: PatientLogEntry)

    // User Notes & Messages
    @Query("SELECT * FROM user_notes ORDER BY isPinned DESC, timestamp DESC")
    fun getAllUserNotes(): Flow<List<UserNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserNote(note: UserNote)

    @Query("DELETE FROM user_notes WHERE id = :id")
    suspend fun deleteUserNote(id: Long)

    @Query("UPDATE user_notes SET isPinned = NOT isPinned WHERE id = :id")
    suspend fun toggleUserNotePinned(id: Long)
}
