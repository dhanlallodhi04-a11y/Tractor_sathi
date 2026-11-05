package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.KhetSaathiViewModel
import com.example.ui.components.KhetCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberLight
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.DueRed
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.BorderNeutral
import com.example.ui.theme.MintTint
import com.example.ui.theme.WarmCreamBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareHelper
import com.example.util.Strings

@Composable
fun MoreScreen(
    viewModel: KhetSaathiViewModel,
    onNavigateToCustomers: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    onNavigateToImplements: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDiagnostics: () -> Unit = {},
    onNavigateToHelpSupport: () -> Unit = {},
    onLaunchTour: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToNotificationSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showSeedSuccessDialog by remember { mutableStateOf(false) }
    var showVoiceTipsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 16.dp)
            .testTag("more_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = Strings.get("settings", lang),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Profile Card
        item {
            KhetCard(onClick = onNavigateToProfile) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AmberLight,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user?.name?.take(1) ?: "R",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = user?.name ?: "रामसिंह लोधी",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = user?.phone ?: "+91 9826012345",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Text(
                                text = "जन्म तिथि: ${user?.dob ?: "1988-06-15"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Text("बदलें >", color = ForestGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section: Management Shortcuts
        item {
            KhetCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("व्यवसाय प्रबंधन (Management)", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsMenuItem(
                        icon = Icons.Default.Group,
                        title = if (lang == "hi") "किसान (Kisan)" else "Kisan",
                        subtitle = if (lang == "hi") "किसान सूची, उधारी खाता व तकादा" else "Khata ledger, balance due & reminders",
                        onClick = onNavigateToCustomers
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsMenuItem(
                        icon = Icons.Default.Agriculture,
                        title = Strings.get("my_vehicles", lang),
                        subtitle = "ट्रैक्टर व गाड़ी विवरण",
                        onClick = onNavigateToVehicles
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsMenuItem(
                        icon = Icons.Default.Handyman,
                        title = Strings.get("my_implements", lang),
                        subtitle = "रोटावेटर, कल्टीवेटर व दरें",
                        onClick = onNavigateToImplements
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsMenuItem(
                        icon = Icons.Default.Info,
                        title = "सहायता और सपोर्ट (Help & Support)",
                        subtitle = "व्हाट्सएप, कॉल, शिकायत दर्ज करें व FAQs",
                        onClick = onNavigateToHelpSupport
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsMenuItem(
                        icon = Icons.Default.EmojiEvents,
                        title = "उपलब्धियां (Achievements)",
                        subtitle = "किसान और कमाई के मेडल व उपलब्धियां देखें",
                        onClick = onNavigateToAchievements
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsMenuItem(
                        icon = Icons.Default.Schedule,
                        title = "बाकी रिमाइंडर (Reminders)",
                        subtitle = "सक्रिय तकादे, तारीख व अंतराल सेटिंग्स",
                        onClick = onNavigateToReminders
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "सूचना केंद्र (Notifications)",
                        subtitle = "सभी तकादे, बधाई और अलर्ट संदेश",
                        onClick = onNavigateToNotifications
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsMenuItem(
                        icon = Icons.Default.Tune,
                        title = "सूचना सेटिंग (Notification Settings)",
                        subtitle = "समय, शांत घंटे, तकादा अंतराल व मास्टर स्विच",
                        onClick = onNavigateToNotificationSettings
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsMenuItem(
                        icon = Icons.Default.Refresh,
                        title = "ऐप मार्गदर्शन (App Guide)",
                        subtitle = "फीचर्स और उपयोग का गाइडेड टूर देखें",
                        onClick = onLaunchTour
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section: Preferences & Voice
        item {
            KhetCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("सुविधाएं व भाषा (Preferences)", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Language Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = ForestGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(Strings.get("language", lang), fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                Text(if (lang == "hi") "वर्तमान: हिन्दी" else "Current: English", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        Button(
                            onClick = { viewModel.setLanguage(if (lang == "hi") "en" else "hi") },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenLight),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(if (lang == "hi") "Switch to EN" else "हिन्दी में बदलें", color = ForestGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsMenuItem(
                        icon = Icons.Default.Mic,
                        title = "आवाज से बोलने के टिप्स (Voice Tips)",
                        subtitle = "हिंदी व अंग्रेजी संख्याएं और कमांड",
                        onClick = { showVoiceTipsDialog = true }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section: Demo Data & Backup
        item {
            KhetCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("डेटा एवं बैकअप (Data & Tools)", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Cloud Sync Status Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (syncStatus) {
                            com.example.data.SyncStatus.SYNCED -> MintTint
                            com.example.data.SyncStatus.SYNCING -> AmberLight
                            else -> WarmCreamBg
                        },
                        border = BorderStroke(1.dp, BorderNeutral),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (syncStatus) {
                                                    com.example.data.SyncStatus.SYNCED -> PaidGreen
                                                    com.example.data.SyncStatus.SYNCING -> AmberGold
                                                    else -> TextMuted
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (syncStatus) {
                                            com.example.data.SyncStatus.SYNCED -> "सुरक्षित (Synced)"
                                            com.example.data.SyncStatus.SYNCING -> "सिंक हो रहा है..."
                                            com.example.data.SyncStatus.WAITING_INTERNET -> "इंटरनेट की प्रतीक्षा"
                                            com.example.data.SyncStatus.ERROR -> "सिंक त्रुटि (Retry)"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "क्लाउड बैकअप सक्रिय • 100% सुरक्षित",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            Button(
                                onClick = { viewModel.triggerCloudSync() },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("अभी सिंक करें", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Seed Demo Data
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DataObject, contentDescription = null, tint = AmberGold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(Strings.get("seed_demo", lang), fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                Text("पूर्ण टेस्ट डेटा (काम, खर्च व किसान)", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        Button(
                            onClick = {
                                viewModel.seedDemoData()
                                showSeedSuccessDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("लोड करें", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reset Data
                    SettingsMenuItem(
                        icon = Icons.Default.Refresh,
                        title = Strings.get("reset_data", lang),
                        subtitle = "सभी पुराने काम व खर्च मिटाएं",
                        onClick = { showResetConfirmDialog = true },
                        tint = DueRed
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // System Diagnostics & Error Log
                    SettingsMenuItem(
                        icon = Icons.Default.Info,
                        title = if (lang == "hi") "सिस्टम डायग्नोस्टिक्स एवं एरर लॉग" else "System Diagnostics & Error Log",
                        subtitle = if (lang == "hi") "रैम हीप, डेटाबेस स्टेटस व क्रैश लॉग्स" else "RAM heap, database stats & error logs",
                        onClick = onNavigateToDiagnostics,
                        tint = ForestGreen
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // App Information & Share
        item {
            KhetCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("खेत साथी (KhetSaathi)", fontWeight = FontWeight.Bold, color = ForestGreen, fontSize = 16.sp)
                            Text("संस्करण 1.0 (ऑफलाइन फर्स्ट)", fontSize = 12.sp, color = TextMuted)
                        }
                        IconButton(onClick = {
                            val msg = "ट्रैक्टर का पूरा हिसाब रखने के लिए 'खेत साथी' ऐप अपनाएं!"
                            ShareHelper.shareText(context, msg)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = ForestGreen)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    // Modal Voice Tips Dialog
    if (showVoiceTipsDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceTipsDialog = false },
            title = {
                Text("🗣️ बोलकर दर्ज करने के नियम", fontWeight = FontWeight.Bold, color = ForestGreen)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("खेत साथी में आप हिंदी और अंग्रेजी दोनों में बोल सकते हैं:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• रोटावेटर दो घंटा बीस मिनट -> समय व यंत्र दर्ज होगा")
                    Text("• कल्टीवेटर एक घंटा -> कल्टीवेटर 1 घंटा")
                    Text("• रमेश ने 2000 रुपये दिए नकद -> भुगतान ड्राफ्ट")
                    Text("• ट्राली तीन चक्कर -> ढुलाई 3 ट्रिप")
                    Text("• संख्याएं जैसे डेढ़ (1.5), ढाई (2.5), साढ़े तीन (3.5) स्वतः समझता है!")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showVoiceTipsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("समझ गया", color = Color.White)
                }
            }
        )
    }

    // Modal Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text("डेटा रीसेट की पुष्टि", fontWeight = FontWeight.Bold, color = DueRed)
            },
            text = {
                Text("क्या आप वाकई सभी दर्ज किए गए काम और खर्चों को हटाना चाहते हैं? यह क्रिया पूर्ववत नहीं की जा सकती।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed)
                ) {
                    Text("हां, सब हटाएं", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("रद्द करें", color = TextPrimary)
                }
            }
        )
    }

    // Modal Seed Demo Success Dialog
    if (showSeedSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSeedSuccessDialog = false },
            title = {
                Text("✅ डेमो डेटा लोड हो गया!", fontWeight = FontWeight.Bold, color = ForestGreen)
            },
            text = {
                Text("5 किसान, 7 काम, और 5 डीजल खर्च सफलतापूर्वक जोड़ दिए गए हैं। आप होम, जॉब्स और रिपोर्ट्स में पूरा हिसाब देख सकते हैं।")
            },
            confirmButton = {
                Button(
                    onClick = { showSeedSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("ठीक है", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = ForestGreen
) {
    Surface(
        color = CreamBackground,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = tint)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text(subtitle, fontSize = 12.sp, color = TextMuted)
                }
            }
            Text(">", color = TextMuted, fontWeight = FontWeight.Bold)
        }
    }
}
