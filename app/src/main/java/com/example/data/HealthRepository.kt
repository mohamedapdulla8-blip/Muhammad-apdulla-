package com.example.data

import com.example.data.api.GeminiClient
import com.example.data.db.HealthDao
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class HealthRepository(private val healthDao: HealthDao) {

    val allBookmarks: Flow<List<BookmarkItem>> = healthDao.getAllBookmarks()
    val allMedications: Flow<List<MedicationItem>> = healthDao.getAllMedications()

    fun getTodayWaterLog(): Flow<WaterLog?> {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return healthDao.getWaterLog(todayStr)
    }

    suspend fun addWater(amountMl: Int) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val currentLog = healthDao.getWaterLog(todayStr)
        // Insert or update
        val existing = try {
            // Simple upsert helper
            WaterLog(dateString = todayStr, consumedMl = amountMl, targetMl = 2500)
        } catch (e: Exception) {
            WaterLog(dateString = todayStr, consumedMl = amountMl, targetMl = 2500)
        }
        healthDao.upsertWaterLog(existing)
    }

    suspend fun updateWaterConsumed(consumedMl: Int, targetMl: Int = 2500) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        healthDao.upsertWaterLog(WaterLog(dateString = todayStr, consumedMl = consumedMl, targetMl = targetMl))
    }

    suspend fun toggleBookmark(topic: HealthTopic): Boolean {
        val isCurrentlyBookmarked = healthDao.isBookmarked(topic.id)
        if (isCurrentlyBookmarked) {
            healthDao.deleteBookmark(topic.id)
            return false
        } else {
            healthDao.insertBookmark(
                BookmarkItem(
                    id = topic.id,
                    title = topic.title,
                    category = topic.category.titleAr,
                    summary = topic.summary,
                    content = topic.detailedContent,
                    ageGroupCode = topic.ageGroup.code
                )
            )
            return true
        }
    }

    suspend fun isTopicBookmarked(topicId: String): Boolean {
        return healthDao.isBookmarked(topicId)
    }

    suspend fun addMedication(
        name: String,
        dosage: String,
        timeOfDay: String,
        instructions: String,
        hour: Int = 8,
        minute: Int = 0,
        notificationEnabled: Boolean = true
    ): Long {
        return healthDao.insertMedication(
            MedicationItem(
                name = name,
                dosage = dosage,
                timeOfDay = timeOfDay,
                instructions = instructions,
                isTakenToday = false,
                reminderHour = hour,
                reminderMinute = minute,
                notificationEnabled = notificationEnabled
            )
        )
    }

    suspend fun toggleMedicationTaken(id: Long, currentStatus: Boolean) {
        healthDao.updateMedicationTakenStatus(id, !currentStatus)
    }

    suspend fun toggleMedicationNotification(id: Long, enabled: Boolean) {
        healthDao.updateMedicationNotificationEnabled(id, enabled)
    }

    suspend fun updateMedicationTime(id: Long, hour: Int, minute: Int, timeOfDay: String) {
        healthDao.updateMedicationTime(id, hour, minute, timeOfDay)
    }

    suspend fun deleteMedication(id: Long) {
        healthDao.deleteMedication(id)
    }

    val allPatients: Flow<List<PatientRecord>> = healthDao.getAllPatients()

    suspend fun addPatient(patient: PatientRecord) {
        healthDao.insertPatient(patient)
    }

    suspend fun deletePatient(id: Long) {
        healthDao.deletePatient(id)
    }

    fun getPatientLogs(patientId: Long): Flow<List<PatientLogEntry>> {
        return healthDao.getPatientLogs(patientId)
    }

    suspend fun addPatientLog(log: PatientLogEntry) {
        healthDao.insertPatientLog(log)
    }

    val allUserNotes: Flow<List<UserNote>> = healthDao.getAllUserNotes()

    suspend fun addUserNote(note: UserNote) {
        healthDao.insertUserNote(note)
    }

    suspend fun deleteUserNote(id: Long) {
        healthDao.deleteUserNote(id)
    }

    suspend fun toggleUserNotePinned(id: Long) {
        healthDao.toggleUserNotePinned(id)
    }

    suspend fun askGeminiHealthAdvisor(query: String, ageGroupAr: String): String {
        return GeminiClient.askHealthAssistant(query, ageGroupAr)
    }

    fun getTopicsForAgeGroup(ageGroup: AgeGroup): List<HealthTopic> {
        return if (ageGroup == AgeGroup.ALL) {
            StaticHealthData.HEALTH_TOPICS
        } else {
            StaticHealthData.HEALTH_TOPICS.filter { it.ageGroup == ageGroup || it.ageGroup == AgeGroup.ALL }
        }
    }

    fun searchTopics(query: String): List<HealthTopic> {
        if (query.isBlank()) return StaticHealthData.HEALTH_TOPICS
        val cleanQuery = query.trim().lowercase()
        return StaticHealthData.HEALTH_TOPICS.filter {
            it.title.lowercase().contains(cleanQuery) ||
                    it.summary.lowercase().contains(cleanQuery) ||
                    it.detailedContent.lowercase().contains(cleanQuery) ||
                    it.category.titleAr.lowercase().contains(cleanQuery)
        }
    }
}
