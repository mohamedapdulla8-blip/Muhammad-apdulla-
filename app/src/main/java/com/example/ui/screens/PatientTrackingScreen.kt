package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PatientLogEntry
import com.example.data.models.PatientRecord
import com.example.data.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientTrackingScreen(
    viewModel: HealthViewModel,
    onNavigateToAi: () -> Unit
) {
    val patients by viewModel.patientRecords.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedPatientForLog by remember { mutableStateOf<PatientRecord?>(null) }
    var selectedPatientForHistory by remember { mutableStateOf<PatientRecord?>(null) }

    val filteredPatients = remember(patients, searchQuery) {
        if (searchQuery.isBlank()) patients
        else {
            val q = searchQuery.trim().lowercase()
            patients.filter {
                it.patientName.lowercase().contains(q) ||
                        it.conditionTitle.lowercase().contains(q) ||
                        it.status.lowercase().contains(q)
            }
        }
    }

    val stableCount = patients.count { it.status.contains("مستقرة") }
    val attentionCount = patients.size - stableCount

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "إضافة مريض") },
                text = { Text("إضافة حالة جديدة", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_patient")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Header Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_tracking_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "متابعة الحالات الصحية للمرضى",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "سجل متابعة المؤشرات الحيوية والتوصيات الطبية لأفراد الأسرة",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.MonitorHeart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // Stats Summary Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "إجمالي الحالات",
                        value = "${patients.size}",
                        icon = Icons.Default.AssignmentInd,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "مستقرة",
                        value = "$stableCount",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "تحتاج متابعة",
                        value = "$attentionCount",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث باسم المريض أو التشخيص...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_search_input"),
                    singleLine = true
                )
            }

            // Section Title
            item {
                Text(
                    text = "سجلات المتابعة اليومية (${filteredPatients.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Patient List
            if (filteredPatients.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد سجلات مرضى مطابقة للبحث",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredPatients, key = { it.id }) { patient ->
                    PatientCardItem(
                        patient = patient,
                        onAddLog = { selectedPatientForLog = patient },
                        onViewHistory = { selectedPatientForHistory = patient },
                        onAskAi = {
                            val prompt = "أنا أتابع حالة المريض: ${patient.patientName}، العمر: ${patient.age} سنة. التشخيص: ${patient.conditionTitle}. حالة المريض العامة: ${patient.status}. آخر قراءة لضغط الدم: ${patient.latestBloodPressure}، والسكر: ${patient.latestBloodSugar}، والحرارة: ${patient.latestTemperature}. توصيات الطبيب: ${patient.doctorNotes}. أرجو تزويدي بتوجيهات صحية وتوعوية وإشارات السلامة التي ينبغي الانتباه لها."
                            viewModel.sendAiQuestion(prompt)
                            onNavigateToAi()
                        },
                        onDelete = { viewModel.deletePatient(patient.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Add New Patient Dialog
    if (showAddDialog) {
        AddPatientDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, age, condition, status, bp, sugar, temp, notes, nextApp ->
                viewModel.addPatient(name, age, condition, status, bp, sugar, temp, notes, nextApp)
                showAddDialog = false
            }
        )
    }

    // Add Log Dialog
    selectedPatientForLog?.let { patient ->
        AddPatientLogDialog(
            patientName = patient.patientName,
            onDismiss = { selectedPatientForLog = null },
            onConfirm = { note, bp, sugar, temp ->
                viewModel.addPatientLog(patient.id, note, bp, sugar, temp)
                selectedPatientForLog = null
            }
        )
    }

    // View History Sheet/Dialog
    selectedPatientForHistory?.let { patient ->
        PatientHistoryDialog(
            patient = patient,
            viewModel = viewModel,
            onDismiss = { selectedPatientForHistory = null }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PatientCardItem(
    patient: PatientRecord,
    onAddLog: () -> Unit,
    onViewHistory: () -> Unit,
    onAskAi: () -> Unit,
    onDelete: () -> Unit
) {
    val isStable = patient.status.contains("مستقرة")
    val statusBg = if (isStable) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
    val statusText = if (isStable) Color(0xFF2E7D32) else Color(0xFFE65100)

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("patient_card_${patient.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Avatar, Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = patient.patientName.take(1),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = patient.patientName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "العمر: ${patient.age} سنة • ${patient.conditionTitle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = patient.status,
                        color = statusText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Vitals Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VitalItem("ضغط الدم", patient.latestBloodPressure.ifBlank { "غير مسجل" }, Icons.Default.Favorite)
                VitalItem("مستوى السكر", patient.latestBloodSugar.ifBlank { "غير مسجل" }, Icons.Default.WaterDrop)
                VitalItem("الحرارة", patient.latestTemperature.ifBlank { "37.0 °C" }, Icons.Default.Thermostat)
            }

            if (patient.doctorNotes.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "توصيات الطبيب: ${patient.doctorNotes}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (patient.nextAppointment.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الموعد القادم: ${patient.nextAppointment}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAddLog,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة قراءة", fontSize = 12.sp)
                }

                Button(
                    onClick = onAskAi,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("استشارة AI", fontSize = 12.sp)
                }

                IconButton(onClick = onViewHistory) {
                    Icon(Icons.Default.History, contentDescription = "السجل السابق", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun VitalItem(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, age: Int, condition: String, status: String, bp: String, sugar: String, temp: String, notes: String, nextApp: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("50") }
    var condition by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("مستقرة") }
    var bp by remember { mutableStateOf("") }
    var sugar by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf("37.0 °C") }
    var notes by remember { mutableStateOf("") }
    var nextApp by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة سجل مريض جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المريض/المتابع") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = { ageStr = it },
                        label = { Text("العمر") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text("الحالة (مستقرة/تحتاج متابعة)") },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("التشخيص/الحالة الصحية (مثال: السكري)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bp,
                        onValueChange = { bp = it },
                        label = { Text("ضغط الدم (120/80)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sugar,
                        onValueChange = { sugar = it },
                        label = { Text("مستوى السكر") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توصيات وملاحظات الطبيب") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nextApp,
                    onValueChange = { nextApp = it },
                    label = { Text("موعد المراجعة القادمة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val age = ageStr.toIntOrNull() ?: 30
                        onConfirm(name, age, condition, status, bp, sugar, temp, notes, nextApp)
                    }
                }
            ) {
                Text("حفظ السجل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun AddPatientLogDialog(
    patientName: String,
    onDismiss: () -> Unit,
    onConfirm: (note: String, bp: String, sugar: String, temp: String) -> Unit
) {
    var note by remember { mutableStateOf("") }
    var bp by remember { mutableStateOf("") }
    var sugar by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة قراءة اليوم لـ $patientName", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = bp,
                    onValueChange = { bp = it },
                    label = { Text("ضغط الدم (مثال: 120/80)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sugar,
                    onValueChange = { sugar = it },
                    label = { Text("مستوى السكر (مثال: 110 mg/dL)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = temp,
                    onValueChange = { temp = it },
                    label = { Text("درجة الحرارة (مثال: 37.0 °C)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظات المريض أو الأعراض اليومية") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(note, bp, sugar, temp)
                }
            ) {
                Text("تسجيل القراءة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun PatientHistoryDialog(
    patient: PatientRecord,
    viewModel: HealthViewModel,
    onDismiss: () -> Unit
) {
    val logs by viewModel.getPatientLogs(patient.id).collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("سجل قراءات ${patient.patientName}", fontWeight = FontWeight.Bold) },
        text = {
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد قراءات مسجلة سابقة بعد لهذا المريض.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(logs) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = log.dateStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (log.bp.isNotBlank()) Text("الضغط: ${log.bp}", style = MaterialTheme.typography.bodySmall)
                                if (log.sugar.isNotBlank()) Text("السكر: ${log.sugar}", style = MaterialTheme.typography.bodySmall)
                                if (log.note.isNotBlank()) Text("الملاحظات: ${log.note}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}
