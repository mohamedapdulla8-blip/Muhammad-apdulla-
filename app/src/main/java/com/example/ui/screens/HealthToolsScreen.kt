package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.MedicationItem
import com.example.data.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthToolsScreen(
    viewModel: HealthViewModel
) {
    var selectedToolTab by remember { mutableStateOf(0) } // 0 = Water, 1 = BMI, 2 = Medication Schedule

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "الأدوات والمتابعة الصحية",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            TabRow(selectedTabIndex = selectedToolTab) {
                Tab(
                    selected = selectedToolTab == 0,
                    onClick = { selectedToolTab = 0 },
                    text = { Text("حاسبة الماء", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.WaterDrop, contentDescription = null) }
                )
                Tab(
                    selected = selectedToolTab == 1,
                    onClick = { selectedToolTab = 1 },
                    text = { Text("مؤشر الكتلة", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) }
                )
                Tab(
                    selected = selectedToolTab == 2,
                    onClick = { selectedToolTab = 2 },
                    text = { Text("جدول الأدوية", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Alarm, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedToolTab) {
                0 -> WaterTrackerView(viewModel)
                1 -> BmiCalculatorView(viewModel)
                2 -> MedicationScheduleView(viewModel)
            }
        }
    }
}

@Composable
private fun WaterTrackerView(viewModel: HealthViewModel) {
    val waterLog by viewModel.waterLog.collectAsState()
    val calculatedRequirement by viewModel.calculatedWaterRequirement.collectAsState()
    var weightInput by remember { mutableStateOf("70") }

    val consumed = waterLog?.consumedMl ?: 0
    val target = waterLog?.targetMl ?: calculatedRequirement
    val progress = (consumed.toFloat() / target.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "استهلاك الماء اليومي",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$consumed / $target مل",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.addWaterCup(250) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_add_water")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة كوب (٢٥٠ مل)")
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetTodayWater() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("إعادة تصفير")
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "حاسبة الاحتياج المخصص حسب الوزن:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = {
                            weightInput = it
                            viewModel.waterWeightKg.value = it
                            viewModel.calculateWaterRequirement()
                        },
                        label = { Text("الوزن الحالي (كيلوجرام)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_water_weight"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "نصيحة: يحتاج الجسم لحوالي ٣٥ مل ماء لكل كيلوجرام من وزن الجسم تزيد عند ممارسة الرياضة.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
            }
        }
    }
}

