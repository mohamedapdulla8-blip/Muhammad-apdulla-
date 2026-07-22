package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AgeGroup(val code: String, val titleAr: String, val ageRangeAr: String, val iconName: String) {
    ALL("all", "جميع الأعمار", "نصائح عامة", "FamilyRestroom"),
    CHILDREN("children", "الأطفال", "٠ - ١٢ سنة", "ChildCare"),
    TEENS("teens", "الشباب واليافعون", "١٣ - ٢٥ سنة", "SportsBasketball"),
    ADULTS("adults", "البالغون", "٢٦ - ٥٩ سنة", "Work"),
    SENIORS("seniors", "كبار السن", "٦٠+ سنة", "Elderly")
}

enum class TopicCategory(val code: String, val titleAr: String) {
    THERAPEUTIC("therapeutic", "التوعية العلاجية والدواء"),
    NUTRITION("nutrition", "التغذية والوقاية"),
    FIRST_AID("first_aid", "الإسعافات الأولية"),
    MYTHS("myths", "حقائق وأساطير صحية"),
    LIFESTYLE("lifestyle", "أنماط الحياة الصحية")
}

@Entity(tableName = "bookmarks")
data class BookmarkItem(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val content: String,
    val ageGroupCode: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "medications")
data class MedicationItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,
    val timeOfDay: String, // e.g., "08:00 ص"
    val instructions: String, // e.g., "بعد الأكل"
    val isTakenToday: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val notificationEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey val dateString: String, // YYYY-MM-DD
    val consumedMl: Int = 0,
    val targetMl: Int = 2500
)

@Entity(tableName = "patient_records")
data class PatientRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientName: String,
    val age: Int,
    val conditionTitle: String,
    val status: String, // e.g. "مستقرة", "تحتاج متابعة", "ملاحظة دقيقة"
    val latestBloodPressure: String = "",
    val latestBloodSugar: String = "",
    val latestTemperature: String = "",
    val doctorNotes: String = "",
    val nextAppointment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "patient_logs")
data class PatientLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val dateStr: String,
    val note: String,
    val bp: String = "",
    val sugar: String = "",
    val temp: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_notes")
data class UserNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "عام",
    val isPinned: Boolean = false,
    val dateStr: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class MythFact(
    val id: String,
    val myth: String,
    val fact: String,
    val explanation: String,
    val category: String
)

data class FirstAidGuide(
    val id: String,
    val title: String,
    val iconName: String,
    val quickSummary: String,
    val steps: List<String>,
    val doNots: List<String>
)

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class HealthTopic(
    val id: String,
    val title: String,
    val category: TopicCategory,
    val ageGroup: AgeGroup,
    val summary: String,
    val detailedContent: String,
    val bulletPoints: List<String>,
    val quickTip: String,
    val isBookmarked: Boolean = false
)
