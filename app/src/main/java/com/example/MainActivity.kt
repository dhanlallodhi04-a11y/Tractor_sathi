package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.ui.KhetSaathiViewModel
import com.example.ui.components.CelebrationDialog
import com.example.ui.components.FloatingAiAssistant
import com.example.ui.components.GuidedTourOverlay
import com.example.ui.components.KhetErrorBoundary
import com.example.ui.components.NotificationPermissionDialog
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.AddEditImplementScreen
import com.example.ui.screens.AddEditKisanScreen
import com.example.ui.screens.AddJobScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.HelpSupportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImplementDetailScreen
import com.example.ui.screens.ImplementsScreen
import com.example.ui.screens.JobsScreen
import com.example.ui.screens.KisanDetailScreen
import com.example.ui.screens.KisanScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.NotificationCenterScreen
import com.example.ui.screens.NotificationSettingsScreen
import com.example.ui.screens.OnboardingWizardScreen
import com.example.ui.screens.ProfileAuthScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.VehiclesScreen
import com.example.ui.theme.BorderNeutral
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.MintTint
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.util.Strings
import com.example.util.configureAppCheck

enum class AppDestination {
    HOME,
    JOBS,
    REPORTS,
    EXPENSES,
    MORE,
    ADD_JOB,
    KISAN_LIST,
    KISAN_DETAIL,
    ADD_EDIT_KISAN,
    VEHICLES,
    IMPLEMENTS,
    IMPLEMENT_DETAIL,
    ADD_EDIT_IMPLEMENT,
    PROFILE,
    DIAGNOSTICS,
    HELP_SUPPORT,
    NOTIFICATION_CENTER,
    REMINDERS,
    ACHIEVEMENTS,
    NOTIFICATION_SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Step 1: Configure App Check before any UI or Firebase operations
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: KhetSaathiViewModel = viewModel()
                KhetSaathiApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun KhetSaathiApp(viewModel: KhetSaathiViewModel) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val isTourActive by viewModel.isTourActive.collectAsStateWithLifecycle()
    val tourStepIndex by viewModel.tourStepIndex.collectAsStateWithLifecycle()
    val isTourMuted by viewModel.isTourMuted.collectAsStateWithLifecycle()
    val celebrationData by viewModel.celebrationData.collectAsStateWithLifecycle()
    val showPermissionDialog by viewModel.showPermissionDialog.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var previousTab by remember { mutableStateOf(AppDestination.HOME) }
    var selectedKisanId by remember { mutableStateOf<String?>(null) }
    var editKisanId by remember { mutableStateOf<String?>(null) }
    var selectedImplementId by remember { mutableStateOf<String?>(null) }
    var editImplementId by remember { mutableStateOf<String?>(null) }

    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        viewModel.setPermissionDialogVisible(false)
    }

    fun handleDeepLink(link: String) {
        when {
            link.startsWith("kisan_detail:") -> {
                selectedKisanId = link.removePrefix("kisan_detail:")
                currentDestination = AppDestination.KISAN_DETAIL
            }
            link.startsWith("add_payment:") -> {
                selectedKisanId = link.removePrefix("add_payment:")
                currentDestination = AppDestination.KISAN_DETAIL
            }
            link == "baki_list" -> currentDestination = AppDestination.KISAN_LIST
            link == "reports" -> currentDestination = AppDestination.REPORTS
            link == "vehicles" -> currentDestination = AppDestination.VEHICLES
            link == "add_job" -> currentDestination = AppDestination.ADD_JOB
            link == "reminders" -> currentDestination = AppDestination.REMINDERS
            link == "achievements" -> currentDestination = AppDestination.ACHIEVEMENTS
            link == "notification_center" -> currentDestination = AppDestination.NOTIFICATION_CENTER
        }
    }

    // First-run Onboarding Gate: If user hasn't completed onboarding, render wizard
    if (user != null && !user!!.onboardingDone) {
        OnboardingWizardScreen(
            viewModel = viewModel,
            onOnboardingFinished = {
                currentDestination = AppDestination.HOME
                // Prompt for notification permission nicely after onboarding
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    viewModel.setPermissionDialogVisible(true)
                }
            }
        )
        return
    }

    val isTopLevelTab = currentDestination in listOf(
        AppDestination.HOME,
        AppDestination.JOBS,
        AppDestination.REPORTS,
        AppDestination.EXPENSES,
        AppDestination.MORE
    )

    // Handle back button for secondary screens
    if (!isTopLevelTab) {
        BackHandler {
            when (currentDestination) {
                AppDestination.KISAN_DETAIL -> currentDestination = AppDestination.KISAN_LIST
                AppDestination.ADD_EDIT_KISAN -> {
                    currentDestination = if (selectedKisanId != null) AppDestination.KISAN_DETAIL else AppDestination.KISAN_LIST
                }
                AppDestination.IMPLEMENT_DETAIL -> currentDestination = AppDestination.IMPLEMENTS
                AppDestination.ADD_EDIT_IMPLEMENT -> {
                    currentDestination = if (selectedImplementId != null) AppDestination.IMPLEMENT_DETAIL else AppDestination.IMPLEMENTS
                }
                AppDestination.KISAN_LIST, AppDestination.IMPLEMENTS, AppDestination.VEHICLES, AppDestination.PROFILE, AppDestination.DIAGNOSTICS, AppDestination.HELP_SUPPORT, AppDestination.REMINDERS, AppDestination.ACHIEVEMENTS, AppDestination.NOTIFICATION_SETTINGS -> {
                    currentDestination = AppDestination.MORE
                }
                AppDestination.NOTIFICATION_CENTER -> currentDestination = previousTab
                AppDestination.ADD_JOB -> currentDestination = previousTab
                else -> currentDestination = previousTab
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (isTopLevelTab) {
                    Surface(
                        color = CardWhite,
                        border = BorderStroke(1.5.dp, BorderNeutral),
                        shadowElevation = 6.dp
                    ) {
                        NavigationBar(
                            containerColor = CardWhite,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .testTag("bottom_nav_bar")
                        ) {
                            val tabs = listOf(
                                AppDestination.HOME to (Strings.get("tab_home", lang) to Icons.Default.Home),
                                AppDestination.JOBS to (Strings.get("tab_jobs", lang) to Icons.Default.Agriculture),
                                AppDestination.REPORTS to (Strings.get("tab_reports", lang) to Icons.Default.BarChart),
                                AppDestination.EXPENSES to (Strings.get("tab_expenses", lang) to Icons.Default.LocalGasStation),
                                AppDestination.MORE to (Strings.get("tab_more", lang) to Icons.Default.MoreHoriz)
                            )

                            tabs.forEach { (dest, pair) ->
                                val (label, icon) = pair
                                val isSelected = currentDestination == dest
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        currentDestination = dest
                                        previousTab = dest
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = DeepForestGreen,
                                        selectedTextColor = DeepForestGreen,
                                        indicatorColor = MintTint,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${dest.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                KhetErrorBoundary(screenName = currentDestination.name) {
                    when (currentDestination) {
                        AppDestination.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToAddJob = {
                                    previousTab = AppDestination.HOME
                                    currentDestination = AppDestination.ADD_JOB
                                },
                                onNavigateToJobs = {
                                    currentDestination = AppDestination.JOBS
                                    previousTab = AppDestination.JOBS
                                },
                                onNavigateToCustomers = {
                                    previousTab = AppDestination.HOME
                                    currentDestination = AppDestination.KISAN_LIST
                                },
                                onNavigateToProfile = {
                                    previousTab = AppDestination.HOME
                                    currentDestination = AppDestination.PROFILE
                                },
                                onNavigateToReports = {
                                    currentDestination = AppDestination.REPORTS
                                    previousTab = AppDestination.REPORTS
                                },
                                onNavigateToExpenses = {
                                    currentDestination = AppDestination.EXPENSES
                                    previousTab = AppDestination.EXPENSES
                                },
                                onNavigateToVehicles = {
                                    previousTab = AppDestination.HOME
                                    currentDestination = AppDestination.VEHICLES
                                },
                                onNavigateToImplements = {
                                    previousTab = AppDestination.HOME
                                    currentDestination = AppDestination.IMPLEMENTS
                                },
                                onNavigateToNotifications = {
                                    previousTab = AppDestination.HOME
                                    currentDestination = AppDestination.NOTIFICATION_CENTER
                                },
                                onSelectJob = {
                                    currentDestination = AppDestination.JOBS
                                    previousTab = AppDestination.JOBS
                                }
                            )
                        }
                        AppDestination.JOBS -> {
                            JobsScreen(
                                viewModel = viewModel,
                                onNavigateToAddJob = {
                                    previousTab = AppDestination.JOBS
                                    currentDestination = AppDestination.ADD_JOB
                                }
                            )
                        }
                        AppDestination.REPORTS -> {
                            ReportsScreen(viewModel = viewModel)
                        }
                        AppDestination.EXPENSES -> {
                            ExpensesScreen(viewModel = viewModel)
                        }
                        AppDestination.MORE -> {
                            MoreScreen(
                                viewModel = viewModel,
                                onNavigateToCustomers = {
                                    previousTab = AppDestination.MORE
                                    currentDestination = AppDestination.KISAN_LIST
                                },
                                onNavigateToVehicles = {
                                    previousTab = AppDestination.MORE
                                    currentDestination = AppDestination.VEHICLES
                                },
                                onNavigateToImplements = {
                                    previousTab = AppDestination.MORE
                                    currentDestination = AppDestination.IMPLEMENTS
                                },
                                onNavigateToProfile = {
                                    previousTab = AppDestination.MORE
                                    currentDestination = AppDestination.PROFILE
                                },
                                onNavigateToDiagnostics = {
                                    previousTab = AppDestination.MORE
                                    currentDestination = AppDestination.DIAGNOSTICS
                                },
                                onNavigateToHelpSupport = {
                                    previousTab = AppDestination.MORE
                                    currentDestination = AppDestination.HELP_SUPPORT
                                },
                                onNavigateToNotifications = {
                                    previousTab = AppDestination.MORE
                                    currentDestination = AppDestination.NOTIFICATION_CENTER
                                },
                                onNavigateToReminders = {
                                    previousTab = AppDestination.MORE
                                    currentDestination = AppDestination.REMINDERS
                                },
                                onNavigateToAchievements = {
                                    previousTab = AppDestination.MORE
                                    currentDestination = AppDestination.ACHIEVEMENTS
                                },
                                onNavigateToNotificationSettings = {
                                    previousTab = AppDestination.MORE
                                    currentDestination = AppDestination.NOTIFICATION_SETTINGS
                                },
                                onLaunchTour = {
                                    viewModel.startTour()
                                    currentDestination = AppDestination.HOME
                                }
                            )
                        }
                        AppDestination.ADD_JOB -> {
                            AddJobScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentDestination = previousTab }
                            )
                        }
                        AppDestination.KISAN_LIST -> {
                            KisanScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentDestination = AppDestination.MORE },
                                onNavigateToAddKisan = {
                                    editKisanId = null
                                    currentDestination = AppDestination.ADD_EDIT_KISAN
                                },
                                onNavigateToKisanDetail = { kId ->
                                    selectedKisanId = kId
                                    currentDestination = AppDestination.KISAN_DETAIL
                                }
                            )
                        }
                        AppDestination.KISAN_DETAIL -> {
                            val kId = selectedKisanId
                            if (kId != null) {
                                KisanDetailScreen(
                                    kisanId = kId,
                                    viewModel = viewModel,
                                    onNavigateBack = { currentDestination = AppDestination.KISAN_LIST },
                                    onNavigateToEdit = { editId ->
                                        editKisanId = editId
                                        currentDestination = AppDestination.ADD_EDIT_KISAN
                                    }
                                )
                            } else {
                                currentDestination = AppDestination.KISAN_LIST
                            }
                        }
                        AppDestination.ADD_EDIT_KISAN -> {
                            AddEditKisanScreen(
                                kisanId = editKisanId,
                                viewModel = viewModel,
                                onNavigateBack = {
                                    currentDestination = if (selectedKisanId != null) AppDestination.KISAN_DETAIL else AppDestination.KISAN_LIST
                                },
                                onOpenExisting = { existingId ->
                                    selectedKisanId = existingId
                                    currentDestination = AppDestination.KISAN_DETAIL
                                }
                            )
                        }
                        AppDestination.VEHICLES -> {
                            VehiclesScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentDestination = previousTab }
                            )
                        }
                        AppDestination.IMPLEMENTS -> {
                            ImplementsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentDestination = AppDestination.MORE },
                                onNavigateToAdd = {
                                    editImplementId = null
                                    currentDestination = AppDestination.ADD_EDIT_IMPLEMENT
                                },
                                onNavigateToDetail = { impId ->
                                    selectedImplementId = impId
                                    currentDestination = AppDestination.IMPLEMENT_DETAIL
                                }
                            )
                        }
                        AppDestination.IMPLEMENT_DETAIL -> {
                            val impId = selectedImplementId
                            if (impId != null) {
                                ImplementDetailScreen(
                                    implementId = impId,
                                    viewModel = viewModel,
                                    onNavigateBack = { currentDestination = AppDestination.IMPLEMENTS },
                                    onNavigateToEdit = { editId ->
                                        editImplementId = editId
                                        currentDestination = AppDestination.ADD_EDIT_IMPLEMENT
                                    }
                                )
                            } else {
                                currentDestination = AppDestination.IMPLEMENTS
                            }
                        }
                        AppDestination.ADD_EDIT_IMPLEMENT -> {
                            AddEditImplementScreen(
                                implementId = editImplementId,
                                viewModel = viewModel,
                                onNavigateBack = {
                                    currentDestination = if (selectedImplementId != null) AppDestination.IMPLEMENT_DETAIL else AppDestination.IMPLEMENTS
                                }
                            )
                        }
                        AppDestination.PROFILE -> {
                            ProfileAuthScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentDestination = previousTab }
                            )
                        }
                        AppDestination.DIAGNOSTICS -> {
                            DiagnosticsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentDestination = AppDestination.MORE }
                            )
                        }
                        AppDestination.HELP_SUPPORT -> {
                            HelpSupportScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentDestination = AppDestination.MORE }
                            )
                        }
                        AppDestination.NOTIFICATION_CENTER -> {
                            NotificationCenterScreen(
                                viewModel = viewModel,
                                onBack = { currentDestination = previousTab },
                                onNavigateDeepLink = { link -> handleDeepLink(link) }
                            )
                        }
                        AppDestination.REMINDERS -> {
                            RemindersScreen(
                                viewModel = viewModel,
                                onBack = { currentDestination = AppDestination.MORE },
                                onSelectCustomer = { cust ->
                                    selectedKisanId = cust.id
                                    currentDestination = AppDestination.KISAN_DETAIL
                                }
                            )
                        }
                        AppDestination.ACHIEVEMENTS -> {
                            AchievementsScreen(
                                viewModel = viewModel,
                                onBack = { currentDestination = AppDestination.MORE }
                            )
                        }
                        AppDestination.NOTIFICATION_SETTINGS -> {
                            NotificationSettingsScreen(
                                viewModel = viewModel,
                                onBack = { currentDestination = AppDestination.MORE }
                            )
                        }
                    }
                }
            }
        }

        // Floating AI Assistant: Sits at bottom-left (opposing the bottom-right "+" buttons)
        FloatingAiAssistant(
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = if (isTopLevelTab) 90.dp else 24.dp)
        )

        // Guided Tour Spotlight Overlay
        if (isTourActive) {
            GuidedTourOverlay(
                viewModel = viewModel,
                stepIndex = tourStepIndex,
                isMuted = isTourMuted,
                onNext = { viewModel.nextTourStep(4) },
                onPrev = { viewModel.prevTourStep() },
                onDismiss = { viewModel.dismissTour() },
                onToggleMute = { viewModel.toggleTourMute() }
            )
        }

        // Celebration Dialog Overlay
        if (celebrationData != null) {
            CelebrationDialog(
                data = celebrationData!!,
                onDismiss = { viewModel.dismissCelebration() }
            )
        }

        // Friendly Notification Permission Pre-Screen Dialog
        if (showPermissionDialog) {
            NotificationPermissionDialog(
                onConfirm = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setPermissionDialogVisible(false)
                    }
                },
                onDismiss = { viewModel.setPermissionDialogVisible(false) }
            )
        }
    }
}