@Composable
private fun BmiCalculatorView(viewModel: HealthViewModel) {
    var height by remember { mutableStateOf("170") }
    var weight by remember { mutableStateOf("70") }
    val bmiResult by viewModel.bmiResult.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "حاسبة مؤشر كتلة الجسم (BMI)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = height,
                            onValueChange = {
                                height = it
                                viewModel.bmiHeightCm.value = it
                            },
                            label = { Text("الطول (سم)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_bmi_height"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = weight,
                            onValueChange = {
                                weight = it
                                viewModel.bmiWeightKg.value = it
                            },
                            label = { Text("الوزن (كجم)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_bmi_weight"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.calculateBmi() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_calc_bmi"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("احسب المؤشر والنتيجة")
                    }
                }
            }
        }

        bmiResult?.let { (bmiVal, statusText) ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "نتيجة كتلة الجسم:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "%.1f".format(bmiVal),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تصنيف نتائج مؤشر كتلة الجسم المبسط:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• أقل من ١٨.٥: وزن أقل من الطبيعي", style = MaterialTheme.typography.bodySmall)
                    Text("• ١٨.٥ إلى ٢٤.٩: وزن طبيعي ومثالي", style = MaterialTheme.typography.bodySmall)
                    Text("• ٢٥ إلى ٢٩.٩: وزن زائد خفيف", style = MaterialTheme.typography.bodySmall)
                    Text("• ٣٠ فأكثر: سمنة تحتاج متابعة نمط حياة", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicationScheduleView(viewModel: HealthViewModel) {
    val context = LocalContext.current
    val medications by viewModel.medications.collectAsState()

    // Notification Permission Launcher (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "تم منح إذن إشعارات الأدوية بنجاح 🔔", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "يرجى التنسيق لمنح إذن الإشعارات لتلقي تنبيهات الأدوية في مواعيدها", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var medName by remember { mutableStateOf("") }
    var medDosage by remember { mutableStateOf("") }
    var medNotes by remember { mutableStateOf("بعد الطعام") }
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var enableNotify by remember { mutableStateOf(true) }

    fun formatHourMinute(hour: Int, minute: Int): String {
        val period = if (hour < 12) "صباحاً" else "مساءً"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "%02d:%02d %s".format(displayHour, minute, period)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // Header Info & Notification Test Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "مُنه المواعيد والتنبيهات الموقوتة",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.sendTestNotification(
                                    medicationName = "بنادول / الأدوية اليومية",
                                    dosage = "حبة واحدة 500 ملغم",
                                    instructions = "اختبار تنبيه موعد الدواء"
                                )
                                Toast.makeText(context, "تم إرسال إشعار تجريبي بنجاح! تفقد شريط الإشعارات 🔔", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_test_notification")
                        ) {
                            Text("اختبار الإشعار 🔔", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يقوم التطبيق بجدولة إشعارات نظام أندرويد الدقيقة لتذكيرك بأخذ ألمتك في الوقت المحدد يومياً.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "جدول الأدوية المسجلة (${medications.size}):",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_add_med")
                ) {
                    Icon(Icons.Default.AddAlarm, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة دواء وتنبيه")
                }
            }
        }

        if (medications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier.padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لم تقم بإضافة أدوية حتى الآن. اضغط على \"إضافة دواء وتنبيه\" لبدء التنظيم.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(medications, key = { it.id }) { med ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("med_card_${med.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (med.isTakenToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = med.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "الجرعة: ${med.dosage} • التعليمات: ${med.instructions}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = med.isTakenToday,
                                    onCheckedChange = { viewModel.toggleMedicationTaken(med.id, med.isTakenToday) },
                                    modifier = Modifier.testTag("check_med_${med.id}")
                                )

                                IconButton(onClick = { viewModel.deleteMedication(med.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Notification schedule bar & control
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (med.notificationEnabled) Icons.Default.AlarmOn else Icons.Default.AlarmOff,
                                    contentDescription = null,
                                    tint = if (med.notificationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (med.notificationEnabled) "موعد التنبيه: ${med.timeOfDay}" else "التنبيه متوقف (${med.timeOfDay})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (med.notificationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.sendTestNotification(
                                            medicationName = med.name,
                                            dosage = med.dosage,
                                            instructions = med.instructions
                                        )
                                        Toast.makeText(context, "تم تجربة إشعار ${med.name} 🔔", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("تجربة 🔔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Switch(
                                    checked = med.notificationEnabled,
                                    onCheckedChange = { newState ->
                                        viewModel.toggleMedicationNotification(med, newState)
                                        val msg = if (newState) "تم تفعيل التنبيه الموقوت لـ ${med.name}" else "تم إلغاء التنبيه لـ ${med.name}"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("switch_notify_${med.id}")
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("إضافة دواء وتحديد موعد التنبيه", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("اسم الدواء") },
                        placeholder = { Text("مثال: بندول سينس") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_med_name")
                    )

                    OutlinedTextField(
                        value = medDosage,
                        onValueChange = { medDosage = it },
                        label = { Text("الجرعة المطلوبة") },
                        placeholder = { Text("مثال: حبة واحدة (500 ملغم)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_med_dosage")
                    )

                    OutlinedTextField(
                        value = medNotes,
                        onValueChange = { medNotes = it },
                        label = { Text("تعليمات الاستخدام") },
                        placeholder = { Text("مثال: بعد تناول الطعام مع كاس ماء") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_med_notes")
                    )

                    Text("توقيت التنبيه اليومي:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "الموعد المختار: ${formatHourMinute(selectedHour, selectedMinute)}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )

                            Text("الساعة (0 - 23): ${selectedHour}", style = MaterialTheme.typography.labelMedium)
                            Slider(
                                value = selectedHour.toFloat(),
                                onValueChange = { selectedHour = it.toInt() },
                                valueRange = 0f..23f,
                                steps = 22
                            )

                            Text("الدقيقة (0 - 59): ${selectedMinute}", style = MaterialTheme.typography.labelMedium)
                            Slider(
                                value = selectedMinute.toFloat(),
                                onValueChange = { selectedMinute = it.toInt() },
                                valueRange = 0f..55f,
                                steps = 10
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("تفعيل التنبيه التلقائي 🔔", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = enableNotify,
                            onCheckedChange = { enableNotify = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (medName.isNotBlank()) {
                            val timeStr = formatHourMinute(selectedHour, selectedMinute)
                            viewModel.addMedication(
                                name = medName,
                                dosage = medDosage.ifBlank { "جرعة واحدة" },
                                timeOfDay = timeStr,
                                instructions = medNotes.ifBlank { "حسب إرشادات الطبيب" },
                                hour = selectedHour,
                                minute = selectedMinute,
                                notificationEnabled = enableNotify
                            )
                            Toast.makeText(context, "تم حفظ الدواء وجدولة التنبيه بنجاح! 🔔", Toast.LENGTH_SHORT).show()
                            medName = ""
                            medDosage = ""
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_save_med")
                ) {
                    Text("حفظ الدواء والتنبيه")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
