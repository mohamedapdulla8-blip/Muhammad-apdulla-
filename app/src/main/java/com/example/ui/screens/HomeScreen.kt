package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.AgeGroup
import com.example.data.models.HealthTopic
import com.example.data.viewmodel.HealthViewModel
import com.example.ui.components.*
import com.example.ui.theme.CoralAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HealthViewModel,
    onNavigateToTools: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToTherapeutic: () -> Unit,
    onNavigateToPatients: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {}
) {
    val context = LocalContext.current
    val selectedAgeGroup by viewModel.selectedAgeGroup.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val dailyTip by viewModel.dailyTip.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.updateSearchQuery(spokenText)
            }
        }
    }

    val filteredTopics = remember(selectedAgeGroup, searchQuery) {
        val topics = if (selectedAgeGroup == AgeGroup.ALL) {
            com.example.data.StaticHealthData.HEALTH_TOPICS
        } else {
            com.example.data.StaticHealthData.HEALTH_TOPICS.filter {
                it.ageGroup == selectedAgeGroup || it.ageGroup == AgeGroup.ALL
            }
        }
        if (searchQuery.isBlank()) {
            topics
        } else {
            val q = searchQuery.trim().lowercase()
            topics.filter {
                it.title.lowercase().contains(q) || it.summary.lowercase().contains(q)
            }
        }
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Hero Header
            item {
                HeroHeaderBanner(
                    title = "ثقافة صحية وتوعية علاجية",
                    subtitle = "معلومات صحية مبسطة وموثوقة لمختلف الأعمار",
                    drawableId = R.drawable.img_health_hero_1784675411510
                )
            }

            // Daily Tip Box
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_tip_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "نصيحة اليوم الصحية",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }

                            IconButton(
                                onClick = { viewModel.refreshDailyTip() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "تحديث النصيحة",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = dailyTip,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }
            }

            // Search Field & Gemini AI Search Integration
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input"),
                        placeholder = { Text("ابحث عن موضوع صحي، حالة مرضية، أو دواء...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث بالبحث الصحي...")
                                        }
                                        try {
                                            speechLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "خاصية التعرف على الصوت غير متاحة في هذا الجهاز", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("btn_mic_search")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "البحث بالميكروفون",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    if (searchQuery.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_search_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "بحث واستفسار ذكي بـ Gemini AI",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Text(
                                            text = "إيضاح مفصل عن: \"${searchQuery.take(25)}\"",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        val q = "اشرح لي بالتفصيل وبأسلوب توعوي مبسط عن: $searchQuery"
                                        viewModel.sendAiQuestion(q)
                                        onNavigateToAi()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("btn_ask_gemini_search")
                                ) {
                                    Text("اسأل AI ✨")
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions Carousel / Row
            item {
                Column {
                    Text(
                        text = "الخدمات والأدوات التفاعلية السريعة:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            QuickActionChip(
                                title = "متابعة المرضى",
                                icon = Icons.Default.MonitorHeart,
                                onClick = onNavigateToPatients,
                                tag = "quick_tool_patients"
                            )
                        }
                        item {
                            QuickActionChip(
                                title = "ملاحظاتي ورسائلي",
                                icon = Icons.Default.EditNote,
                                onClick = onNavigateToNotes,
                                tag = "quick_tool_notes"
                            )
                        }
                        item {
                            QuickActionChip(
                                title = "حاسبة الماء",
                                icon = Icons.Default.WaterDrop,
                                onClick = onNavigateToTools,
                                tag = "quick_tool_water"
                            )
                        }
                        item {
                            QuickActionChip(
                                title = "مؤشر الكتلة",
                                icon = Icons.Default.MonitorWeight,
                                onClick = onNavigateToTools,
                                tag = "quick_tool_bmi"
                            )
                        }
                        item {
                            QuickActionChip(
                                title = "اختبار الثقافة",
                                icon = Icons.Default.Quiz,
                                onClick = onNavigateToQuiz,
                                tag = "quick_tool_quiz"
                            )
                        }
                        item {
                            QuickActionChip(
                                title = "مساعد AI الطبية",
                                icon = Icons.Default.Psychology,
                                onClick = onNavigateToAi,
                                tag = "quick_tool_ai"
                            )
                        }
                        item {
                            QuickActionChip(
                                title = "إسعافات أولية",
                                icon = Icons.Default.MedicalServices,
                                onClick = onNavigateToTherapeutic,
                                tag = "quick_tool_first_aid"
                            )
                        }
                    }
                }
            }

            // Age Group Selector Chips
            item {
                AgeGroupFilterSelector(
                    selectedGroup = selectedAgeGroup,
                    onGroupSelected = { viewModel.selectAgeGroup(it) }
                )
            }

            // Topic Articles Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "دليل الإرشادات التوعوية (${filteredTopics.size}):",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Feed Items
            if (filteredTopics.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "لم نجد موضوعات تطابق بحثك.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                            )
                        }
                    }
                }
            } else {
                items(filteredTopics) { topic ->
                    val isBookmarked = bookmarks.any { it.id == topic.id }
                    HealthTopicCard(
                        topic = topic,
                        onClick = { viewModel.selectTopic(topic) },
                        onBookmarkToggle = { viewModel.toggleBookmark(topic) },
                        isBookmarked = isBookmarked
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Modal Bottom Sheet when topic clicked
        selectedTopic?.let { topic ->
            val isBookmarked = bookmarks.any { it.id == topic.id }
            TopicDetailModal(
                topic = topic,
                isBookmarked = isBookmarked,
                onBookmarkToggle = { viewModel.toggleBookmark(topic) },
                onDismiss = { viewModel.selectTopic(null) }
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
