package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CustomerEntity
import com.example.data.ImplementEntity
import com.example.data.JobEntity
import com.example.data.VehicleEntity
import com.example.ui.KhetSaathiViewModel
import com.example.ui.components.KhetCard
import com.example.ui.components.KhetInputField
import com.example.ui.components.StepperControl
import com.example.ui.components.VoiceInputDialog
import com.example.ui.theme.*
import com.example.util.I18n
import com.example.util.Strings
import com.example.util.VoiceParsedResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddJobScreen(
    viewModel: KhetSaathiViewModel,
    onNavigateBack: () -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val implements by viewModel.implements.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val activeVoiceDraft by viewModel.activeVoiceDraft.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSavingJob by remember { mutableStateOf(false) }

    // Kisan Selection States
    var selectedCustomerId by remember { mutableStateOf<String?>(null) }
    var selectedCustomerName by remember { mutableStateOf("") }
    var kisanSearchQuery by remember { mutableStateOf("") }
    var showKisanPickerSheet by remember { mutableStateOf(false) }
    var showInlineAddKisanSheet by remember { mutableStateOf(false) }
    var inlineKisanPrefillName by remember { mutableStateOf("") }

    // Vehicle and Implement Selection
    var selectedVehicle by remember { mutableStateOf<VehicleEntity?>(null) }
    var selectedImplement by remember { mutableStateOf<ImplementEntity?>(null) }
    var jobStatus by remember { mutableStateOf("COMPLETED") } // COMPLETED or SCHEDULED

    // Rate & Unit Details
    var rateType by remember { mutableStateOf("HOUR") }
    var currentRate by remember { mutableDoubleStateOf(800.0) }
    var isEditingRate by remember { mutableStateOf(false) }
    var alsoUpdateDefaultRate by remember { mutableStateOf(false) }
    var editedRateInput by remember { mutableStateOf("800") }

    // Unit inputs
    var hours by remember { mutableIntStateOf(1) }
    var minutes by remember { mutableIntStateOf(0) }
    var areaInput by remember { mutableStateOf("1.0") }
    var tripsCount by remember { mutableIntStateOf(1) }
    var daysCount by remember { mutableIntStateOf(1) }
    var customQuantityInput by remember { mutableStateOf("1.0") }

    // Optional Collapsible
    var showOptionalFields by remember { mutableStateOf(false) }
    var extraChargesInput by remember { mutableStateOf("") }
    var discountInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    // Payment Received
    var paymentReceivedInput by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }

    var savedJobJustNow by remember { mutableStateOf<JobEntity?>(null) }
    var showVoiceDialog by remember { mutableStateOf(false) }

    // Pre-select default vehicle & first implement
    LaunchedEffect(vehicles) {
        if (selectedVehicle == null && vehicles.isNotEmpty()) {
            selectedVehicle = vehicles.find { it.isDefault } ?: vehicles.first()
        }
    }

    LaunchedEffect(implements) {
        if (selectedImplement == null && implements.isNotEmpty()) {
            val imp = implements.first()
            selectedImplement = imp
            rateType = imp.rateType
            currentRate = imp.defaultRate
            editedRateInput = imp.defaultRate.toInt().toString()
        }
    }

    // Apply voice draft if present
    LaunchedEffect(activeVoiceDraft) {
        activeVoiceDraft?.let { draft ->
            when (draft) {
                is VoiceParsedResult.JobDraft -> {
                    hours = draft.hours
                    minutes = draft.minutes
                    if (draft.area > 0) areaInput = draft.area.toString()
                    val matchedImp = implements.find {
                        it.hindiName.contains(draft.implementName) ||
                        it.name.contains(draft.implementName, ignoreCase = true) ||
                        draft.implementName.contains(it.hindiName)
                    }
                    if (matchedImp != null) {
                        selectedImplement = matchedImp
                        rateType = matchedImp.rateType
                        currentRate = matchedImp.defaultRate
                        editedRateInput = matchedImp.defaultRate.toInt().toString()
                    }
                }
                is VoiceParsedResult.PaymentDraft -> {
                    paymentReceivedInput = draft.amount.toInt().toString()
                    paymentMethod = draft.method
                }
                else -> {}
            }
            viewModel.setVoiceDraft(null)
        }
    }

    // Live calculation
    val calculatedTotal by remember {
        derivedStateOf {
            val base = when (rateType) {
                "HOUR" -> (hours + (minutes / 60.0)) * currentRate
                "ACRE", "BIGHA", "HECTARE", "GUNTHA", "KANAL", "MARLA", "CENT" -> {
                    (areaInput.toDoubleOrNull() ?: 0.0) * currentRate
                }
                "CHAKKAR" -> tripsCount * currentRate
                "DAY" -> daysCount * currentRate
                "FIXED" -> currentRate
                else -> (customQuantityInput.toDoubleOrNull() ?: 0.0) * currentRate
            }
            val extra = extraChargesInput.toDoubleOrNull() ?: 0.0
            val disc = discountInput.toDoubleOrNull() ?: 0.0
            (base + extra - disc).coerceAtLeast(0.0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (lang == "hi") "नया कार्य जोड़ें" else "Add New Job",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("add_job_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showVoiceDialog = true }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = AmberGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ForestGreen)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Sticky Save Job Bar
            Surface(
                color = CardWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (savedJobJustNow != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    // Reset for "Do this job again"
                                    savedJobJustNow = null
                                    selectedCustomerId = null
                                    selectedCustomerName = ""
                                    paymentReceivedInput = ""
                                },
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = null, tint = ForestGreen)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Strings.get("do_again", lang), color = ForestGreen, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onNavigateBack,
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lang == "hi") "पूर्ण (Done)" else "Done", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (isSavingJob) return@Button
                                isSavingJob = true
                                if (selectedCustomerId == null || selectedCustomerName.isBlank()) {
                                    isSavingJob = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar(if (lang == "hi") "कृपया किसान चुनें" else "Please select a Kisan")
                                    }
                                    return@Button
                                }
                                if (selectedImplement == null) {
                                    isSavingJob = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar(if (lang == "hi") "कृपया यंत्र चुनें" else "Please select an implement")
                                    }
                                    return@Button
                                }
                                if (selectedVehicle == null) {
                                    isSavingJob = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar(if (lang == "hi") "कृपया ट्रैक्टर चुनें" else "Please select a vehicle")
                                    }
                                    return@Button
                                }

                                val paid = paymentReceivedInput.toDoubleOrNull() ?: 0.0
                                val isAreaUnit = rateType in listOf("ACRE", "BIGHA", "HECTARE", "GUNTHA", "KANAL", "MARLA", "CENT")
                                val qtyValue = when (rateType) {
                                    "CHAKKAR" -> tripsCount.toDouble()
                                    "DAY" -> daysCount.toDouble()
                                    "FIXED" -> 1.0
                                    "HOUR" -> 0.0
                                    else -> if (isAreaUnit) 0.0 else (customQuantityInput.toDoubleOrNull() ?: 1.0)
                                }

                                val job = JobEntity(
                                    customerId = selectedCustomerId!!,
                                    customerName = selectedCustomerName,
                                    vehicleId = selectedVehicle!!.id,
                                    vehicleName = selectedVehicle!!.name,
                                    implementId = selectedImplement!!.id,
                                    implementName = selectedImplement!!.hindiName.ifEmpty { selectedImplement!!.name },
                                    implementDetailsSnapshot = selectedImplement?.details ?: "",
                                    dateMillis = System.currentTimeMillis(),
                                    status = jobStatus,
                                    rateTypeSnapshot = rateType,
                                    customUnitSnapshot = selectedImplement?.customUnitName ?: "",
                                    rateSnapshot = currentRate,
                                    hours = if (rateType == "HOUR") hours else 0,
                                    minutes = if (rateType == "HOUR") minutes else 0,
                                    area = if (isAreaUnit) areaInput.toDoubleOrNull() ?: 0.0 else 0.0,
                                    quantity = qtyValue,
                                    extras = extraChargesInput.toDoubleOrNull() ?: 0.0,
                                    discount = discountInput.toDoubleOrNull() ?: 0.0,
                                    total = (calculatedTotal * 100).roundToInt() / 100.0,
                                    paidAmount = paid,
                                    paymentMethod = if (paid > 0) paymentMethod else "NONE",
                                    notes = notesInput
                                )
                                viewModel.saveJob(
                                    job = job,
                                    alsoUpdateDefaultRate = alsoUpdateDefaultRate,
                                    newDefaultRate = currentRate
                                )
                                savedJobJustNow = job
                                isSavingJob = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(if (lang == "hi") "कार्य सफलतापूर्वक सुरक्षित हुआ!" else "Job saved successfully!")
                                }
                            },
                            enabled = !isSavingJob,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("save_job_submit_button")
                        ) {
                            Text(
                                text = "${if (lang == "hi") "कार्य सेव करें" else "Save Job"} (₹${calculatedTotal.toInt()})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // Section 1: Kisan (Farmer)
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (lang == "hi") "किसान (Kisan) *" else "Kisan *",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Kisan Selection Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(CardWhite, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                                .clickable { showKisanPickerSheet = true }
                                .padding(horizontal = 14.dp)
                                .testTag("kisan_dropdown_selector"),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (selectedCustomerName.isNotEmpty()) ForestGreen else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedCustomerName.isNotEmpty()) selectedCustomerName else (if (lang == "hi") "किसान चुनें / खोजें" else "Select / Search Kisan"),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (selectedCustomerName.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedCustomerName.isNotEmpty()) ForestGreen else TextMuted
                                    )
                                }
                                Text(
                                    text = if (selectedCustomerName.isNotEmpty()) (if (lang == "hi") "बदलें >" else "Change >") else (if (lang == "hi") "खोजें >" else "Search >"),
                                    color = ForestGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Recent Kisan Chips
                        Text(
                            text = if (lang == "hi") "हाल के किसान (Recent):" else "Recent Kisans:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            customers.take(5).forEach { c ->
                                val isSelected = selectedCustomerId == c.id
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) ForestGreenLight else CardWhite,
                                    border = BorderStroke(1.dp, if (isSelected) ForestGreen else BorderGray),
                                    modifier = Modifier.clickable {
                                        selectedCustomerId = c.id
                                        selectedCustomerName = c.name
                                    }
                                ) {
                                    Text(
                                        text = c.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) ForestGreen else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            // Inline Add Kisan chip
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = AmberLight,
                                border = BorderStroke(1.dp, AmberGold),
                                modifier = Modifier
                                    .clickable {
                                        inlineKisanPrefillName = ""
                                        showInlineAddKisanSheet = true
                                    }
                                    .testTag("add_new_kisan_inline_chip")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (lang == "hi") "+ नया किसान" else "+ New Kisan",
                                        color = Color(0xFF8A5A00),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section 2: Date & Status Toggle (Completed / Scheduled)
            item {
                KhetCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = Strings.get("date", lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            val todayStr = remember { SimpleDateFormat("dd MMM yyyy", Locale("hi", "IN")).format(Date()) }
                            Text(
                                text = todayStr,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Completed / Scheduled toggle
                        Row(
                            modifier = Modifier
                                .background(CreamBackground, RoundedCornerShape(24.dp))
                                .padding(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (jobStatus == "COMPLETED") ForestGreen else Color.Transparent,
                                modifier = Modifier.clickable { jobStatus = "COMPLETED" }
                            ) {
                                Text(
                                    text = Strings.get("completed", lang),
                                    color = if (jobStatus == "COMPLETED") Color.White else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (jobStatus == "SCHEDULED") AmberGold else Color.Transparent,
                                modifier = Modifier.clickable { jobStatus = "SCHEDULED" }
                            ) {
                                Text(
                                    text = Strings.get("scheduled", lang),
                                    color = if (jobStatus == "SCHEDULED") Color.White else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section 3: Vehicle Selector
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = Strings.get("tractor_vehicle", lang),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            vehicles.forEach { v ->
                                val isSelected = selectedVehicle?.id == v.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) ForestGreenLight else CardWhite,
                                    border = BorderStroke(1.5.dp, if (isSelected) ForestGreen else BorderGray),
                                    modifier = Modifier.clickable { selectedVehicle = v }
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                        Text(
                                            text = v.name,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) ForestGreen else TextPrimary,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = v.regNo,
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section 4: Implement Tile Grid & Auto-filled Rate
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = Strings.get("implement", lang),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Implements filtered by vehicle if applicable
                        val filteredImplements = remember(implements, selectedVehicle) {
                            val vId = selectedVehicle?.id
                            if (vId == null) implements else {
                                implements.filter { it.vehicleIds == "ALL" || it.vehicleIds.contains(vId) }
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            filteredImplements.forEach { imp ->
                                val isSelected = selectedImplement?.id == imp.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) ForestGreen else CardWhite,
                                    border = BorderStroke(1.5.dp, ForestGreen),
                                    modifier = Modifier
                                        .clickable {
                                            selectedImplement = imp
                                            rateType = imp.rateType
                                            currentRate = imp.defaultRate
                                            editedRateInput = imp.defaultRate.toInt().toString()
                                            isEditingRate = false
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(
                                            text = imp.hindiName.ifEmpty { imp.name },
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else ForestGreen,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (imp.details.isNotBlank()) {
                                            Text(
                                                text = imp.details,
                                                fontSize = 10.sp,
                                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Rate Display & Auto-fill line
                        val unitLabel = I18n.getRateTypeLabel(rateType, lang, selectedImplement?.customUnitName)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ForestGreenLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "दर: ₹${currentRate.toInt()} / $unitLabel",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreenDark
                                    )
                                    Text(
                                        text = if (isEditingRate) "✓ बंद करें" else (if (lang == "hi") "इस कार्य के लिए बदलें" else "Change for this job"),
                                        color = AmberGold,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.clickable {
                                            isEditingRate = !isEditingRate
                                        }
                                    )
                                }

                                if (isEditingRate) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        KhetInputField(
                                            label = "नया रेट (₹)",
                                            value = editedRateInput,
                                            onValueChange = {
                                                editedRateInput = it
                                                val parsed = it.toDoubleOrNull()
                                                if (parsed != null) currentRate = parsed
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = alsoUpdateDefaultRate,
                                            onCheckedChange = { alsoUpdateDefaultRate = it },
                                            colors = CheckboxDefaults.colors(checkedColor = ForestGreen)
                                        )
                                        Text(
                                            text = Strings.get("update_default_rate", lang),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section 5: Dynamic Quantity by Rate-Unit Logic
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        when (rateType) {
                            "HOUR" -> {
                                Text(
                                    text = if (lang == "hi") "समय दर्ज करें (घंटे एवं मिनट):" else "Enter Time (Hours & Minutes):",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(Strings.get("hours", lang), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        StepperControl(
                                            value = hours,
                                            onValueChange = { hours = it },
                                            minValue = 0,
                                            maxValue = 24
                                        )
                                    }
                                    Column {
                                        Text(Strings.get("minutes", lang), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        StepperControl(
                                            value = minutes,
                                            onValueChange = { minutes = it },
                                            minValue = 0,
                                            maxValue = 55,
                                            step = 5
                                        )
                                    }
                                    IconButton(
                                        onClick = { showVoiceDialog = true },
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(AmberLight, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Mic, contentDescription = "Voice", tint = AmberGold)
                                    }
                                }
                            }

                            "ACRE", "BIGHA", "HECTARE", "GUNTHA", "KANAL", "MARLA", "CENT" -> {
                                val areaUnitLabel = I18n.getRateTypeLabel(rateType, lang)
                                KhetInputField(
                                    label = "${if (lang == "hi") "क्षेत्रफल" else "Area"} ($areaUnitLabel) *",
                                    value = areaInput,
                                    onValueChange = { areaInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    trailingIcon = {
                                        IconButton(onClick = { showVoiceDialog = true }) {
                                            Icon(Icons.Default.Mic, contentDescription = null, tint = AmberGold)
                                        }
                                    }
                                )
                            }

                            "CHAKKAR" -> {
                                Text(
                                    text = if (lang == "hi") "चक्कर की संख्या (Trips) *" else "Number of Trips (Chakkar) *",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StepperControl(
                                        value = tripsCount,
                                        onValueChange = { tripsCount = it },
                                        minValue = 1,
                                        maxValue = 100
                                    )
                                    Text(
                                        text = "$tripsCount चक्कर",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = ForestGreen
                                    )
                                }
                            }

                            "DAY" -> {
                                Text(
                                    text = if (lang == "hi") "दिनों की संख्या (Days) *" else "Number of Days *",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StepperControl(
                                        value = daysCount,
                                        onValueChange = { daysCount = it },
                                        minValue = 1,
                                        maxValue = 30
                                    )
                                    Text(
                                        text = "$daysCount दिन",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = ForestGreen
                                    )
                                }
                            }

                            "FIXED" -> {
                                Surface(
                                    color = ForestGreenLight,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = ForestGreen)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (lang == "hi")
                                                "निश्चित दर (Fixed Price) - किसी मात्रा की आवश्यकता नहीं है"
                                            else
                                                "Fixed price job - No quantity input required",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = ForestGreenDark
                                        )
                                    }
                                }
                            }

                            else -> {
                                // CUSTOM unit
                                val customUnit = selectedImplement?.customUnitName ?: (if (lang == "hi") "इकाई" else "Unit")
                                KhetInputField(
                                    label = "${if (lang == "hi") "मात्रा" else "Quantity"} ($customUnit) *",
                                    value = customQuantityInput,
                                    onValueChange = { customQuantityInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Calculation Display: "quantity x rate = ₹total"
                        Surface(
                            color = CardWhite,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, ForestGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = Strings.get("live_calc", lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val equationStr = when (rateType) {
                                    "HOUR" -> "${hours}h ${minutes}m × ₹${currentRate.toInt()}/h = ₹${calculatedTotal.toInt()}"
                                    "ACRE", "BIGHA", "HECTARE", "GUNTHA", "KANAL", "MARLA", "CENT" -> {
                                        val u = I18n.getRateTypeLabel(rateType, lang)
                                        "$areaInput $u × ₹${currentRate.toInt()}/$u = ₹${calculatedTotal.toInt()}"
                                    }
                                    "CHAKKAR" -> "$tripsCount चक्कर × ₹${currentRate.toInt()} = ₹${calculatedTotal.toInt()}"
                                    "DAY" -> "$daysCount दिन × ₹${currentRate.toInt()} = ₹${calculatedTotal.toInt()}"
                                    "FIXED" -> "एकमुश्त निश्चित दर = ₹${calculatedTotal.toInt()}"
                                    else -> {
                                        val u = selectedImplement?.customUnitName ?: "यूनिट"
                                        "$customQuantityInput $u × ₹${currentRate.toInt()} = ₹${calculatedTotal.toInt()}"
                                    }
                                }
                                Text(
                                    text = equationStr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestGreen
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section 6: Optional Collapsed (Extras, Discount, Notes)
            item {
                KhetCard(onClick = { showOptionalFields = !showOptionalFields }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "hi") "अतिरिक्त चार्ज, छूट व विवरण (Optional)" else "Extra charges, discount & notes (Optional)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = if (showOptionalFields) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = ForestGreen
                            )
                        }

                        AnimatedVisibility(visible = showOptionalFields) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    KhetInputField(
                                        label = Strings.get("extra_charges", lang),
                                        value = extraChargesInput,
                                        onValueChange = { extraChargesInput = it },
                                        placeholder = "₹0",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    KhetInputField(
                                        label = Strings.get("discount", lang),
                                        value = discountInput,
                                        onValueChange = { discountInput = it },
                                        placeholder = "₹0",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                KhetInputField(
                                    label = Strings.get("notes", lang),
                                    value = notesInput,
                                    onValueChange = { notesInput = it },
                                    placeholder = if (lang == "hi") "उदा: उत्तर का 5 बीघा खेत" else "e.g. North plot"
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section 7: Payment Received
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = Strings.get("payment_received", lang),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        KhetInputField(
                            label = if (lang == "hi") "जमा रकम (₹)" else "Paid Amount (₹)",
                            value = paymentReceivedInput,
                            onValueChange = { paymentReceivedInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            placeholder = "₹0 (यदि अभी मिला)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Payment Methods
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("CASH" to "नकद", "UPI" to "UPI / PhonePe", "BANK" to "बैंक").forEach { (methodKey, label) ->
                                val isSelected = paymentMethod == methodKey
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) ForestGreen else CardWhite,
                                    border = BorderStroke(1.5.dp, ForestGreen),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { paymentMethod = methodKey }
                                ) {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else ForestGreen,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp)) // padding for sticky bottom bar
            }
        }
    }

    // Modal Searchable Kisan Picker Sheet
    if (showKisanPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showKisanPickerSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "hi") "किसान चुनें" else "Select Kisan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    TextButton(onClick = {
                        inlineKisanPrefillName = kisanSearchQuery.trim()
                        showKisanPickerSheet = false
                        showInlineAddKisanSheet = true
                    }) {
                        Text(if (lang == "hi") "+ नया किसान" else "+ New Kisan", fontWeight = FontWeight.Bold, color = AmberDark)
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = kisanSearchQuery,
                    onValueChange = { kisanSearchQuery = it },
                    placeholder = { Text(if (lang == "hi") "किसान का नाम, फोन या गाँव खोजें..." else "Search by name, phone or village...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        if (kisanSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { kisanSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("kisan_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = Color(0xFFCFCFCF)
                    )
                )

                Spacer(Modifier.height(12.dp))

                val filtered = remember(customers, kisanSearchQuery) {
                    customers.filter {
                        it.name.contains(kisanSearchQuery, ignoreCase = true) ||
                        it.phone.contains(kisanSearchQuery) ||
                        it.village.contains(kisanSearchQuery, ignoreCase = true)
                    }
                }

                // If query is typed and no exact match exists, offer one-tap "+ Add 'query' as new Kisan"
                val exactMatch = customers.any { it.name.equals(kisanSearchQuery.trim(), ignoreCase = true) }
                if (kisanSearchQuery.trim().length >= 2 && !exactMatch) {
                    Surface(
                        onClick = {
                            val nameToSave = kisanSearchQuery.trim()
                            viewModel.saveCustomerInline(name = nameToSave, phone = "", village = "") { newKisan ->
                                selectedCustomerId = newKisan.id
                                selectedCustomerName = newKisan.name
                                showKisanPickerSheet = false
                                kisanSearchQuery = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = AmberLight,
                        border = BorderStroke(1.dp, AmberGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("one_tap_add_kisan_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = AmberDark)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (lang == "hi") "+ '${kisanSearchQuery.trim()}' को नए किसान के रूप में जोड़ें" else "+ Add '${kisanSearchQuery.trim()}' as new Kisan",
                                fontWeight = FontWeight.Bold,
                                color = AmberDark,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                LazyColumn(modifier = Modifier.height(280.dp)) {
                    if (filtered.isEmpty() && kisanSearchQuery.trim().length < 2) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(if (lang == "hi") "कोई किसान नहीं मिला" else "No Kisan found", color = Color.Gray)
                            }
                        }
                    }
                    items(filtered, key = { it.id }) { c ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CreamBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedCustomerId = c.id
                                    selectedCustomerName = c.name
                                    showKisanPickerSheet = false
                                    kisanSearchQuery = ""
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(c.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    val subText = listOf(c.village, c.phone).filter { it.isNotBlank() }.joinToString(" • ")
                                    if (subText.isNotEmpty()) {
                                        Text(subText, fontSize = 12.sp, color = TextMuted)
                                    }
                                }
                                if (selectedCustomerId == c.id) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = ForestGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Inline Add Kisan BottomSheet (Name required, phone & village optional)
    if (showInlineAddKisanSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInlineAddKisanSheet = false },
            containerColor = Color.White
        ) {
            InlineAddKisanSheetContent(
                prefillName = inlineKisanPrefillName,
                existingKisans = customers,
                lang = lang,
                onSave = { name, phone, village ->
                    viewModel.saveCustomerInline(name = name, phone = phone, village = village) { newKisan ->
                        selectedCustomerId = newKisan.id
                        selectedCustomerName = newKisan.name
                        showInlineAddKisanSheet = false
                    }
                },
                onCancel = { showInlineAddKisanSheet = false }
            )
        }
    }

    if (showVoiceDialog) {
        VoiceInputDialog(
            onDismiss = { showVoiceDialog = false },
            onResultConfirmed = { draft ->
                when (draft) {
                    is VoiceParsedResult.JobDraft -> {
                        hours = draft.hours
                        minutes = draft.minutes
                        if (draft.area > 0) areaInput = draft.area.toString()
                        val matched = implements.find {
                            it.hindiName.contains(draft.implementName) ||
                            it.name.contains(draft.implementName, ignoreCase = true)
                        }
                        if (matched != null) {
                            selectedImplement = matched
                            rateType = matched.rateType
                            currentRate = matched.defaultRate
                            editedRateInput = matched.defaultRate.toInt().toString()
                        }
                    }
                    is VoiceParsedResult.PaymentDraft -> {
                        paymentReceivedInput = draft.amount.toInt().toString()
                        paymentMethod = draft.method
                    }
                    else -> {}
                }
            },
            lang = lang
        )
    }
}

@Composable
fun InlineAddKisanSheetContent(
    prefillName: String,
    existingKisans: List<CustomerEntity>,
    lang: String,
    onSave: (name: String, phone: String, village: String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(prefillName) }
    var phoneDigits by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var showDuplicateWarning by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                name = spoken.trim()
                nameError = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (lang == "hi") "+ नया किसान जोड़ें" else "+ Add New Kisan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        // Kisan Name (Required, mic button)
        Column {
            Text(
                text = if (lang == "hi") "किसान का नाम (Kisan Name) *" else "Kisan Name *",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                placeholder = { Text(if (lang == "hi") "उदा. रामलाल जी" else "e.g. Ramesh Patel") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("inline_kisan_name_input"),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lang == "hi") "hi-IN" else "en-IN")
                        }
                        try {
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {
                            // Ignored
                        }
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "Mic", tint = AmberDark)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = if (nameError != null) DueRed else Color(0xFFCFCFCF)
                )
            )
            if (nameError != null) {
                Text(nameError!!, color = DueRed, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        // Mobile Number (Optional, 10-digit)
        Column {
            Text(
                text = if (lang == "hi") "मोबाइल नंबर (वैकल्पिक)" else "Mobile Number (Optional)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = phoneDigits,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(10)
                    phoneDigits = filtered
                    phoneError = null
                },
                leadingIcon = {
                    Text("+91 ", fontWeight = FontWeight.Bold, color = Color.DarkGray, modifier = Modifier.padding(start = 12.dp))
                },
                placeholder = { Text("10 अंकों का नंबर") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("inline_kisan_phone_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = if (phoneError != null) DueRed else Color(0xFFCFCFCF)
                )
            )
            if (phoneError != null) {
                Text(phoneError!!, color = DueRed, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        // Address / Village (Optional)
        Column {
            Text(
                text = if (lang == "hi") "पता / गाँव (वैकल्पिक)" else "Address / Village (Optional)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = village,
                onValueChange = { village = it },
                placeholder = { Text(if (lang == "hi") "गाँव का नाम या ढाणी" else "Village or Area") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("inline_kisan_village_input"),
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
                val cleanName = name.trim()
                if (cleanName.length < 2) {
                    nameError = if (lang == "hi") "नाम अनिवार्य है (कम से कम 2 अक्षर)" else "Name is required (min 2 characters)"
                    return@Button
                }

                if (phoneDigits.isNotBlank() && phoneDigits.length != 10) {
                    phoneError = if (lang == "hi") "10 अंकों का मान्य मोबाइल नंबर दर्ज करें" else "Enter a valid 10-digit mobile number"
                    return@Button
                }

                val finalPhone = if (phoneDigits.isNotBlank()) "+91 $phoneDigits" else ""

                // Duplicate check
                val isDuplicate = existingKisans.any {
                    it.name.equals(cleanName, ignoreCase = true) &&
                    finalPhone.isNotBlank() &&
                    it.phone == finalPhone
                }

                if (isDuplicate && !showDuplicateWarning) {
                    showDuplicateWarning = true
                    return@Button
                }

                onSave(cleanName, finalPhone, village.trim())
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("inline_kisan_save_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
        ) {
            Text(
                text = if (lang == "hi") "किसान जोड़ें एवं चुनें" else "Save & Select Kisan",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        if (showDuplicateWarning) {
            Surface(
                color = AmberLight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (lang == "hi")
                            "⚠️ इस नाम और मोबाइल नंबर का किसान पहले से मौजूद हो सकता है।"
                        else
                            "⚠️ A Kisan with this name and mobile may already exist.",
                        color = AmberDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = {
                            val finalPhone = if (phoneDigits.isNotBlank()) "+91 $phoneDigits" else ""
                            onSave(name.trim(), finalPhone, village.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text(if (lang == "hi") "फिर भी सेव करें" else "Save Anyway", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
