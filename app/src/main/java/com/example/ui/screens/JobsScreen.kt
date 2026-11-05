package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.JobEntity
import com.example.ui.KhetSaathiViewModel
import com.example.ui.components.KhetCard
import com.example.ui.components.KhetInputField
import com.example.ui.components.KhetPillChip
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberLight
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.DueRed
import com.example.ui.theme.DueRedLight
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareHelper
import com.example.util.Strings
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsScreen(
    viewModel: KhetSaathiViewModel,
    onNavigateToAddJob: () -> Unit
) {
    val context = LocalContext.current
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val allJobs by viewModel.jobs.collectAsStateWithLifecycle()
    val undoMessage by viewModel.undoMessage.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isCalendarView by remember { mutableStateOf(false) }
    var selectedTimeFilter by remember { mutableStateOf("ALL") } // ALL, TODAY, MONTH, YEAR
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // ALL, PAID, PART, DUE

    var selectedJobForDetails by remember { mutableStateOf<JobEntity?>(null) }
    var showPaymentDialogForJob by remember { mutableStateOf<JobEntity?>(null) }

    // Undo snackbar trigger
    LaunchedEffect(undoMessage) {
        undoMessage?.let {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = Strings.get("deleted_success", lang),
                    actionLabel = Strings.get("undo", lang),
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoDeleteJob()
                } else {
                    viewModel.clearUndoMessage()
                }
            }
        }
    }

    val thresholdMillis = remember(selectedTimeFilter) {
        val cal = Calendar.getInstance()
        when (selectedTimeFilter) {
            "TODAY" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "MONTH" -> {
                cal.add(Calendar.DAY_OF_YEAR, -30)
                cal.timeInMillis
            }
            "YEAR" -> {
                cal.add(Calendar.DAY_OF_YEAR, -365)
                cal.timeInMillis
            }
            else -> 0L
        }
    }

    val filteredJobs = remember(allJobs, thresholdMillis, selectedStatusFilter) {
        allJobs.filter { job ->
            val matchesTime = thresholdMillis == 0L || job.dateMillis >= thresholdMillis
            val due = (job.total - job.paidAmount).coerceAtLeast(0.0)
            val matchesStatus = when (selectedStatusFilter) {
                "PAID" -> job.paidAmount >= job.total && job.total > 0
                "PART" -> job.paidAmount > 0 && due > 0
                "DUE" -> job.paidAmount == 0.0 && job.total > 0
                else -> true
            }
            matchesTime && matchesStatus
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddJob,
                containerColor = ForestGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .testTag("jobs_screen_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Job", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title & Calendar/List Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("jobs_list", lang),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )

                // Toggle Button View
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        IconButton(
                            onClick = { isCalendarView = false },
                            modifier = Modifier
                                .size(38.dp)
                                .background(if (!isCalendarView) ForestGreenLight else Color.Transparent, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.FormatListBulleted,
                                contentDescription = "List View",
                                tint = if (!isCalendarView) ForestGreen else TextMuted
                            )
                        }
                        IconButton(
                            onClick = { isCalendarView = true },
                            modifier = Modifier
                                .size(38.dp)
                                .background(if (isCalendarView) ForestGreenLight else Color.Transparent, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Calendar View",
                                tint = if (isCalendarView) ForestGreen else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Period Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to Strings.get("all_time", lang),
                    "TODAY" to Strings.get("today", lang),
                    "MONTH" to Strings.get("this_month", lang),
                    "YEAR" to Strings.get("this_year", lang)
                ).forEach { (key, label) ->
                    KhetPillChip(
                        text = label,
                        selected = selectedTimeFilter == key,
                        onClick = { selectedTimeFilter = key }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to Strings.get("status_all", lang),
                    "PAID" to Strings.get("status_paid", lang),
                    "PART" to Strings.get("status_part", lang),
                    "DUE" to Strings.get("status_due", lang)
                ).forEach { (key, label) ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedStatusFilter == key) ForestGreen else CardWhite,
                        border = BorderStroke(1.dp, if (selectedStatusFilter == key) ForestGreen else BorderGray),
                        modifier = Modifier.clickable { selectedStatusFilter = key }
                    ) {
                        Text(
                            text = label,
                            color = if (selectedStatusFilter == key) Color.White else TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isCalendarView) {
                // Calendar Month View with day totals under dates
                CalendarMonthJobsView(
                    jobs = allJobs,
                    onSelectJob = { selectedJobForDetails = it },
                    lang = lang
                )
            } else {
                // List View
                if (filteredJobs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌾", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "कोई काम नहीं मिला (No jobs)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("hi", "IN")) }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredJobs, key = { it.id }) { job ->
                            KhetCard(onClick = { selectedJobForDetails = job }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = job.customerName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val dur = when (job.rateTypeSnapshot) {
                                            "HOUR" -> "${job.hours}h ${job.minutes}m"
                                            "ACRE" -> "${job.area} एकड़"
                                            else -> "${job.quantity.toInt()} यूनिट"
                                        }
                                        Text(
                                            text = "${job.implementName} • $dur",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "${dateFormatter.format(Date(job.dateMillis))} • ${job.vehicleName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${job.total.toInt()}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        StatusBadge(status = job.status, total = job.total, paid = job.paidAmount)
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Job Details, Add Payment, Share Bill, Delete
    selectedJobForDetails?.let { job ->
        ModalBottomSheet(
            onDismissRequest = { selectedJobForDetails = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CardWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = job.customerName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        val dateFormatted = remember { SimpleDateFormat("dd MMMM yyyy", Locale("hi", "IN")).format(Date(job.dateMillis)) }
                        Text(text = dateFormatted, color = TextMuted, fontSize = 13.sp)
                    }
                    StatusBadge(status = job.status, total = job.total, paid = job.paidAmount)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Work Specifications
                Surface(
                    color = CreamBackground,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("यंत्र (Implement):", color = TextSecondary, fontSize = 14.sp)
                            Text(job.implementName, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ट्रैक्टर:", color = TextSecondary, fontSize = 14.sp)
                            Text(job.vehicleName, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val durLabel = when (job.rateTypeSnapshot) {
                                "HOUR" -> "${job.hours} घंटा ${job.minutes} मिनट"
                                "ACRE" -> "${job.area} एकड़"
                                else -> "${job.quantity.toInt()} यूनिट"
                            }
                            Text("मात्रा/समय:", color = TextSecondary, fontSize = 14.sp)
                            Text("$durLabel (दर: ₹${job.rateSnapshot.toInt()})", fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        if (job.notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("टिप्पणी: ${job.notes}", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Breakdown
                val due = (job.total - job.paidAmount).coerceAtLeast(0.0)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("कुल बिल", color = TextMuted, fontSize = 13.sp)
                        Text("₹${job.total.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("जमा मिला", color = TextMuted, fontSize = 13.sp)
                        Text("₹${job.paidAmount.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PaidGreen)
                    }
                    Column {
                        Text("बाकी (Due)", color = TextMuted, fontSize = 13.sp)
                        Text("₹${due.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DueRed)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Share WhatsApp Bill, Add Payment, Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val billText = ShareHelper.formatJobBill(job, lang)
                            ShareHelper.shareWhatsApp(context, billText)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("व्हाट्सएप बिल", color = Color.White)
                    }

                    if (due > 0) {
                        Button(
                            onClick = {
                                showPaymentDialogForJob = job
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("भुगतान जोड़ें", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.deleteJob(job)
                        selectedJobForDetails = null
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DueRed),
                    border = BorderStroke(1.dp, DueRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = DueRed)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Strings.get("delete_job", lang), color = DueRed, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Add Payment Dialog
    showPaymentDialogForJob?.let { job ->
        val due = (job.total - job.paidAmount).coerceAtLeast(0.0)
        var paymentAmountInput by remember { mutableStateOf(due.toInt().toString()) }
        var paymentMethodInput by remember { mutableStateOf("CASH") }

        AlertDialog(
            onDismissRequest = { showPaymentDialogForJob = null },
            title = {
                Text("भुगतान दर्ज करें (Add Payment)", fontWeight = FontWeight.Bold, color = ForestGreen)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("किसान: ${job.customerName}", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("बाकी रकम: ₹${due.toInt()}", color = DueRed, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    KhetInputField(
                        label = "प्राप्त राशि (₹)",
                        value = paymentAmountInput,
                        onValueChange = { paymentAmountInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("CASH" to "नकद", "UPI" to "UPI", "BANK" to "बैंक").forEach { (method, label) ->
                            val isSelected = paymentMethodInput == method
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) ForestGreen else CardWhite,
                                border = BorderStroke(1.dp, ForestGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { paymentMethodInput = method }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else ForestGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountInput.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.addPaymentToJob(job, amount, paymentMethodInput)
                            showPaymentDialogForJob = null
                            selectedJobForDetails = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("सेव करें", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialogForJob = null }) {
                    Text("रद्द करें", color = TextPrimary)
                }
            }
        )
    }
}

@Composable
fun CalendarMonthJobsView(
    jobs: List<JobEntity>,
    onSelectJob: (JobEntity) -> Unit,
    lang: String = "hi"
) {
    val cal = remember { Calendar.getInstance() }
    val currentMonth = remember { SimpleDateFormat("MMMM yyyy", Locale("hi", "IN")).format(cal.time) }
    val daysInMonth = remember { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }

    // Map day of month -> total earnings and job count
    val dayTotals = remember(jobs) {
        val map = mutableMapOf<Int, Double>()
        val jobCal = Calendar.getInstance()
        jobs.forEach { job ->
            jobCal.timeInMillis = job.dateMillis
            val day = jobCal.get(Calendar.DAY_OF_MONTH)
            map[day] = (map[day] ?: 0.0) + job.total
        }
        map
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = ForestGreenLight,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 $currentMonth",
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenDark,
                    fontSize = 16.sp
                )
                val monthTotal = dayTotals.values.sum()
                Text(
                    text = "माह कुल: ₹${monthTotal.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    color = ForestGreen,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
        ) {
            items(daysInMonth) { dayIndex ->
                val dayNum = dayIndex + 1
                val totalForDay = dayTotals[dayNum] ?: 0.0
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (totalForDay > 0) CardWhite else CreamBackground,
                    border = BorderStroke(1.dp, if (totalForDay > 0) ForestGreen else BorderGray),
                    modifier = Modifier.height(60.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dayNum.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (totalForDay > 0) ForestGreen else TextMuted
                        )
                        if (totalForDay > 0) {
                            Text(
                                text = "₹${totalForDay.toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = PaidGreen
                            )
                        } else {
                            Text(text = "-", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}
