package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CustomerEntity
import com.example.data.ImplementEntity
import com.example.data.JobEntity
import com.example.data.SyncStatus
import com.example.ui.KhetSaathiViewModel
import com.example.ui.components.KhetCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.wavyFieldTexture
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BlushPinkBorder
import com.example.ui.theme.BlushPinkEdge
import com.example.ui.theme.BlushPinkTint
import com.example.ui.theme.BorderDarkEdge
import com.example.ui.theme.BorderNeutral
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamYellowBorder
import com.example.ui.theme.CreamYellowEdge
import com.example.ui.theme.CreamYellowTint
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintEdge
import com.example.ui.theme.MintTint
import com.example.ui.theme.MoneyDueDarkEdge
import com.example.ui.theme.MoneyDueLightTint
import com.example.ui.theme.MoneyDueRed
import com.example.ui.theme.MoneyInDarkEdge
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.MoneyInLightTint
import com.example.ui.theme.SkyBorder
import com.example.ui.theme.SkyEdge
import com.example.ui.theme.SkyTint
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSub
import com.example.ui.theme.WarmCreamBg
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

enum class SummaryPeriod(val labelHi: String, val labelEn: String) {
    ALL("सब", "All"),
    TODAY("आज", "Today"),
    WEEK("हफ्ता", "Week"),
    MONTH("महीना", "Month"),
    YEAR("साल", "Year")
}

// Indian Currency Formatter (e.g., 1,20,000)
fun formatIndianNumber(amount: Double): String {
    val num = amount.toLong()
    val isNegative = num < 0
    val absVal = abs(num)
    val s = absVal.toString()
    if (s.length <= 3) return (if (isNegative) "-" else "") + s
    val lastThree = s.substring(s.length - 3)
    val otherNumbers = s.substring(0, s.length - 3)
    val formattedOther = StringBuilder()
    for (i in otherNumbers.indices) {
        if (i > 0 && (otherNumbers.length - i) % 2 == 0) {
            formattedOther.append(",")
        }
        formattedOther.append(otherNumbers[i])
    }
    return (if (isNegative) "-" else "") + formattedOther.toString() + "," + lastThree
}

