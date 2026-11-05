package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CustomerEntity
import com.example.notification.NotificationService
import com.example.ui.KhetSaathiViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BorderDarkEdge
import com.example.ui.theme.BorderNeutral
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamYellowTint
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.MintTint
import com.example.ui.theme.MoneyDueDarkEdge
import com.example.ui.theme.MoneyDueLightTint
import com.example.ui.theme.MoneyDueRed
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.MoneyInLightTint
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSub
import com.example.ui.theme.WarmCreamBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: KhetSaathiViewModel,
    onBack: () -> Unit,
    onSelectCustomer: (CustomerEntity) -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val allJobs by viewModel.jobs.collectAsStateWithLifecycle()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    // Filter customers who have baki > 0
    val bakiCustomers = remember(customers, allJobs) {
        val dues = mutableMapOf<String, Double>()
        for (job in allJobs) {
            val due = (job.total - job.paidAmount).coerceAtLeast(0.0)
            if (due > 0) {
                dues[job.customerId] = (dues[job.customerId] ?: 0.0) + due
            }
        }
        customers.mapNotNull { customer ->
            val due = dues[customer.id] ?: 0.0
            if (due > 0) Pair(customer, due) else null
        }.sortedByDescending { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (lang == "hi") "बाकी रिमाइंडर (Reminders)" else "Payment Reminders",
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
        if (bakiCustomers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = MintTint,
                        border = BorderStroke(1.5.dp, DeepForestGreen),
                        modifier = Modifier.size(76.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DeepForestGreen,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "सारा बाकी वसूल है! 🎉",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepForestGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "किसी भी किसान का कोई पैसा बाकी नहीं है। आपका बहीखाता बिल्कुल चुस्त-दुरुस्त है!",
                        fontSize = 13.sp,
                        color = TextSub,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "सक्रिय तकादे (${bakiCustomers.size} किसान)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DeepForestGreen
                    )
                }

                items(bakiCustomers, key = { it.first.id }) { (customer, due) ->
                    ReminderCustomerCard(
                        customer = customer,
                        dueAmount = due,
                        onCardClick = { onSelectCustomer(customer) },
                        onToggleReminders = { enabled ->
                            viewModel.updateCustomer(customer.copy(remindersEnabled = enabled))
                        },
                        onCall = {
                            if (customer.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                context.startActivity(intent)
                            }
                        },
                        onWhatsApp = {
                            if (customer.phone.isNotBlank()) {
                                val cleanPhone = customer.phone.replace("+", "").replace(" ", "").replace("-", "")
                                val fullNumber = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                                val msg = "राम-राम ${customer.name} जी! आपके ट्रैक्टर के काम का ₹${NotificationService.formatRupee(due)} बाकी है। कृपया सुविधानुसार भुगतान कर दें। धन्यवाद।"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$fullNumber?text=${Uri.encode(msg)}"))
                                context.startActivity(intent)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
private fun ReminderCustomerCard(
    customer: CustomerEntity,
    dueAmount: Double,
    onCardClick: () -> Unit,
    onToggleReminders: (Boolean) -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    val intervalText = "हर ${customer.repeatDays} दिन"
    val dueDateText = if (customer.dueDateMillis != null) {
        val dateStr = SimpleDateFormat("dd MMM", Locale("hi", "IN")).format(Date(customer.dueDateMillis))
        "तारीख: $dateStr"
    } else if (customer.dueDayOfMonth != null) {
        "हर महीने ${customer.dueDayOfMonth} तारीख"
    } else {
        "नियमित अंतराल"
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BorderDarkEdge,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("reminder_kisan_${customer.id}")
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 3.dp)
                .background(CardWhite, RoundedCornerShape(18.dp))
                .border(BorderStroke(1.dp, BorderNeutral), RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header: Kisan Name + Baki Amount + Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = customer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DeepForestGreen
                        )
                        if (customer.phone.isNotBlank()) {
                            Text(
                                text = customer.phone,
                                fontSize = 12.sp,
                                color = TextSub
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹" + NotificationService.formatRupee(dueAmount),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = MoneyDueRed
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (customer.remindersEnabled) "चालू" else "बंद",
                                fontSize = 11.sp,
                                color = if (customer.remindersEnabled) MoneyInGreen else TextMuted
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = customer.remindersEnabled,
                                onCheckedChange = onToggleReminders,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CardWhite,
                                    checkedTrackColor = DeepForestGreen
                                ),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // Schedule pills row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = CreamYellowTint,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = AmberDark, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = dueDateText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MintTint,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = intervalText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepForestGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Quick Action Buttons (Call, WhatsApp)
                if (customer.phone.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onCall,
                            colors = ButtonDefaults.buttonColors(containerColor = MintTint),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("कॉल करें", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                        }

                        Button(
                            onClick = onWhatsApp,
                            colors = ButtonDefaults.buttonColors(containerColor = MoneyInLightTint),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = MoneyInGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MoneyInGreen)
                        }
                    }
                }
            }
        }
    }
}
