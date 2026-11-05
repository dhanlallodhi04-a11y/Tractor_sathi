package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.NotificationPrefsEntity
import com.example.notification.NotificationService
import com.example.ui.KhetSaathiViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BorderDarkEdge
import com.example.ui.theme.BorderNeutral
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamYellowBorder
import com.example.ui.theme.CreamYellowTint
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintTint
import com.example.ui.theme.MoneyDueLightTint
import com.example.ui.theme.MoneyDueRed
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.MoneyInLightTint
import com.example.ui.theme.SkyTint
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub
import com.example.ui.theme.WarmCreamBg
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: KhetSaathiViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs by viewModel.notificationPrefs.collectAsStateWithLifecycle()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    val currentPrefs = prefs ?: NotificationPrefsEntity()
    var isEnabled = NotificationService.areNotificationsEnabled(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (lang == "hi") "सूचना सेटिंग (Notifications)" else "Notification Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DeepForestGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DeepForestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmCreamBg)
            )
        },
        containerColor = WarmCreamBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Permission Status Banner
            if (!isEnabled) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MoneyDueLightTint,
                    border = BorderStroke(1.5.dp, MoneyDueRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MoneyDueRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "सिस्टम नोटिफिकेशन बंद हैं",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MoneyDueRed
                            )
                            Text(
                                text = "समय पर बाकी और काम का अलर्ट पाने के लिए सेटिंग्स में सूचनाएं चालू करें।",
                                fontSize = 12.sp,
                                color = TextMain
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                } else {
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MoneyDueRed),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text("चालू करें", fontSize = 12.sp, color = CardWhite)
                        }
                    }
                }
            }

            // 2. Master Switch Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = BorderDarkEdge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                        .background(CardWhite, RoundedCornerShape(18.dp))
                        .border(BorderStroke(1.dp, BorderNeutral), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = MintTint,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = DeepForestGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "सभी सूचनाएं (Master Switch)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DeepForestGreen
                                )
                                Text(
                                    text = "तकादे, बधाई और काम की समय पर याद",
                                    fontSize = 12.sp,
                                    color = TextSub
                                )
                            }
                        }
                        Switch(
                            checked = currentPrefs.masterEnabled,
                            onCheckedChange = { checked ->
                                viewModel.updateNotificationPrefs(currentPrefs.copy(masterEnabled = checked))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CardWhite,
                                checkedTrackColor = DeepForestGreen
                            ),
                            modifier = Modifier.testTag("notification_master_switch")
                        )
                    }
                }
            }

            // 3. Category Toggles
            Text(
                text = "सूचनाओं के प्रकार (Notification Types)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepForestGreen
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = BorderDarkEdge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                        .background(CardWhite, RoundedCornerShape(18.dp))
                        .border(BorderStroke(1.dp, BorderNeutral), RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ToggleRow(
                            title = "बाकी वसूली रिमाइंडर",
                            desc = "तारीख आने पर और हर 5 दिन में किसान से बाकी की याद",
                            checked = currentPrefs.paymentRemindersEnabled,
                            onCheckedChange = { checked ->
                                viewModel.updateNotificationPrefs(currentPrefs.copy(paymentRemindersEnabled = checked))
                            }
                        )
                        ToggleRow(
                            title = "बधाई एवं उपलब्धियां",
                            desc = "किसान संख्या और कमाई के नए मुकाम पर सम्मान",
                            checked = currentPrefs.milestonesEnabled,
                            onCheckedChange = { checked ->
                                viewModel.updateNotificationPrefs(currentPrefs.copy(milestonesEnabled = checked))
                            }
                        )
                        ToggleRow(
                            title = "मासिक बाकी सारांश (1 तारीख)",
                            desc = "हर महीने की शुरुआत में कुल बाकी की रिपोर्ट",
                            checked = currentPrefs.monthlySummaryEnabled,
                            onCheckedChange = { checked ->
                                viewModel.updateNotificationPrefs(currentPrefs.copy(monthlySummaryEnabled = checked))
                            }
                        )
                        ToggleRow(
                            title = "साप्ताहिक रिपोर्ट (रविवार शाम)",
                            desc = "हफ्तेभर की कमाई, खर्च और मुनाफा सारांश",
                            checked = currentPrefs.weeklySummaryEnabled,
                            onCheckedChange = { checked ->
                                viewModel.updateNotificationPrefs(currentPrefs.copy(weeklySummaryEnabled = checked))
                            }
                        )
                        ToggleRow(
                            title = "काम और शाम का हिसाब",
                            desc = "आगामी काम का समय और रात 8 बजे काम दर्ज करने का अलर्ट",
                            checked = currentPrefs.jobRemindersEnabled,
                            onCheckedChange = { checked ->
                                viewModel.updateNotificationPrefs(currentPrefs.copy(jobRemindersEnabled = checked))
                            }
                        )
                        ToggleRow(
                            title = "ट्रैक्टर सर्विस व दस्तावेज़",
                            desc = "250 घंटे होने पर सर्विस व बीमा/फिटनेस खत्म होने की चेतावनी",
                            checked = currentPrefs.vehicleServiceEnabled,
                            onCheckedChange = { checked ->
                                viewModel.updateNotificationPrefs(currentPrefs.copy(vehicleServiceEnabled = checked))
                            }
                        )
                        ToggleRow(
                            title = "बैकअप और सुरक्षा अलर्ट",
                            desc = "क्लाउड सिंक रुकने पर सतर्कता संदेश",
                            checked = currentPrefs.backupAlertsEnabled,
                            onCheckedChange = { checked ->
                                viewModel.updateNotificationPrefs(currentPrefs.copy(backupAlertsEnabled = checked))
                            }
                        )
                    }
                }
            }

            // 4. Default Repeat Days
            Text(
                text = "बाकी याद दिलाने का अंतराल (Repeat Interval)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepForestGreen
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(3, 5, 7, 10).forEach { days ->
                    val isSelected = currentPrefs.defaultRepeatDays == days
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (isSelected) DeepForestGreen else CardWhite,
                        border = BorderStroke(1.dp, if (isSelected) DeepForestGreen else BorderNeutral),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable {
                                viewModel.updateNotificationPrefs(currentPrefs.copy(defaultRepeatDays = days))
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "हर $days दिन",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) CardWhite else TextMain
                            )
                        }
                    }
                }
            }

            // 5. Test Notification Button
            Button(
                onClick = {
                    scope.launch {
                        viewModel.sendTestNotification()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_test_notification_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = CardWhite, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "टेस्ट नोटिफिकेशन भेजें (Send Test)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CardWhite
                )
            }

            // 6. Battery Saver / Autostart Note
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CreamYellowTint,
                border = BorderStroke(1.dp, CreamYellowBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = AmberDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "बैटरी सेवर सुझाव (Xiaomi / Vivo / Oppo / Realme)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DeepForestGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "कुछ फोन बैटरी सेवर के कारण बैकग्राउंड रिमाइंडर रोक देते हैं। समय पर सूचना पाने के लिए फोन सेटिंग्स में 'Autostart' चालू रखें और बैटरी को 'No restrictions' पर सेट करें।",
                            fontSize = 12.sp,
                            color = TextMain,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextMain
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = TextSub
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CardWhite,
                checkedTrackColor = DeepForestGreen
            )
        )
    }
}
