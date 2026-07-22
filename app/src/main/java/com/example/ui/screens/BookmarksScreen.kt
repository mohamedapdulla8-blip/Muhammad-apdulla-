package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.HealthTopic
import com.example.data.models.TopicCategory
import com.example.data.models.AgeGroup
import com.example.data.viewmodel.HealthViewModel
import com.example.ui.components.TopicDetailModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: HealthViewModel
) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    var selectedTopicForModal by remember { mutableStateOf<HealthTopic?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "الموضوعات والإرشادات المحفوظة",
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
            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد موضوعات محفوظة في مفضلتك حتى الآن.",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(bookmarks) { item ->
                        val dummyTopic = HealthTopic(
                            id = item.id,
                            title = item.title,
                            category = TopicCategory.values().find { it.titleAr == item.category } ?: TopicCategory.THERAPEUTIC,
                            ageGroup = AgeGroup.ALL,
                            summary = item.summary,
                            detailedContent = item.content,
                            bulletPoints = emptyList(),
                            quickTip = "محفوظ في قائمة المفضلة"
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bookmark_item_${item.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.summary,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                                        maxLines = 2
                                    )
                                }

                                Row {
                                    Button(
                                        onClick = { selectedTopicForModal = dummyTopic },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("قراءة")
                                    }

                                    IconButton(
                                        onClick = { viewModel.toggleBookmark(dummyTopic) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BookmarkRemove,
                                            contentDescription = "إزالة",
                                            tint = Color.Red.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedTopicForModal?.let { topic ->
            TopicDetailModal(
                topic = topic,
                isBookmarked = true,
                onBookmarkToggle = {
                    viewModel.toggleBookmark(topic)
                    selectedTopicForModal = null
                },
                onDismiss = { selectedTopicForModal = null }
            )
        }
    }
}
