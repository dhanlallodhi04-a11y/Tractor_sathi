package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerEntity
import com.example.data.JobEntity
import com.example.ui.KhetSaathiViewModel
import com.example.ui.theme.*
import com.example.util.I18n
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KisanDetailScreen(
    kisanId: String,
    viewModel: KhetSaathiViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()

    val kisan = customers.find { it.id == kisanId }
    val kisanJobs = remember(jobs, kisanId) {
        jobs.filter { it.customerId == kisanId }.sortedByDescending { it.dateMillis }
    }

    val totalBilled = remember(kisanJobs) { kisanJobs.sumOf { it.total } }
    val totalPaid = remember(kisanJobs) { kisanJobs.sumOf { it.paidAmount } }
    val totalDue = remember(totalBilled, totalPaid) { (totalBilled - totalPaid).coerceAtLeast(0.0) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    if (kisan == null) {
        // Fallback if deleted or not found
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    val initials = kisan.name.trim().take(2).uppercase(Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = kisan.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("kisan_detail_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToEdit(kisan.id) },
                        modifier = Modifier.testTag("kisan_detail_edit_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Kisan", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ForestGreen
                )
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile & Contact Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = kisan.name,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )

                                if (kisan.village.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = kisan.village,
                                            fontSize = 14.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                if (kisan.phone.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = ForestGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = kisan.phone,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = ForestGreen
                                        )
                                    }
                                }
                            }
                        }

                        // Mobile missing warning hint
                        if (kisan.phone.isBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AmberAccent.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = AmberDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (lang == "hi")
                                            "तकादा भेजने के लिए मोबाइल नंबर जोड़ें"
                                        else
                                            "Add mobile number to send reminders",
                                        fontSize = 13.sp,
                                        color = CharcoalText,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { onNavigateToEdit(kisan.id) }) {
                                        Text(
                                            text = if (lang == "hi") "+ जोड़ें" else "+ Add",
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Khata Summary Card (Billed, Paid, Baki)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (lang == "hi") "खाता सारांश (Khata Summary)" else "Khata Summary",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Total Billed
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = if (lang == "hi") "कुल कार्य" else "Total Billed",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "₹${totalBilled.toInt()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                            }

                            // Total Paid
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (lang == "hi") "जमा (Paid)" else "Paid",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "₹${totalPaid.toInt()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PaidGreen
                                )
                            }

                            // Total Due / Baki
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (lang == "hi") "बाकी (Due)" else "Balance Due",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalDue > 0) DueRed else PaidGreen
                                )
                                if (totalDue > 0) {
                                    Text(
                                        text = "₹${totalDue.toInt()}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DueRed
                                    )
                                } else {
                                    Surface(
                                        color = PaidGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (lang == "hi") "चुकता (Clear)" else "Clear",
                                            color = PaidGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions: Add Payment, Send Reminder, Share Statement
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Add Payment
                    Button(
                        onClick = { showPaymentSheet = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("kisan_add_payment_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (lang == "hi") "+ जमा दर्ज" else "+ Payment",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Send Reminder (WhatsApp/SMS)
                    Button(
                        onClick = {
                            if (kisan.phone.isNotBlank()) {
                                showReminderDialog = true
                            }
                        },
                        enabled = kisan.phone.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("kisan_send_reminder_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberAccent,
                            disabledContainerColor = Color.LightGray.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (kisan.phone.isNotBlank()) Color.Black else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (lang == "hi") "तकादा" else "Reminder",
                            fontWeight = FontWeight.Bold,
                            color = if (kisan.phone.isNotBlank()) Color.Black else Color.Gray,
                            fontSize = 13.sp
                        )
                    }

                    // Share Statement
                    OutlinedButton(
                        onClick = {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val statementText = buildString {
                                append("खेत साथी - किसान खाता पर्ची\n")
                                append("किसान: ${kisan.name}\n")
                                if (kisan.phone.isNotBlank()) append("मोबाइल: ${kisan.phone}\n")
                                if (kisan.village.isNotBlank()) append("गाँव: ${kisan.village}\n")
                                append("दिनांक: ${sdf.format(Date())}\n")
                                append("----------------------------\n")
                                append("कुल कार्य बिल: ₹${totalBilled.toInt()}\n")
                                append("कुल जमा राशि: ₹${totalPaid.toInt()}\n")
                                append("शेष बकाया राशि: ₹${totalDue.toInt()}\n")
                                append("----------------------------\n")
                                append("हाल के कार्य:\n")
                                kisanJobs.take(5).forEach { j ->
                                    val dStr = sdf.format(Date(j.dateMillis))
                                    append("• $dStr | ${j.implementName} | ₹${j.total.toInt()}\n")
                                }
                                append("\nधन्यवाद! खेत साथी द्वारा निर्मित।")
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, statementText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Kisan Statement"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("kisan_share_statement_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ForestGreen)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (lang == "hi") "खाता पर्ची" else "Statement",
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Job History Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "hi") "कार्य इतिहास (${kisanJobs.size})" else "Job History (${kisanJobs.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                }
            }

            // Job items
            if (kisanJobs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (lang == "hi") "इस किसान का कोई कार्य दर्ज नहीं है" else "No jobs recorded for this Kisan yet",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(kisanJobs, key = { it.id }) { job ->
                    KisanJobItemCard(job = job, lang = lang)
                }
            }

            // Bottom Danger / Manage Actions
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateToEdit(kisan.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ForestGreen)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = ForestGreen)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (lang == "hi") "संपादित करें" else "Edit",
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("kisan_delete_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DueRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = DueRed)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (lang == "hi") "हटाएं" else "Delete",
                            color = DueRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = if (lang == "hi") "किसान हटाएं?" else "Delete this Kisan?",
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
            },
            text = {
                if (totalDue > 0) {
                    Text(
                        text = if (lang == "hi")
                            "इस किसान का ₹${totalDue.toInt()} बकाया है! क्या आप वाकई हटाना चाहते हैं? इनके सभी पुराने कार्य रिकॉर्ड सुरक्षित रहेंगे।"
                        else
                            "This Kisan still owes ₹${totalDue.toInt()}. Are you sure? All past job records will be kept.",
                        color = DueRed,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = if (lang == "hi")
                            "क्या आप '${kisan.name}' को हटाना चाहते हैं? इनके सभी कार्य रिकॉर्ड सुरक्षित रहेंगे।"
                        else
                            "Delete '${kisan.name}'? All past job records will be kept.",
                        color = CharcoalText
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteCustomerWithUndo(kisan)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed)
                ) {
                    Text(if (lang == "hi") "हाँ, हटाएं" else "Yes, Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(if (lang == "hi") "रद्द करें" else "Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Reminder Dialog (WhatsApp, SMS, Call)
    if (showReminderDialog) {
        val reminderMessage = remember(kisan, totalDue) {
            "नमस्ते ${kisan.name} जी, खेत साथी के अनुसार आपके ट्रैक्टर कार्य का ₹${totalDue.toInt()} बकाया है। कृपया भुगतान करें। धन्यवाद।"
        }

        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = {
                Text(
                    text = if (lang == "hi") "तकादा भेजें" else "Send Reminder",
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = reminderMessage,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        modifier = Modifier
                            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    )

                    // WhatsApp button
                    Button(
                        onClick = {
                            showReminderDialog = false
                            try {
                                val cleanPhone = kisan.phone.replace("+", "").replace(" ", "")
                                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(reminderMessage)}")
                                val waIntent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(waIntent)
                            } catch (e: Exception) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, reminderMessage)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Send via"))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("WhatsApp पर भेजें", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // SMS button
                    OutlinedButton(
                        onClick = {
                            showReminderDialog = false
                            val smsUri = Uri.parse("smsto:${kisan.phone}")
                            val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                                putExtra("sms_body", reminderMessage)
                            }
                            context.startActivity(smsIntent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = CharcoalText)
                        Spacer(Modifier.width(8.dp))
                        Text("SMS भेजें", color = CharcoalText, fontWeight = FontWeight.Bold)
                    }

                    // Call button
                    OutlinedButton(
                        onClick = {
                            showReminderDialog = false
                            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${kisan.phone}"))
                            context.startActivity(callIntent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = ForestGreen)
                        Spacer(Modifier.width(8.dp))
                        Text("कॉल करें (${kisan.phone})", color = ForestGreen, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showReminderDialog = false }) {
                    Text(if (lang == "hi") "बंद करें" else "Close", color = Color.Gray)
                }
            }
        )
    }

    // Add Payment BottomSheet
    if (showPaymentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            containerColor = Color.White
        ) {
            AddPaymentSheetContent(
                kisan = kisan,
                suggestedDue = totalDue,
                lang = lang,
                onSave = { amount, method, note ->
                    viewModel.recordCustomerPayment(kisan.id, amount, method, note)
                    showPaymentSheet = false
                },
                onCancel = { showPaymentSheet = false }
            )
        }
    }
}

@Composable
fun KisanJobItemCard(job: JobEntity, lang: String) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateStr = remember(job.dateMillis) { sdf.format(Date(job.dateMillis)) }
    val dueAmount = (job.total - job.paidAmount).coerceAtLeast(0.0)

    val (statusLabel, statusColor, statusBg) = when {
        dueAmount <= 0.01 -> Triple(
            if (lang == "hi") "जमा (Paid)" else "Paid",
            PaidGreen,
            PaidGreen.copy(alpha = 0.12f)
        )
        job.paidAmount > 0 -> Triple(
            if (lang == "hi") "आंशिक (Part)" else "Part",
            AmberDark,
            AmberAccent.copy(alpha = 0.2f)
        )
        else -> Triple(
            if (lang == "hi") "बाकी (Baki)" else "Baki",
            DueRed,
            DueRed.copy(alpha = 0.12f)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = job.implementName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CharcoalText
                    )
                    Text(
                        text = "$dateStr • ${job.vehicleName}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity / Work details
                val workDetails = when (job.rateTypeSnapshot) {
                    "HOUR" -> "${job.hours}h ${job.minutes}m @ ₹${job.rateSnapshot.toInt()}/h"
                    "CHAKKAR" -> "${job.quantity.toInt()} चक्कर @ ₹${job.rateSnapshot.toInt()}"
                    "DAY" -> "${job.quantity.toInt()} दिन @ ₹${job.rateSnapshot.toInt()}"
                    "FIXED" -> "निश्चित दर (Fixed)"
                    else -> "${job.area} ${job.rateTypeSnapshot.lowercase()} @ ₹${job.rateSnapshot.toInt()}"
                }

                Text(
                    text = workDetails,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )

                Text(
                    text = "₹${job.total.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = CharcoalText
                )
            }
        }
    }
}

@Composable
fun AddPaymentSheetContent(
    kisan: CustomerEntity,
    suggestedDue: Double,
    lang: String,
    onSave: (amount: Double, method: String, notes: String) -> Unit,
    onCancel: () -> Unit
) {
    var amountText by remember { mutableStateOf(if (suggestedDue > 0) suggestedDue.toInt().toString() else "") }
    var selectedMethod by remember { mutableStateOf("CASH") }
    var noteText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val methods = listOf(
        "CASH" to (if (lang == "hi") "नकद (Cash)" else "Cash"),
        "PHONEPE" to "PhonePe",
        "GPAY" to "Google Pay",
        "PAYTM" to "Paytm",
        "BANK" to (if (lang == "hi") "खाते में (Bank)" else "Bank Transfer")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (lang == "hi") "जमा राशि दर्ज करें (${kisan.name})" else "Record Payment (${kisan.name})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = CharcoalText
        )

        // Amount input
        Column {
            Text(
                text = if (lang == "hi") "राशि (₹) *" else "Amount (₹) *",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { ch -> ch.isDigit() || ch == '.' }
                    errorMessage = null
                },
                placeholder = { Text("₹ 0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("payment_amount_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = Color(0xFFCFCFCF)
                )
            )
            if (errorMessage != null) {
                Text(errorMessage!!, color = DueRed, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        // Payment Method Chips
        Column {
            Text(
                text = if (lang == "hi") "भुगतान का माध्यम" else "Payment Method",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                methods.take(3).forEach { (id, label) ->
                    val isSelected = selectedMethod == id
                    Surface(
                        onClick = { selectedMethod = id },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ForestGreen else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) ForestGreen else Color.LightGray),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else CharcoalText,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                methods.drop(3).forEach { (id, label) ->
                    val isSelected = selectedMethod == id
                    Surface(
                        onClick = { selectedMethod = id },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ForestGreen else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) ForestGreen else Color.LightGray),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else CharcoalText,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Note
        Column {
            Text(
                text = if (lang == "hi") "टिप्पणी / विवरण (वैकल्पिक)" else "Note (Optional)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text(if (lang == "hi") "उदा. नकद दिया, रबी जुताई का भुगतान" else "e.g. Cash received") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = Color(0xFFCFCFCF)
                )
            )
        }

        // Save Button
        Button(
            onClick = {
                val amt = amountText.toDoubleOrNull()
                if (amt == null || amt <= 0.0) {
                    errorMessage = if (lang == "hi") "कृपया मान्य राशि दर्ज करें" else "Enter a valid amount"
                } else {
                    onSave(amt, selectedMethod, noteText)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_payment_confirm_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
        ) {
            Text(
                text = if (lang == "hi") "जमा सुरक्षित करें" else "Save Payment",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