// Subtle repeating diagonal furrow pattern + soft glow for dark green hero card
fun Modifier.furrowTexture(): Modifier = this.drawWithContent {
    drawContent()
    // Soft top-right lighter glow
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
            center = Offset(size.width, 0f),
            radius = size.width * 0.75f
        )
    )
    // Diagonal furrow lines (14px period, stripes of 5% white)
    val period = 14.dp.toPx()
    val strokeWidth = 1.dp.toPx()
    val lineCol = Color.White.copy(alpha = 0.05f)
    var offset = -size.height
    while (offset < size.width + size.height) {
        drawLine(
            color = lineCol,
            start = Offset(offset, 0f),
            end = Offset(offset + size.height, size.height),
            strokeWidth = strokeWidth
        )
        offset += period
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: KhetSaathiViewModel,
    onNavigateToAddJob: () -> Unit,
    onNavigateToJobs: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToReports: () -> Unit = {},
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToVehicles: () -> Unit = {},
    onNavigateToImplements: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onSelectJob: (JobEntity) -> Unit
) {
    val context = LocalContext.current
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val selectedVehicleId by viewModel.selectedVehicleId.collectAsStateWithLifecycle()
    val implements by viewModel.implements.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val allJobs by viewModel.jobs.collectAsStateWithLifecycle()
    val allExpenses by viewModel.expenses.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val unreadNotifsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    var selectedPeriod by remember { mutableStateOf(SummaryPeriod.ALL) }
    var showVehicleSheet by remember { mutableStateOf(false) }
    var showCalculatorSheet by remember { mutableStateOf(false) }
    var showAlertsSheet by remember { mutableStateOf(false) }
    var showBakiSheet by remember { mutableStateOf(false) }

    val selectedVehicle = remember(vehicles, selectedVehicleId) {
        vehicles.find { it.id == selectedVehicleId }
    }

    // Filtered jobs and expenses based on selected vehicle and period
    val periodFilteredData = remember(allJobs, allExpenses, selectedVehicleId, selectedPeriod) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        val periodStartMillis: Long = when (selectedPeriod) {
            SummaryPeriod.ALL -> 0L
            SummaryPeriod.TODAY -> {
                cal.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            SummaryPeriod.WEEK -> now - (7L * 24 * 60 * 60 * 1000)
            SummaryPeriod.MONTH -> now - (30L * 24 * 60 * 60 * 1000)
            SummaryPeriod.YEAR -> now - (365L * 24 * 60 * 60 * 1000)
        }

        val filteredJobs = allJobs.filter { job ->
            val matchVehicle = selectedVehicleId == null || job.vehicleId == selectedVehicleId
            val matchPeriod = job.dateMillis >= periodStartMillis
            matchVehicle && matchPeriod
        }

        val filteredExpenses = allExpenses.filter { expense ->
            val matchVehicle = selectedVehicleId == null || expense.vehicleId == selectedVehicleId || expense.vehicleId == null
            val matchPeriod = expense.dateMillis >= periodStartMillis
            matchVehicle && matchPeriod
        }

        var totalBilled = 0.0
        var totalReceived = 0.0
        var totalHours = 0.0
        var totalArea = 0.0

        for (job in filteredJobs) {
            totalBilled += job.total
            totalReceived += job.paidAmount
            totalHours += (job.hours + (job.minutes / 60.0))
            totalArea += job.area
        }

        var totalDieselLitres = 0.0
        var totalExpenseAmount = 0.0

        for (exp in filteredExpenses) {
            totalExpenseAmount += exp.amount
            totalDieselLitres += exp.litres
        }

        val totalBaki = (totalBilled - totalReceived).coerceAtLeast(0.0)
        val netProfit = totalBilled - totalExpenseAmount

        object {
            val jobs = filteredJobs
            val billed = totalBilled
            val received = totalReceived
            val baki = totalBaki
            val hours = totalHours
            val area = totalArea
            val diesel = totalDieselLitres
            val expenses = totalExpenseAmount
            val profit = netProfit
        }
    }

    // Recent 10 jobs for the selected vehicle (memoized)
    val recentJobs = remember(allJobs, selectedVehicleId) {
        val list = if (selectedVehicleId != null) {
            allJobs.filter { it.vehicleId == selectedVehicleId }
        } else {
            allJobs
        }
        list.take(10)
    }

    // Pending baki alerts
    val bakiCustomers = remember(customers, allJobs) {
        val dueMap = mutableMapOf<String, Double>()
        for (job in allJobs) {
            val due = (job.total - job.paidAmount).coerceAtLeast(0.0)
            if (due > 0) {
                dueMap[job.customerId] = (dueMap[job.customerId] ?: 0.0) + due
            }
        }
        customers.mapNotNull { customer ->
            val due = dueMap[customer.id] ?: 0.0
            if (due > 0.0) Pair(customer, due) else null
        }.sortedByDescending { it.second }
    }

    val totalPendingBakiOverall = remember(bakiCustomers) {
        bakiCustomers.sumOf { it.second }
    }

    val hasAlerts = remember(bakiCustomers) {
        bakiCustomers.isNotEmpty()
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale("hi", "IN")) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // ==========================================
            // 1. TOP BAR (Height 52dp, top gap 12dp, bottom gap 12dp, name on ONE line)
            // ==========================================
            item {
                Surface(
                    color = WarmCreamBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Logo (36px circle) + App Name on ONE line
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = DeepForestGreen,
                                    border = BorderStroke(2.dp, CardWhite),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Agriculture,
                                            contentDescription = "Kisan Saathi Logo",
                                            tint = CardWhite,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (lang == "hi") "किसान साथी" else "Kisan Saathi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = DeepForestGreen,
                                    letterSpacing = (-0.5).sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Right: Sync Dot + Notification Bell (36px) + Profile Avatar (36px)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Sync indicator dot
                                val (syncDotColor, syncLabel) = when (syncStatus) {
                                    SyncStatus.SYNCED -> Pair(MoneyInGreen, if (lang == "hi") "सुरक्षित (Synced)" else "Synced")
                                    SyncStatus.SYNCING -> Pair(AmberAccent, if (lang == "hi") "सिंक हो रहा है..." else "Syncing...")
                                    SyncStatus.WAITING_INTERNET, SyncStatus.ERROR -> Pair(Color(0xFF9E9E9E), if (lang == "hi") "इंटरनेट की प्रतीक्षा" else "Offline")
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(syncDotColor, CircleShape)
                                        .border(BorderStroke(1.dp, CardWhite), CircleShape)
                                        .testTag("sync_status_dot")
                                )

                                // Notification Bell
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable { onNavigateToNotifications() }
                                        .testTag("notification_bell"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = DeepForestGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (unreadNotifsCount > 0 || hasAlerts) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MoneyDueRed,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(if (unreadNotifsCount > 0) 16.dp else 9.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (unreadNotifsCount > 0) {
                                                    Text(
                                                        text = if (unreadNotifsCount > 99) "99+" else unreadNotifsCount.toString(),
                                                        color = CardWhite,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Profile Avatar with deep green ring
                                Surface(
                                    shape = CircleShape,
                                    color = CreamYellowTint,
                                    border = BorderStroke(2.dp, DeepForestGreen),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable(onClick = onNavigateToProfile)
                                        .testTag("top_bar_profile_avatar")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val initial = user?.name?.trim()?.take(1)?.uppercase() ?: "K"
                                        Text(
                                            text = initial,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = DeepForestGreen
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // ==========================================
            // 2. VEHICLE ROW (Raised white cards, 12px gap below)
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left (Flex): 3D Raised pill-shaped vehicle dropdown
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = BorderDarkEdge,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable { showVehicleSheet = true }
                            .testTag("vehicle_selector_dropdown")
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 3.dp)
                                .background(CardWhite, RoundedCornerShape(24.dp))
                                .border(BorderStroke(1.5.dp, BorderNeutral), RoundedCornerShape(24.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MintTint,
                                        border = BorderStroke(1.dp, MintBorder),
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Agriculture,
                                                contentDescription = null,
                                                tint = DeepForestGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedVehicle?.name ?: if (lang == "hi") "सभी ट्रैक्टर" else "All Tractors",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextMain,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Vehicle",
                                    tint = DeepForestGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Right: 3D Raised button with calculator icon (label "कैलकुलेटर")
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BorderDarkEdge,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .height(48.dp)
                            .clickable { showCalculatorSheet = true }
                            .testTag("calculator_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 3.dp)
                                .background(CardWhite, RoundedCornerShape(14.dp))
                                .border(BorderStroke(1.5.dp, BorderNeutral), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Calculator",
                                    tint = DeepForestGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lang == "hi") "कैलकुलेटर" else "Calculator",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextMain
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ==========================================
            // 3. "हिसाब" SECTION (Deep Forest Green with Furrow Texture & 3D Edge)
            // ==========================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = if (lang == "hi") "हिसाब" else "Ledger",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepForestGreen,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Hero 3D Raised Card (Deep Forest Green + Furrow Texture)
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = ForestGreenDark,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("summary_card")
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .background(DeepForestGreen, RoundedCornerShape(24.dp))
                                .border(BorderStroke(1.5.dp, ForestGreenDark), RoundedCornerShape(24.dp))
                                .furrowTexture()
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Top Row: Label "कुल किया गया काम"
                                Text(
                                    text = if (lang == "hi") "कुल किया गया काम" else "Total Work Billed",
                                    fontSize = 13.sp,
                                    color = CardWhite.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Period Filter Chips (Unselected = 15% white, Selected = Amber #D99A1F)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SummaryPeriod.values().forEach { period ->
                                        val isSelected = selectedPeriod == period
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = if (isSelected) AmberDark else CardWhite.copy(alpha = 0.15f),
                                            modifier = Modifier
                                                .clickable { selectedPeriod = period }
                                                .testTag("period_chip_${period.name.lowercase()}")
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(bottom = if (isSelected) 2.5.dp else 0.dp)
                                                    .background(
                                                        if (isSelected) AmberAccent else CardWhite.copy(alpha = 0.15f),
                                                        RoundedCornerShape(999.dp)
                                                    )
                                                    .border(
                                                        BorderStroke(1.dp, if (isSelected) AmberDark else Color.Transparent),
                                                        RoundedCornerShape(999.dp)
                                                    )
                                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = if (lang == "hi") period.labelHi else period.labelEn,
                                                    color = CardWhite,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Big Amount (White, 40px bold)
                                AnimatedContent(
                                    targetState = periodFilteredData.billed,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                                    },
                                    label = "amount_anim"
                                ) { billedVal ->
                                    Text(
                                        text = "₹" + formatIndianNumber(billedVal),
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CardWhite,
                                        letterSpacing = (-0.5).sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Two 3D Money cards side by side (with faint rupee watermark)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 3D Money In Card "मिला पैसा"
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MoneyInDarkEdge,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(bottom = 3.dp)
                                                .background(MoneyInGreen, RoundedCornerShape(16.dp))
                                                .border(BorderStroke(1.dp, MoneyInDarkEdge), RoundedCornerShape(16.dp))
                                                .padding(12.dp)
                                        ) {
                                            // Rupee Watermark (8% white)
                                            Text(
                                                text = "₹",
                                                fontSize = 54.sp,
                                                fontWeight = FontWeight.Black,
                                                color = CardWhite.copy(alpha = 0.08f),
                                                modifier = Modifier.align(Alignment.BottomEnd)
                                            )
                                            Column {
                                                Text(
                                                    text = if (lang == "hi") "मिला पैसा" else "Collected",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = CardWhite.copy(alpha = 0.9f)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "₹" + formatIndianNumber(periodFilteredData.received),
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = CardWhite,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    // 3D Money Due Card "बाकी है पैसा" (Tap opens Kisan baki list)
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MoneyDueDarkEdge,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showBakiSheet = true }
                                            .testTag("baki_money_card")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(bottom = 3.dp)
                                                .background(MoneyDueRed, RoundedCornerShape(16.dp))
                                                .border(BorderStroke(1.dp, MoneyDueDarkEdge), RoundedCornerShape(16.dp))
                                                .padding(12.dp)
                                        ) {
                                            // Rupee Watermark (8% white)
                                            Text(
                                                text = "₹",
                                                fontSize = 54.sp,
                                                fontWeight = FontWeight.Black,
                                                color = CardWhite.copy(alpha = 0.08f),
                                                modifier = Modifier.align(Alignment.BottomEnd)
                                            )
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = if (lang == "hi") "बाकी है पैसा" else "Due (Baki)",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = CardWhite.copy(alpha = 0.9f)
                                                    )
                                                    Text(
                                                        text = "›",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = CardWhite
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "₹" + formatIndianNumber(periodFilteredData.baki),
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = CardWhite,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // 5. NEW: Full-width "शुद्ध मुनाफा" (Net Profit) 3D Raised Panel
                                val isProfitPositive = periodFilteredData.profit > 0
                                val isProfitNegative = periodFilteredData.profit < 0

                                val profitPanelBg = if (isProfitNegative) Color(0xFFFDE6E4) else Color(0xFFFBEFCB)
                                val profitPanelBorder = if (isProfitNegative) Color(0xFFEDBBC8) else Color(0xFFEBD9A0)
                                val profitPanelEdge = if (isProfitNegative) Color(0xFFDCA3B2) else Color(0xFFD9BE70)
                                val profitAmountColor = when {
                                    isProfitPositive -> Color(0xFF1F6B40)
                                    isProfitNegative -> Color(0xFFD64545)
                                    else -> Color(0xFF4A4A4A)
                                }
                                val profitIconBorder = if (isProfitNegative) Color(0xFFD64545) else DeepForestGreen
                                val profitIconTint = if (isProfitNegative) Color(0xFFD64545) else Color(0xFF1F6B40)

                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = profitPanelEdge,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 72.dp)
                                        .clickable(onClick = onNavigateToReports)
                                        .testTag("net_profit_panel")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(bottom = 4.dp)
                                            .background(profitPanelBg, RoundedCornerShape(18.dp))
                                            .border(BorderStroke(1.5.dp, profitPanelBorder), RoundedCornerShape(18.dp))
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f, fill = false)
                                            ) {
                                                // Round white icon badge with green/red border
                                                Surface(
                                                    shape = CircleShape,
                                                    color = CardWhite,
                                                    border = BorderStroke(1.5.dp, profitIconBorder),
                                                    modifier = Modifier.size(38.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = if (isProfitNegative) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                                            contentDescription = "Net Profit Icon",
                                                            tint = profitIconTint,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column {
                                                    Text(
                                                        text = if (lang == "hi") "शुद्ध मुनाफा" else "Net Profit",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = DeepForestGreen,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = if (lang == "hi") "कमाई - खर्च" else "Income - Expenses",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF6B6B6B),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            val formattedProfit = when {
                                                isProfitPositive -> "₹" + formatIndianNumber(periodFilteredData.profit)
                                                isProfitNegative -> "-₹" + formatIndianNumber(abs(periodFilteredData.profit))
                                                else -> "₹0"
                                            }

                                            Text(
                                                text = formattedProfit,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = profitAmountColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ==========================================
            // 4. "आंकड़े" CARD (4 Distinct Pastel Tints with Wavy Field Patterns)
            // ==========================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = if (lang == "hi") "आंकड़े" else "Key Stats",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepForestGreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // 2x2 Grid of 3D Raised Pastel Modules
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Row 1: किसान (Mint) & काम (Cream-Yellow)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PastelStatTile(
                                modifier = Modifier.weight(1f),
                                label = if (lang == "hi") "किसान" else "Kisan",
                                count = customers.size.toString(),
                                backgroundColor = MintTint,
                                borderColor = MintBorder,
                                bottomEdgeColor = MintEdge,
                                icon = Icons.Default.People,
                                onClick = onNavigateToCustomers
                            )
                            PastelStatTile(
                                modifier = Modifier.weight(1f),
                                label = if (lang == "hi") "काम" else "Jobs",
                                count = periodFilteredData.jobs.size.toString(),
                                backgroundColor = CreamYellowTint,
                                borderColor = CreamYellowBorder,
                                bottomEdgeColor = CreamYellowEdge,
                                icon = Icons.Default.Assignment,
                                onClick = onNavigateToJobs
                            )
                        }

                        // Row 2: ट्रैक्टर (Sky) & इम्प्लीमेंट (Blush-Pink)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PastelStatTile(
                                modifier = Modifier.weight(1f),
                                label = if (lang == "hi") "ट्रैक्टर" else "Vehicles",
                                count = vehicles.size.toString(),
                                backgroundColor = SkyTint,
                                borderColor = SkyBorder,
                                bottomEdgeColor = SkyEdge,
                                icon = Icons.Default.Agriculture,
                                onClick = onNavigateToVehicles
                            )
                            PastelStatTile(
                                modifier = Modifier.weight(1f),
                                label = if (lang == "hi") "इम्प्लीमेंट" else "Implements",
                                count = implements.size.toString(),
                                backgroundColor = BlushPinkTint,
                                borderColor = BlushPinkBorder,
                                bottomEdgeColor = BlushPinkEdge,
                                icon = Icons.Default.Build,
                                onClick = onNavigateToImplements
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // ==========================================
            // 5. QUICK INSIGHT TILES (3 Tiles in ONE Line + Slim Total Expense Row)
            // ==========================================
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (lang == "hi") "झलकियाँ" else "Quick Insights",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepForestGreen,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Exactly 3 tiles in a single row (No horizontal scroll, 1fr each)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. घंटे चले (Hours worked)
                        val totalHoursWhole = periodFilteredData.hours.toInt()
                        val totalMinutesPart = ((periodFilteredData.hours - totalHoursWhole) * 60).toInt()
                        val hoursStr = if (periodFilteredData.hours > 0) {
                            if (totalMinutesPart > 0) "${totalHoursWhole} घं ${totalMinutesPart} मि" else "${totalHoursWhole} घं"
                        } else "0 घं"

                        CompactInsightTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.AccessTime,
                            iconTint = DeepForestGreen,
                            badgeBg = MintTint,
                            badgeBorder = MintBorder,
                            label = if (lang == "hi") "घंटे चले" else "Hours Run",
                            value = hoursStr,
                            onClick = onNavigateToReports
                        )

                        // 2. डीज़ल (ली.) (Diesel litres)
                        val dieselStr = if (periodFilteredData.diesel > 0) {
                            if (periodFilteredData.diesel % 1.0 == 0.0) "${periodFilteredData.diesel.toInt()} ली."
                            else "${String.format(Locale.US, "%.1f", periodFilteredData.diesel)} ली."
                        } else "0 ली."

                        CompactInsightTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.LocalGasStation,
                            iconTint = DeepForestGreen,
                            badgeBg = CreamYellowTint,
                            badgeBorder = CreamYellowBorder,
                            label = if (lang == "hi") "डीज़ल (ली.)" else "Diesel (L)",
                            value = dieselStr,
                            onClick = onNavigateToExpenses
                        )

                        // 3. एकड़ काम (Acres worked)
                        val areaStr = if (periodFilteredData.area > 0) {
                            "${String.format(Locale.US, "%.1f", periodFilteredData.area)} एकड़"
                        } else "0 एकड़"

                        CompactInsightTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.SquareFoot,
                            iconTint = DeepForestGreen,
                            badgeBg = SkyTint,
                            badgeBorder = SkyBorder,
                            label = if (lang == "hi") "एकड़ काम" else "Area (Ac)",
                            value = areaStr,
                            onClick = onNavigateToReports
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. कुल खर्च: Slim full-width row BELOW the 3 tiles
                    val expenseStr = "₹" + formatIndianNumber(periodFilteredData.expenses)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BorderDarkEdge,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable(onClick = onNavigateToExpenses)
                            .testTag("total_expenses_slim_tile")
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 3.dp)
                                .background(CardWhite, RoundedCornerShape(16.dp))
                                .border(BorderStroke(1.5.dp, BorderNeutral), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MoneyDueLightTint,
                                        border = BorderStroke(1.dp, MoneyDueDarkEdge.copy(alpha = 0.35f)),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Receipt,
                                                contentDescription = "Total Expenses",
                                                tint = MoneyDueRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (lang == "hi") "कुल खर्च (अवधि)" else "Total Expenses",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSub
                                    )
                                }
                                Text(
                                    text = expenseStr,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MoneyDueRed
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ==========================================
            // 6. "हाल ही के काम" (RECENT JOBS)
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "hi") "हाल ही के काम" else "Recent Jobs",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepForestGreen
                    )
                    Text(
                        text = if (lang == "hi") "सभी देखें ›" else "View All ›",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepForestGreen,
                        modifier = Modifier.clickable(onClick = onNavigateToJobs)
                    )
                }
            }

            if (recentJobs.isEmpty()) {
                item {
                    KhetCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🚜", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (lang == "hi") "कोई काम दर्ज नहीं है" else "No jobs logged yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (lang == "hi") "पहला काम जोड़कर अपना डिजिटल बहीखाता शुरू करें" else "Start logging your tractor hires offline",
                                fontSize = 13.sp,
                                color = TextSub,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = ForestGreenDark,
                                modifier = Modifier.clickable(onClick = onNavigateToAddJob)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(bottom = 3.dp)
                                        .background(DeepForestGreen, RoundedCornerShape(999.dp))
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = CardWhite)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (lang == "hi") "पहला काम जोड़ें" else "Add First Job",
                                            color = CardWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                items(recentJobs, key = { it.id }) { job ->
                    KhetCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        onClick = { onSelectJob(job) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = job.customerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextMain
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val durationText = when (job.rateTypeSnapshot) {
                                    "HOUR" -> "${job.hours}h ${job.minutes}m"
                                    "ACRE" -> "${job.area} एकड़"
                                    else -> "${job.quantity.toInt()} यूनिट"
                                }
                                Text(
                                    text = "${job.implementName} • $durationText",
                                    fontSize = 13.sp,
                                    color = TextSub
                                )
                                Text(
                                    text = "${dateFormatter.format(Date(job.dateMillis))} • ${job.vehicleName}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            // Right Amount & Status Chip
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹" + formatIndianNumber(job.total),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = TextMain
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                StatusBadge(
                                    status = job.status,
                                    total = job.total,
                                    paid = job.paidAmount
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 7. FLOATING FLAT "+ काम जोड़ें" BUTTON (Solid #14452F, white bold text, pill shape, soft shadow)
        // ==========================================
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = DeepForestGreen,
            shadowElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 20.dp)
                .testTag("floating_add_job_button")
                .clickable(onClick = onNavigateToAddJob)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Job",
                    tint = CardWhite,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (lang == "hi") "काम जोड़ें" else "Add Job",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CardWhite
                )
            }
        }
    }

    // ==========================================
    // VEHICLE SELECTION BOTTOM SHEET
    // ==========================================
    if (showVehicleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVehicleSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = CardWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "hi") "ट्रैक्टर चुनें (My Vehicles)" else "Select Vehicle",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextMain
                    )
                    IconButton(onClick = { showVehicleSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSub)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Option: All Tractors
                VehicleSelectionItem(
                    title = if (lang == "hi") "सभी ट्रैक्टर (All Vehicles)" else "All Tractors",
                    subtitle = if (lang == "hi") "सभी गाड़ियों का संयुक्त हिसाब देखें" else "Combined view",
                    isSelected = selectedVehicleId == null,
                    onClick = {
                        viewModel.selectVehicleFilter(null)
                        showVehicleSheet = false
                    }
                )

                // Each tractor
                vehicles.forEach { vehicle ->
                    VehicleSelectionItem(
                        title = vehicle.name,
                        subtitle = "${vehicle.regNo} • ${vehicle.type}",
                        isSelected = selectedVehicleId == vehicle.id,
                        onClick = {
                            viewModel.selectVehicleFilter(vehicle.id)
                            showVehicleSheet = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Button: + Add Vehicle
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ForestGreenDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showVehicleSheet = false
                            onNavigateToVehicles()
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 3.dp)
                            .background(DeepForestGreen, RoundedCornerShape(14.dp))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = CardWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "hi") "+ नया ट्रैक्टर जोड़ें" else "+ Add Vehicle",
                                fontWeight = FontWeight.Bold,
                                color = CardWhite
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ==========================================
    // TRACTOR CALCULATOR BOTTOM SHEET
    // ==========================================
    if (showCalculatorSheet) {
        TractorCalculatorBottomSheet(
            implements = implements,
            lang = lang,
            onDismiss = { showCalculatorSheet = false },
            onUseInJob = {
                showCalculatorSheet = false
                onNavigateToAddJob()
            }
        )
    }

    // ==========================================
    // ALERTS BOTTOM SHEET
    // ==========================================
    if (showAlertsSheet) {
        AlertsBottomSheet(
            bakiCustomers = bakiCustomers,
            lang = lang,
            onDismiss = { showAlertsSheet = false },
            onCallKisan = { phone ->
                if (phone.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(intent)
                }
            },
            onWhatsAppKisan = { customer, due ->
                val text = if (lang == "hi") {
                    "नमस्ते ${customer.name} जी, आपके ट्रैक्टर काम का ₹${due.toInt()} बकाया है। कृपया भुगतान करें। धन्यवाद! - किसान साथी"
                } else {
                    "Hello ${customer.name}, your tractor work due is ₹${due.toInt()}. Kindly clear it. Thank you! - Kisan Saathi"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91${customer.phone}?text=${Uri.encode(text)}"))
                context.startActivity(intent)
            }
        )
    }

    // ==========================================
    // BAKI KISAN LIST BOTTOM SHEET (Tapped from summary card)
    // ==========================================
    if (showBakiSheet) {
        BakiKisanBottomSheet(
            bakiCustomers = bakiCustomers,
            totalBaki = totalPendingBakiOverall,
            lang = lang,
            onDismiss = { showBakiSheet = false },
            onNavigateToCustomers = {
                showBakiSheet = false
                onNavigateToCustomers()
            },
            onCallKisan = { phone ->
                if (phone.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(intent)
                }
            },
            onWhatsAppKisan = { customer, due ->
                val text = if (lang == "hi") {
                    "नमस्ते ${customer.name} जी, आपके ट्रैक्टर काम का ₹${due.toInt()} बकाया है। कृपया भुगतान करें। धन्यवाद! - किसान साथी"
                } else {
                    "Hello ${customer.name}, your tractor work due is ₹${due.toInt()}. Kindly clear it. Thank you! - Kisan Saathi"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91${customer.phone}?text=${Uri.encode(text)}"))
                context.startActivity(intent)
            }
        )
    }
}

// -------------------------------------------------------------
// Component: Pastel Stat Tile (With Wavy Field Lines & 3D Edge)
// -------------------------------------------------------------
@Composable
private fun PastelStatTile(
    modifier: Modifier = Modifier,
    label: String,
    count: String,
    backgroundColor: Color,
    borderColor: Color,
    bottomEdgeColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bottomEdgeColor,
        shadowElevation = 2.dp,
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 3.dp)
                .background(backgroundColor, RoundedCornerShape(18.dp))
                .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(18.dp))
                .wavyFieldTexture(borderColor.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // White circular icon badge with green border
                Surface(
                    shape = CircleShape,
                    color = CardWhite,
                    border = BorderStroke(1.5.dp, DeepForestGreen),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = DeepForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = TextSub,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = count,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextMain
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Component: Compact Insight 3D Raised Tile (for 3-tile single row)
// -------------------------------------------------------------
@Composable
private fun CompactInsightTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    badgeBg: Color,
    badgeBorder: Color,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BorderDarkEdge,
        shadowElevation = 2.dp,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 3.dp)
                .background(CardWhite, RoundedCornerShape(16.dp))
                .border(BorderStroke(1.5.dp, BorderNeutral), RoundedCornerShape(16.dp))
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: 28px icon badge on left, short label on right
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = badgeBg,
                        border = BorderStroke(1.dp, badgeBorder),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = iconTint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSub,
                        maxLines = 2,
                        lineHeight = 13.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Below: Value on its own line (bold 18-20sp in TextMain)
                val fontSp = if (value.length > 7) 16.sp else 19.sp
                Text(
                    text = value,
                    fontSize = fontSp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Component: Vehicle Selection Item
// -------------------------------------------------------------
@Composable
private fun VehicleSelectionItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) MintTint else CardWhite
    val border = if (isSelected) MintBorder else BorderNeutral
    val edge = if (isSelected) MintEdge else BorderDarkEdge

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = edge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 2.5.dp)
                .background(bg, RoundedCornerShape(14.dp))
                .border(BorderStroke(1.5.dp, border), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isSelected) DeepForestGreen else TextMain
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextSub
                    )
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = DeepForestGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Bottom Sheet: Tractor Calculator
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TractorCalculatorBottomSheet(
    implements: List<ImplementEntity>,
    lang: String,
    onDismiss: () -> Unit,
    onUseInJob: () -> Unit
) {
    var calcMode by remember { mutableStateOf("TIME") } // TIME, AREA, TRIP
    var hours by remember { mutableStateOf("1") }
    var minutes by remember { mutableStateOf("0") }
    var area by remember { mutableStateOf("1") }
    var trips by remember { mutableStateOf("1") }
    var rate by remember { mutableStateOf("800") }
    var selectedImplement by remember { mutableStateOf<ImplementEntity?>(null) }

    // Live Total Computation
    val totalAmount by remember(calcMode, hours, minutes, area, trips, rate) {
        derivedStateOf {
            val r = rate.toDoubleOrNull() ?: 0.0
            when (calcMode) {
                "TIME" -> {
                    val h = hours.toDoubleOrNull() ?: 0.0
                    val m = minutes.toDoubleOrNull() ?: 0.0
                    (h + (m / 60.0)) * r
                }
                "AREA" -> {
                    val a = area.toDoubleOrNull() ?: 0.0
                    a * r
                }
                "TRIP" -> {
                    val t = trips.toDoubleOrNull() ?: 0.0
                    t * r
                }
                else -> 0.0
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = DeepForestGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == "hi") "ट्रैक्टर कैलकुलेटर" else "Tractor Calculator",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSub)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calc Mode Tabs (Amber selected, White raised unselected)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf(
                    "TIME" to (if (lang == "hi") "समय (Time)" else "By Time"),
                    "AREA" to (if (lang == "hi") "रकबा (Area)" else "By Area"),
                    "TRIP" to (if (lang == "hi") "चक्कर (Trips)" else "By Trips")
                )
                modes.forEach { (modeKey, label) ->
                    val isSelected = calcMode == modeKey
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) AmberDark else BorderDarkEdge,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { calcMode = modeKey }
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 2.5.dp)
                                .background(if (isSelected) AmberAccent else CardWhite, RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, if (isSelected) AmberDark else BorderNeutral), RoundedCornerShape(12.dp))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) CardWhite else TextMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Implement Picker to auto-fill rate
            if (implements.isNotEmpty()) {
                Text(
                    text = if (lang == "hi") "इम्प्लीमेंट चुनें (रेट स्वतः भरेगा)" else "Select Implement (Auto-fills rate)",
                    fontSize = 12.sp,
                    color = TextSub,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    implements.forEach { impl ->
                        val isChosen = selectedImplement?.id == impl.id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isChosen) AmberDark else BorderDarkEdge,
                            modifier = Modifier.clickable {
                                selectedImplement = impl
                                rate = impl.defaultRate.toInt().toString()
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 2.dp)
                                    .background(if (isChosen) AmberAccent else CardWhite, RoundedCornerShape(16.dp))
                                    .border(BorderStroke(1.dp, if (isChosen) AmberDark else BorderNeutral), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${impl.name} (₹${impl.defaultRate.toInt()})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) CardWhite else TextMain
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Inputs by mode
            when (calcMode) {
                "TIME" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = hours,
                            onValueChange = { hours = it },
                            label = { Text(if (lang == "hi") "घंटे (Hours)" else "Hours") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minutes,
                            onValueChange = { minutes = it },
                            label = { Text(if (lang == "hi") "मिनट (Minutes)" else "Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                "AREA" -> {
                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text(if (lang == "hi") "रकबा (एकड़)" else "Area (Acres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                "TRIP" -> {
                    OutlinedTextField(
                        value = trips,
                        onValueChange = { trips = it },
                        label = { Text(if (lang == "hi") "चक्कर / फेरे (Trips)" else "Trips") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rate Input
            OutlinedTextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text(if (lang == "hi") "दर (₹ प्रति घंटा/एकड़/चक्कर)" else "Rate (₹ per unit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Calculated Result Card (Raised Green Money Card)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MoneyInDarkEdge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                        .background(MoneyInLightTint, RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.5.dp, MoneyInGreen), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (lang == "hi") "कुल अनुमानित हिसाब" else "Calculated Amount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MoneyInGreen
                            )
                            Text(
                                text = when (calcMode) {
                                    "TIME" -> "${hours}h ${minutes}m × ₹$rate"
                                    "AREA" -> "$area एकड़ × ₹$rate"
                                    else -> "$trips चक्कर × ₹$rate"
                                },
                                fontSize = 11.sp,
                                color = TextSub
                            )
                        }
                        Text(
                            text = "₹" + formatIndianNumber(totalAmount),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MoneyInGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button: Use in Job (Raised Deep Forest Green)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ForestGreenDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onUseInJob)
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                        .background(DeepForestGreen, RoundedCornerShape(14.dp))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CardWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == "hi") "+ काम में दर्ज करें" else "+ Use in Job",
                            fontWeight = FontWeight.Bold,
                            color = CardWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// -------------------------------------------------------------
// Bottom Sheet: Alerts & Reminders
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertsBottomSheet(
    bakiCustomers: List<Pair<CustomerEntity, Double>>,
    lang: String,
    onDismiss: () -> Unit,
    onCallKisan: (String) -> Unit,
    onWhatsAppKisan: (CustomerEntity, Double) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MoneyDueRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == "hi") "अलर्ट एवं रिमाइंडर" else "Alerts & Reminders",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSub)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (bakiCustomers.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MintTint,
                    border = BorderStroke(1.dp, MintBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎉", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (lang == "hi") "सब कुछ ठीक है!" else "All clear!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DeepForestGreen
                        )
                        Text(
                            text = if (lang == "hi") "किसी भी किसान की उधारी बकाया नहीं है।" else "No pending dues found.",
                            fontSize = 13.sp,
                            color = TextSub
                        )
                    }
                }
            } else {
                Text(
                    text = if (lang == "hi") "${bakiCustomers.size} किसानों से बकाया लेना है:" else "${bakiCustomers.size} pending dues to collect:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSub
                )
                Spacer(modifier = Modifier.height(8.dp))

                bakiCustomers.take(5).forEach { (customer, due) ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MoneyDueDarkEdge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 2.5.dp)
                                .background(MoneyDueLightTint, RoundedCornerShape(14.dp))
                                .border(BorderStroke(1.dp, MoneyDueRed.copy(alpha = 0.3f)), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = customer.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextMain
                                    )
                                    Text(
                                        text = "बकाया: ₹" + formatIndianNumber(due),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = MoneyDueRed
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (customer.phone.isNotEmpty()) {
                                        IconButton(
                                            onClick = { onWhatsAppKisan(customer, due) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "WhatsApp", tint = MoneyInGreen)
                                        }
                                        IconButton(
                                            onClick = { onCallKisan(customer.phone) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = DeepForestGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// -------------------------------------------------------------
// Bottom Sheet: Kisan with Baki List (Sorted High to Low)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BakiKisanBottomSheet(
    bakiCustomers: List<Pair<CustomerEntity, Double>>,
    totalBaki: Double,
    lang: String,
    onDismiss: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onCallKisan: (String) -> Unit,
    onWhatsAppKisan: (CustomerEntity, Double) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (lang == "hi") "बाकी है पैसा (उधारी सूची)" else "Pending Dues List",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                    Text(
                        text = if (lang == "hi") "कुल बाकी: ₹" + formatIndianNumber(totalBaki) else "Total Due: ₹" + formatIndianNumber(totalBaki),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MoneyDueRed
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSub)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (bakiCustomers.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MintTint,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎉", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (lang == "hi") "कोई उधारी नहीं है!" else "Zero pending dues!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MoneyInGreen
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    items(bakiCustomers, key = { it.first.id }) { (customer, due) ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = BorderDarkEdge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 2.5.dp)
                                    .background(CardWhite, RoundedCornerShape(14.dp))
                                    .border(BorderStroke(1.5.dp, BorderNeutral), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = customer.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextMain
                                        )
                                        if (customer.village.isNotEmpty()) {
                                            Text(
                                                text = customer.village,
                                                fontSize = 11.sp,
                                                color = TextSub
                                            )
                                        }
                                        Text(
                                            text = "₹" + formatIndianNumber(due),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = MoneyDueRed
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (customer.phone.isNotEmpty()) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MintTint,
                                                border = BorderStroke(1.dp, MintBorder),
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clickable { onWhatsAppKisan(customer, due) }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Share, contentDescription = "WhatsApp", tint = MoneyInGreen, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                            Surface(
                                                shape = CircleShape,
                                                color = MintTint,
                                                border = BorderStroke(1.dp, MintBorder),
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clickable { onCallKisan(customer.phone) }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = DeepForestGreen, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ForestGreenDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToCustomers)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 3.dp)
                            .background(DeepForestGreen, RoundedCornerShape(14.dp))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang == "hi") "सभी किसान खाता देखें" else "View All Kisan Khata",
                            fontWeight = FontWeight.Bold,
                            color = CardWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
