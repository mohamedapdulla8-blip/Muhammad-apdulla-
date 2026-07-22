package com.example.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HealthRepository
import com.example.data.StaticHealthData
import com.example.data.db.AppDatabase
import com.example.data.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HealthRepository
    init {
        val database = AppDatabase.getDatabase(application)
        repository = HealthRepository(database.healthDao())

        viewModelScope.launch {
            val currentList = repository.allPatients.firstOrNull() ?: emptyList()
            if (currentList.isEmpty()) {
                repository.addPatient(
                    PatientRecord(
                        patientName = "الوالد (عبدالله)",
                        age = 65,
                        conditionTitle = "ارتفاع ضغط الدم والسكري",
                        status = "مستقرة",
                        latestBloodPressure = "125/82",
                        latestBloodSugar = "115 mg/dL",
                        latestTemperature = "36.8 °C",
                        doctorNotes = "الالتزام بجرعات الدواء في المواعيد المحددة مع استمرار تقليل الملح.",
                        nextAppointment = "15 أغسطس 2026"
                    )
                )
                repository.addPatient(
                    PatientRecord(
                        patientName = "أحمد (الابن)",
                        age = 12,
                        conditionTitle = "حساسية الصدر (الربو الموسمي)",
                        status = "تحتاج متابعة",
                        latestBloodPressure = "110/70",
                        latestBloodSugar = "92 mg/dL",
                        latestTemperature = "37.1 °C",
                        doctorNotes = "متابعة استخدام البخاخ الوقائي مرتين يومياً وتجنب المثيرات التنفسية.",
                        nextAppointment = "28 يوليو 2026"
                    )
                )
            }

            val currentNotes = repository.allUserNotes.firstOrNull() ?: emptyList()
            if (currentNotes.isEmpty()) {
                repository.addUserNote(
                    UserNote(
                        title = "تساؤل لطبيب العائلة",
                        content = "هل يجب تعديل جرعة دواء الضغط عند ممارسة الرياضة الصباحية الخفيفة؟",
                        category = "استفسار طبي",
                        isPinned = true,
                        dateStr = "2026-07-20"
                    )
                )
                repository.addUserNote(
                    UserNote(
                        title = "ملاحظة صحية يومية",
                        content = "الشعور بتحسن ممتاز بعد الالتزام بالمشي 30 دقيقة وشرب 2.5 لتر ماء يومياً.",
                        category = "ملاحظة شخصية",
                        isPinned = false,
                        dateStr = "2026-07-21"
                    )
                )
            }
        }
    }

    // Dark Theme Toggle State
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Age Group Filter
    private val _selectedAgeGroup = MutableStateFlow(AgeGroup.ALL)
    val selectedAgeGroup: StateFlow<AgeGroup> = _selectedAgeGroup.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected Topic for Detail Dialog / Screen
    private val _selectedTopic = MutableStateFlow<HealthTopic?>(null)
    val selectedTopic: StateFlow<HealthTopic?> = _selectedTopic.asStateFlow()

    // Bookmarks Flow
    val bookmarks: StateFlow<List<BookmarkItem>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Medications Flow
    val medications: StateFlow<List<MedicationItem>> = repository.allMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today Water Log Flow
    val waterLog: StateFlow<WaterLog?> = repository.getTodayWaterLog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Daily Tip
    private val _dailyTip = MutableStateFlow(StaticHealthData.DAILY_TIPS.random())
    val dailyTip: StateFlow<String> = _dailyTip.asStateFlow()

    // BMI Calculator State
    var bmiHeightCm = MutableStateFlow("170")
    var bmiWeightKg = MutableStateFlow("70")
    private val _bmiResult = MutableStateFlow<Pair<Float, String>?>(null)
    val bmiResult: StateFlow<Pair<Float, String>?> = _bmiResult.asStateFlow()

    // Water Calculator State
    var waterWeightKg = MutableStateFlow("70")
    private val _calculatedWaterRequirement = MutableStateFlow(2400) // in ml
    val calculatedWaterRequirement: StateFlow<Int> = _calculatedWaterRequirement.asStateFlow()

    // Quiz State
    private val _quizQuestions = MutableStateFlow(StaticHealthData.QUIZ_QUESTIONS)
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    val selectedOptionIndex: StateFlow<Int?> = _selectedOptionIndex.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    // Gemini Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "AI",
                text = "مرحباً بك! أنا مساعد الثقافة الصحية والتوعية العلاجية. كيف يمكنني إيضاح مفهوم صحي أو شرح مصطلح طبي لك بطريقة سهلة اليوم؟"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun selectAgeGroup(ageGroup: AgeGroup) {
        _selectedAgeGroup.value = ageGroup
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectTopic(topic: HealthTopic?) {
        _selectedTopic.value = topic
    }

    fun refreshDailyTip() {
        _dailyTip.value = StaticHealthData.DAILY_TIPS.random()
    }

    fun toggleBookmark(topic: HealthTopic) {
        viewModelScope.launch {
            repository.toggleBookmark(topic)
        }
    }

    // BMI Calculation
    fun calculateBmi() {
        val h = bmiHeightCm.value.toFloatOrNull() ?: return
        val w = bmiWeightKg.value.toFloatOrNull() ?: return
        if (h <= 0 || w <= 0) return

        val heightInMeters = h / 100f
        val bmi = w / (heightInMeters * heightInMeters)

        val status = when {
            bmi < 18.5f -> "وزن أقل من الطبيعي (ينصح بتغذية صحية متوازنة)"
            bmi in 18.5f..24.9f -> "وزن طبيعي وصحي ممتازة!"
            bmi in 25.0f..29.9f -> "زيادة خفيفة في الوزن (ينصح بالمشي وتخفيف السكريات)"
            else -> "سمنة - ينصح بالمتابعة مع أخصائي تغذية ونشاط بدني"
        }

        _bmiResult.value = Pair(bmi, status)
    }

    // Water Calculation
    fun calculateWaterRequirement() {
        val w = waterWeightKg.value.toFloatOrNull() ?: 70f
        // Standard formula: ~35ml per kg of body weight
        val requiredMl = (w * 35).toInt().coerceAtLeast(1500)
        _calculatedWaterRequirement.value = requiredMl
    }

    fun addWaterCup(amountMl: Int = 250) {
        viewModelScope.launch {
            val currentMl = waterLog.value?.consumedMl ?: 0
            val target = waterLog.value?.targetMl ?: _calculatedWaterRequirement.value
            repository.updateWaterConsumed(currentMl + amountMl, target)
        }
    }

    fun resetTodayWater() {
        viewModelScope.launch {
            val target = waterLog.value?.targetMl ?: _calculatedWaterRequirement.value
            repository.updateWaterConsumed(0, target)
        }
    }

    // Medications
    fun addMedication(
        name: String,
        dosage: String,
        timeOfDay: String,
        instructions: String,
        hour: Int = 8,
        minute: Int = 0,
        notificationEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            val newId = repository.addMedication(
                name = name,
                dosage = dosage,
                timeOfDay = timeOfDay,
                instructions = instructions,
                hour = hour,
                minute = minute,
                notificationEnabled = notificationEnabled
            )
            if (notificationEnabled && newId > 0) {
                com.example.notifications.MedicationNotificationHelper.scheduleMedicationAlarm(
                    context = getApplication(),
                    medicationId = newId,
                    medicationName = name,
                    dosage = dosage,
                    instructions = instructions,
                    hour = hour,
                    minute = minute
                )
            }
        }
    }

    fun toggleMedicationNotification(medication: MedicationItem, newEnabledState: Boolean) {
        viewModelScope.launch {
            repository.toggleMedicationNotification(medication.id, newEnabledState)
            if (newEnabledState) {
                com.example.notifications.MedicationNotificationHelper.scheduleMedicationAlarm(
                    context = getApplication(),
                    medicationId = medication.id,
                    medicationName = medication.name,
                    dosage = medication.dosage,
                    instructions = medication.instructions,
                    hour = medication.reminderHour,
                    minute = medication.reminderMinute
                )
            } else {
                com.example.notifications.MedicationNotificationHelper.cancelMedicationAlarm(
                    context = getApplication(),
                    medicationId = medication.id
                )
            }
        }
    }

    fun updateMedicationReminderTime(medication: MedicationItem, hour: Int, minute: Int, formattedTimeStr: String) {
        viewModelScope.launch {
            repository.updateMedicationTime(medication.id, hour, minute, formattedTimeStr)
            if (medication.notificationEnabled) {
                com.example.notifications.MedicationNotificationHelper.scheduleMedicationAlarm(
                    context = getApplication(),
                    medicationId = medication.id,
                    medicationName = medication.name,
                    dosage = medication.dosage,
                    instructions = medication.instructions,
                    hour = hour,
                    minute = minute
                )
            }
        }
    }

    fun toggleMedicationTaken(id: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleMedicationTaken(id, currentStatus)
        }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch {
            repository.deleteMedication(id)
            com.example.notifications.MedicationNotificationHelper.cancelMedicationAlarm(
                context = getApplication(),
                medicationId = id
            )
        }
    }

    fun sendTestNotification(medicationName: String, dosage: String, instructions: String) {
        com.example.notifications.MedicationNotificationHelper.showImmediateNotification(
            context = getApplication(),
            medicationName = medicationName,
            dosage = dosage,
            instructions = instructions
        )
    }

    // Quiz Actions
    fun answerQuizQuestion(optionIndex: Int) {
        if (_selectedOptionIndex.value != null) return // Already answered current
        _selectedOptionIndex.value = optionIndex

        val currentQ = _quizQuestions.value[_currentQuizIndex.value]
        if (optionIndex == currentQ.correctIndex) {
            _quizScore.value += 1
        }
    }

    fun nextQuizQuestion() {
        val nextIdx = _currentQuizIndex.value + 1
        if (nextIdx < _quizQuestions.value.size) {
            _currentQuizIndex.value = nextIdx
            _selectedOptionIndex.value = null
        } else {
            _isQuizFinished.value = true
        }
    }

    fun resetQuiz() {
        _currentQuizIndex.value = 0
        _quizScore.value = 0
        _selectedOptionIndex.value = null
        _isQuizFinished.value = false
    }

    // Patient Records Flow
    val patientRecords: StateFlow<List<PatientRecord>> = repository.allPatients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPatient(
        patientName: String,
        age: Int,
        conditionTitle: String,
        status: String,
        bp: String = "",
        sugar: String = "",
        temp: String = "",
        doctorNotes: String = "",
        nextAppointment: String = ""
    ) {
        viewModelScope.launch {
            repository.addPatient(
                PatientRecord(
                    patientName = patientName,
                    age = age,
                    conditionTitle = conditionTitle,
                    status = status,
                    latestBloodPressure = bp,
                    latestBloodSugar = sugar,
                    latestTemperature = temp,
                    doctorNotes = doctorNotes,
                    nextAppointment = nextAppointment
                )
            )
        }
    }

    fun deletePatient(id: Long) {
        viewModelScope.launch {
            repository.deletePatient(id)
        }
    }

    fun addPatientLog(patientId: Long, note: String, bp: String = "", sugar: String = "", temp: String = "") {
        viewModelScope.launch {
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            repository.addPatientLog(
                PatientLogEntry(
                    patientId = patientId,
                    dateStr = dateStr,
                    note = note,
                    bp = bp,
                    sugar = sugar,
                    temp = temp
                )
            )
        }
    }

    fun getPatientLogs(patientId: Long): Flow<List<PatientLogEntry>> {
        return repository.getPatientLogs(patientId)
    }

    // User Notes & Messages Flow
    val userNotes: StateFlow<List<UserNote>> = repository.allUserNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addUserNote(title: String, content: String, category: String = "عام") {
        viewModelScope.launch {
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            repository.addUserNote(
                UserNote(
                    title = title,
                    content = content,
                    category = category,
                    dateStr = dateStr
                )
            )
        }
    }

    fun deleteUserNote(id: Long) {
        viewModelScope.launch {
            repository.deleteUserNote(id)
        }
    }

    fun toggleUserNotePinned(id: Long) {
        viewModelScope.launch {
            repository.toggleUserNotePinned(id)
        }
    }

    // Gemini Chat
    fun sendAiQuestion(userQuery: String) {
        if (userQuery.isBlank() || _isAiLoading.value) return

        val userMsg = ChatMessage(sender = "USER", text = userQuery)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val responseText = repository.askGeminiHealthAdvisor(
                query = userQuery,
                ageGroupAr = _selectedAgeGroup.value.titleAr
            )
            val aiMsg = ChatMessage(sender = "AI", text = responseText)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiLoading.value = false
        }
    }
}
