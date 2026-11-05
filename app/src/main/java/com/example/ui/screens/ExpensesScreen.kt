package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ExpenseEntity
import com.example.ui.KhetSaathiViewModel
import com.example.ui.components.KhetCard
import com.example.ui.components.KhetInputField
import com.example.ui.components.KhetPillChip
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ExpensesScreen(
    viewModel: KhetSaathiViewModel
) {
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val vehicleMetrics by viewModel.vehicleMetrics.collectAsStateWithLifecycle()

    var selectedVehicleFilterId by remember { mutableStateOf<String?>(null) }
    var selectedRangeFilter by remember { mutableStateOf("ALL") } // ALL, TODAY, MONTH
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    val thresholdMillis = remember(selectedRangeFilter) {
        val cal = Calendar.getInstance()
        when (selectedRangeFilter) {
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
            else -> 0L
        }
    }

    val filteredExpenses = remember(expenses, selectedVehicleFilterId, thresholdMillis) {
        expenses.filter { exp ->
            val matchVeh = selectedVehicleFilterId == null || exp.vehicleId == selectedVehicleFilterId
            val matchRange = thresholdMillis == 0L || exp.dateMillis >= thresholdMillis
            matchVeh && matchRange
        }
    }

    val totalSpent = filteredExpenses.sumOf { it.amount }
    val totalLitres = filteredExpenses.filter { it.category == "FUEL" }.sumOf { it.litres }

    // Diesel average metric
    val currentVehicleMetric = vehicleMetrics.find { it.vehicle.id == selectedVehicleFilterId }
        ?: vehicleMetrics.firstOrNull()

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("hi", "IN")) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExpenseDialog = true },
                containerColor = ForestGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .testTag("add_expense_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense", modifier = Modifier.size(28.dp))
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

            Text(
                text = Strings.get("expenses_title", lang),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KhetPillChip(
                    text = Strings.get("all_vehicles", lang),
                    selected = selectedVehicleFilterId == null,
                    onClick = { selectedVehicleFilterId = null }
                )
                vehicles.forEach { v ->
                    KhetPillChip(
                        text = v.name,
                        selected = selectedVehicleFilterId == v.id,
                        onClick = { selectedVehicleFilterId = v.id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date-range chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to Strings.get("all_time", lang),
                    "TODAY" to Strings.get("today", lang),
                    "MONTH" to Strings.get("this_month", lang)
                ).forEach { (key, label) ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedRangeFilter == key) ForestGreen else CardWhite,
                        border = BorderStroke(1.dp, if (selectedRangeFilter == key) ForestGreen else BorderGray),
                        modifier = Modifier.clickable { selectedRangeFilter = key }
                    ) {
                        Text(
                            text = label,
                            color = if (selectedRangeFilter == key) Color.White else TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Card (Total Spent, Total Diesel & Diesel Average litres/hour)
            KhetCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(Strings.get("total_spent", lang), color = TextMuted, fontSize = 13.sp)
                            Text("₹${totalSpent.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DueRed)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(Strings.get("total_litres", lang), color = TextMuted, fontSize = 13.sp)
                            Text("${totalLitres.toInt()} L", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AmberGold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Diesel Average Box
                    Surface(
                        color = ForestGreenLight,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = ForestGreenDark)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = Strings.get("diesel_avg", lang),
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreenDark,
                                        fontSize = 14.sp
                                    )
                                    val runningHrs = currentVehicleMetric?.runningHours ?: 0.0
                                    Text(
                                        text = "ट्रैक्टर चला: ${"%.1f".format(runningHrs)} घंटे",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            val avgVal = currentVehicleMetric?.dieselAvgLitresPerHour ?: 0.0
                            Text(
                                text = "${"%.1f".format(avgVal)} L/घंटा",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expenses List
            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "कोई खर्च दर्ज नहीं है (No expenses)",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { exp ->
                        KhetCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (exp.category == "FUEL") AmberLight else ForestGreenLight,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (exp.category == "FUEL") Icons.Default.LocalGasStation else Icons.Default.Build,
                                                contentDescription = null,
                                                tint = if (exp.category == "FUEL") AmberGold else ForestGreen
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        val catLabel = when (exp.category) {
                                            "FUEL" -> "डीजल (Fuel - ${exp.litres.toInt()}L)"
                                            "MAINTENANCE" -> "सर्विस व मरम्मत (Maintenance)"
                                            else -> "अन्य खर्च"
                                        }
                                        Text(catLabel, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                        Text("${dateFormatter.format(Date(exp.dateMillis))} • ${exp.vehicleName}", fontSize = 12.sp, color = TextMuted)
                                        if (exp.notes.isNotEmpty()) {
                                            Text(exp.notes, fontSize = 12.sp, color = TextSecondary)
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${exp.amount.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DueRed
                                    )
                                    IconButton(onClick = { viewModel.deleteExpense(exp.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Modal Add Expense Dialog
    if (showAddExpenseDialog) {
        var selectedCat by remember { mutableStateOf("FUEL") }
        var amountInput by remember { mutableStateOf("") }
        var litresInput by remember { mutableStateOf("") }
        var notesInput by remember { mutableStateOf("") }
        var selectedVeh by remember { mutableStateOf(vehicles.firstOrNull()) }

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = {
                Text(Strings.get("add_expense", lang), fontWeight = FontWeight.Bold, color = ForestGreen)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Category Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("FUEL" to "डीजल", "MAINTENANCE" to "मेंटेनेंस", "OTHER" to "अन्य").forEach { (cat, label) ->
                            val isSelected = selectedCat == cat
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) ForestGreen else CardWhite,
                                border = BorderStroke(1.dp, ForestGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCat = cat }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else ForestGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    KhetInputField(
                        label = Strings.get("amount", lang) + " *",
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        placeholder = "₹0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    if (selectedCat == "FUEL") {
                        Spacer(modifier = Modifier.height(8.dp))
                        KhetInputField(
                            label = Strings.get("litres", lang) + " *",
                            value = litresInput,
                            onValueChange = { litresInput = it },
                            placeholder = "उदा: 40",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    KhetInputField(
                        label = Strings.get("notes", lang),
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        placeholder = "उदा: इंडियन ऑयल पंप"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountInput.toDoubleOrNull() ?: 0.0
                        if (amt > 0 && selectedVeh != null) {
                            viewModel.saveExpense(
                                vehicleId = selectedVeh!!.id,
                                vehicleName = selectedVeh!!.name,
                                category = selectedCat,
                                amount = amt,
                                litres = litresInput.toDoubleOrNull() ?: 0.0,
                                notes = notesInput
                            )
                            showAddExpenseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("सेव करें", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text("रद्द करें", color = TextPrimary)
                }
            }
        )
    }
}
