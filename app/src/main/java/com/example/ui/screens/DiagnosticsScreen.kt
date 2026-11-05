package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.ui.KhetSaathiViewModel
import com.example.ui.components.KhetCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberLight
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.DueRed
import com.example.ui.theme.DueRedLight
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DiagnosticsLogEntry
import com.example.util.DiagnosticsManager
import com.example.util.LogLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    viewModel: KhetSaathiViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var logs by remember { mutableStateOf(DiagnosticsManager.getLogs()) }
    val dateFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }

    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val implements by viewModel.implements.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    val runtime = remember { Runtime.getRuntime() }
    val maxHeap = runtime.maxMemory() / (1024 * 1024)
    val totalHeap = runtime.totalMemory() / (1024 * 1024)
    val freeHeap = runtime.freeMemory() / (1024 * 1024)
    val usedHeap = totalHeap - freeHeap

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "सिस्टम डायग्नोस्टिक्स (Diagnostics)",
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ForestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // System & Memory Specs Card
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = ForestGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "डिवाइस मेमोरी एवं हीप (Memory & Specs)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("अधिकतम JVM हीप (Max Heap):", fontSize = 13.sp, color = TextMuted)
                            Text("${maxHeap} MB", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("उपयोग किया गया (Used / Total):", fontSize = 13.sp, color = TextMuted)
                            Text("${usedHeap} MB / ${totalHeap} MB", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("हार्डवेयर त्वरण (Hardware Accel):", fontSize = 13.sp, color = TextMuted)
                            Text("सक्रिय (Active)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PaidGreen)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("लार्ज हीप (Large Heap):", fontSize = 13.sp, color = TextMuted)
                            Text("सक्षम (Enabled)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PaidGreen)
                        }
                    }
                }
            }

            // Database Statistics Card
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = AmberGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "स्थानीय डेटाबेस स्थिति (Local DB)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("कुल दर्ज काम (Total Jobs):", fontSize = 13.sp, color = TextMuted)
                            Text("${jobs.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("कुल किसान (Total Kisans):", fontSize = 13.sp, color = TextMuted)
                            Text("${customers.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("कुल यंत्र (Implements):", fontSize = 13.sp, color = TextMuted)
                            Text("${implements.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("कुल खर्च प्रविष्टियां (Expenses):", fontSize = 13.sp, color = TextMuted)
                            Text("${expenses.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }

            // Quick Actions: Copy and Clear
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            DiagnosticsManager.copyToClipboard(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("copy_diagnostics_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("कॉपी रिपोर्ट (Copy)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            DiagnosticsManager.clearLogs(context)
                            logs = DiagnosticsManager.getLogs()
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("clear_logs_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = DueRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("साफ़ करें", color = DueRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Log Header
            item {
                Text(
                    text = "त्रुटि व सिस्टम लॉग (${logs.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Log Items
            if (logs.isEmpty()) {
                item {
                    KhetCard {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = PaidGreen, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "कोई त्रुटि दर्ज नहीं हुई (No errors recorded)",
                                    fontSize = 14.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            } else {
                items(logs, key = { it.id }) { logItem ->
                    LogItemCard(logItem = logItem, dateFormatter = dateFormatter)
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun LogItemCard(
    logItem: DiagnosticsLogEntry,
    dateFormatter: SimpleDateFormat
) {
    var expanded by remember { mutableStateOf(false) }

    val (badgeBg, badgeColor) = when (logItem.level) {
        LogLevel.CRASH -> DueRedLight to DueRed
        LogLevel.ERROR -> DueRedLight to DueRed
        LogLevel.WARN -> AmberLight to AmberGold
        LogLevel.INFO -> PaidGreenLight to PaidGreen
    }

    KhetCard(onClick = { if (!logItem.stackTrace.isNullOrBlank()) expanded = !expanded }) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = logItem.level.name,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = dateFormatter.format(Date(logItem.timestamp)),
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${logItem.tag}: ${logItem.message}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            if (!logItem.stackTrace.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (expanded) "विवरण छिपाएं ▲" else "स्टैक ट्रेस देखें ▼",
                    fontSize = 11.sp,
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold
                )

                if (expanded) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = CreamBackground,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = logItem.stackTrace,
                            style = MaterialTheme.typography.labelSmall,
                            color = DueRed,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
