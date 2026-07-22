package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.viewmodel.HealthViewModel
import com.example.ui.screens.*
import com.example.ui.theme.HealthTheme

class MainActivity : ComponentActivity() {

    private val healthViewModel: HealthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by healthViewModel.isDarkTheme.collectAsState()
            HealthTheme(darkTheme = isDarkTheme) {
                // Force Right-to-Left (RTL) layout direction for Arabic
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    HealthAppMainShell(viewModel = healthViewModel)
                }
            }
        }
    }
}

sealed class Screen(val route: String, val titleAr: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "الرئيسية", Icons.Default.Home)
    object Patients : Screen("patients", "المتابعة", Icons.Default.MonitorHeart)
    object Notes : Screen("notes", "ملاحظاتي", Icons.Default.EditNote)
    object Therapeutic : Screen("therapeutic", "التوعية", Icons.Default.HealthAndSafety)
    object Tools : Screen("tools", "الأدوات", Icons.Default.Calculate)
    object Quiz : Screen("quiz", "الاختبار", Icons.Default.Quiz)
    object Assistant : Screen("assistant", "اسأل AI", Icons.Default.AutoAwesome)
    object Bookmarks : Screen("bookmarks", "المحفوظات", Icons.Default.Bookmark)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthAppMainShell(viewModel: HealthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        Screen.Home,
        Screen.Patients,
        Screen.Notes,
        Screen.Therapeutic,
        Screen.Tools,
        Screen.Assistant
    )

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ثقافة صحية مبسطة",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleDarkTheme() },
                        modifier = Modifier.testTag("nav_btn_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "تبديل المظهر",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { navController.navigate(Screen.Bookmarks.route) },
                        modifier = Modifier.testTag("nav_btn_bookmarks")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "الموضوعات المحفوظة",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                navItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = screen.icon, contentDescription = screen.titleAr) },
                        label = {
                            Text(
                                text = screen.titleAr,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        modifier = Modifier.testTag("tab_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToTools = { navController.navigate(Screen.Tools.route) },
                    onNavigateToQuiz = { navController.navigate(Screen.Quiz.route) },
                    onNavigateToAi = { navController.navigate(Screen.Assistant.route) },
                    onNavigateToTherapeutic = { navController.navigate(Screen.Therapeutic.route) },
                    onNavigateToPatients = { navController.navigate(Screen.Patients.route) },
                    onNavigateToNotes = { navController.navigate(Screen.Notes.route) }
                )
            }
            composable(Screen.Patients.route) {
                PatientTrackingScreen(
                    viewModel = viewModel,
                    onNavigateToAi = { navController.navigate(Screen.Assistant.route) }
                )
            }
            composable(Screen.Notes.route) {
                com.example.ui.screens.UserNotesScreen(
                    viewModel = viewModel,
                    onNavigateToAi = { navController.navigate(Screen.Assistant.route) }
                )
            }
            composable(Screen.Therapeutic.route) {
                TherapeuticAwarenessScreen(viewModel = viewModel)
            }
            composable(Screen.Tools.route) {
                HealthToolsScreen(viewModel = viewModel)
            }
            composable(Screen.Quiz.route) {
                HealthQuizScreen(viewModel = viewModel)
            }
            composable(Screen.Assistant.route) {
                GeminiAssistantScreen(viewModel = viewModel)
            }
            composable(Screen.Bookmarks.route) {
                BookmarksScreen(viewModel = viewModel)
            }
        }
    }
}
