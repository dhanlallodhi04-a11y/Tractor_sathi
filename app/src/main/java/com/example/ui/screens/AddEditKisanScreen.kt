package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import kotlin.math.abs
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CustomerEntity
import com.example.ui.KhetSaathiViewModel
import com.example.ui.components.KhetCard
import com.example.ui.components.KhetInputField
import com.example.ui.components.VoiceInputDialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.DueRed
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Strings
import com.example.util.VoiceParsedResult
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditKisanScreen(
    kisanId: String?, // null if adding new
    viewModel: KhetSaathiViewModel,
    onNavigateBack: () -> Unit,
    onOpenExisting: (String) -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()

    val existingCustomer = remember(kisanId, customers) {
        if (kisanId != null) customers.find { it.id == kisanId } else null
    }

    var nameInput by remember { mutableStateOf(existingCustomer?.name ?: "") }
    var phoneInput by remember {
        mutableStateOf(
            existingCustomer?.phone?.replace("+91", "")?.trim() ?: ""
        )
    }
    var addressInput by remember { mutableStateOf(existingCustomer?.village ?: "") }

    var dueDateMillis by remember { mutableStateOf(existingCustomer?.dueDateMillis) }
    var dueDayOfMonth by remember { mutableStateOf(existingCustomer?.dueDayOfMonth) }
    var repeatDays by remember { mutableStateOf(existingCustomer?.repeatDays ?: 5) }
    var remindersEnabled by remember { mutableStateOf(existingCustomer?.remindersEnabled ?: true) }

    var nameAttemptedEmpty by remember { mutableStateOf(false) }
    var phoneValidationError by remember { mutableStateOf<String?>(null) }
    var showDuplicateWarningDialog by remember { mutableStateOf<CustomerEntity?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var showVoiceDialogForName by remember { mutableStateOf(false) }
    var showVoiceDialogForAddress by remember { mutableStateOf(false) }

    fun validatePhone(phone: String): Boolean {
        val clean = phone.replace("+91", "").replace(" ", "").trim()
        if (clean.isEmpty()) {
            phoneValidationError = null
            return true
        }
        if (clean.length == 10 && clean.all { it.isDigit() }) {
            phoneValidationError = null
            return true
        } else {
            phoneValidationError = "कृपया सही 10-अंकों का मोबाइल नंबर दर्ज करें (Enter valid 10-digit number)"
            return false
        }
    }

    fun proceedSave(forceAnyway: Boolean = false) {
        if (isSaving) return
        isSaving = true
        val trimmedName = nameInput.trim()
        if (trimmedName.length < 2) {
            nameAttemptedEmpty = true
            isSaving = false
            return
        }
        val cleanPhoneDigits = phoneInput.replace("+91", "").replace(" ", "").trim()
        if (cleanPhoneDigits.isNotEmpty() && !validatePhone(cleanPhoneDigits)) {
            isSaving = false
            return
        }

        val fullPhone = if (cleanPhoneDigits.isNotEmpty()) "+91 $cleanPhoneDigits" else ""

        // Duplicate check: if the same name AND same mobile already exist (and not the same record)
        if (!forceAnyway && fullPhone.isNotEmpty()) {
            val duplicate = customers.find {
                it.id != (existingCustomer?.id ?: "") &&
                it.name.trim().equals(trimmedName, ignoreCase = true) &&
                it.phone.replace("+91", "").replace(" ", "").trim() == cleanPhoneDigits
            }
            if (duplicate != null) {
                isSaving = false
                showDuplicateWarningDialog = duplicate
                return
            }
        }

        val customerToSave = CustomerEntity(
            id = existingCustomer?.id ?: UUID.randomUUID().toString(),
            name = trimmedName,
            phone = fullPhone,
            village = addressInput.trim(),
            notes = existingCustomer?.notes ?: "",
            dueDateMillis = dueDateMillis,
            dueDayOfMonth = dueDayOfMonth,
            repeatDays = repeatDays,
            remindersEnabled = remindersEnabled
        )
        viewModel.saveCustomer(customerToSave)
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingCustomer == null) Strings.get("add_kisan", lang) else "${Strings.get("edit", lang)} ${existingCustomer.name}",
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
        },
        bottomBar = {
            // Sticky Save button
            Surface(
                color = CardWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { proceedSave(forceAnyway = false) },
                        enabled = nameInput.trim().length >= 2 && !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_kisan_button")
                    ) {
                        Text(
                            text = Strings.get("save", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            // 1. Kisan name (किसान का नाम): REQUIRED. Min 2 chars. Mic button. Red "Name is required"
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        KhetInputField(
                            label = "${Strings.get("kisan_name", lang)} *",
                            value = nameInput,
                            onValueChange = {
                                nameInput = it
                                if (it.trim().length >= 2) nameAttemptedEmpty = false
                            },
                            placeholder = "उदा: रामेश्वर पाटीदार",
                            trailingIcon = {
                                IconButton(onClick = { showVoiceDialogForName = true }) {
                                    Icon(Icons.Default.Mic, contentDescription = "Voice input", tint = AmberGold)
                                }
                            },
                            testTag = "kisan_name_input"
                        )
                        if (nameAttemptedEmpty && nameInput.trim().length < 2) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Strings.get("name_required", lang),
                                color = DueRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Mobile number (मोबाइल नंबर): OPTIONAL. 10-digit India format with +91 prefix
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = Strings.get("phone", lang),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Fixed +91 prefix badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CreamBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                                modifier = Modifier
                                    .height(52.dp)
                                    .padding(end = 8.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = "+91",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            // 10-digit Phone input
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .background(CardWhite, RoundedCornerShape(12.dp))
                                    .border(1.dp, if (phoneValidationError != null) DueRed else BorderGray, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (phoneInput.isEmpty()) {
                                    Text("9876543210", style = TextStyle(fontSize = 16.sp, color = TextMuted))
                                }
                                BasicTextField(
                                    value = phoneInput,
                                    onValueChange = {
                                        phoneInput = it.filter { ch -> ch.isDigit() }.take(10)
                                        validatePhone(phoneInput)
                                    },
                                    textStyle = TextStyle(fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Medium),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    cursorBrush = SolidColor(ForestGreen),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        if (phoneValidationError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = phoneValidationError!!,
                                color = DueRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. Address / Village (पता / गाँव): OPTIONAL. Multi-line text with mic button.
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Strings.get("village", lang),
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                            IconButton(onClick = { showVoiceDialogForAddress = true }) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice address", tint = AmberGold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(CardWhite, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (addressInput.isEmpty()) {
                                Text("उदा: रामपुर, तालाब के पास", color = TextMuted, fontSize = 14.sp)
                            }
                            BasicTextField(
                                value = addressInput,
                                onValueChange = { addressInput = it },
                                textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
                                cursorBrush = SolidColor(ForestGreen),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // 4. Due Date & Reminder Settings (पैसे लेने की तारीख व रिमाइंडर)
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "पैसे लेने की तारीख व रिमाइंडर",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextPrimary
                                )
                            }
                            Switch(
                                checked = remindersEnabled,
                                onCheckedChange = { remindersEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CardWhite,
                                    checkedTrackColor = ForestGreen
                                )
                            )
                        }

                        if (remindersEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "पैसे देने की तारीख (Due Date):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val now = System.currentTimeMillis()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val tomorrow = now + 86400000L
                                val in3Days = now + (3L * 86400000L)
                                val in1Week = now + (7L * 86400000L)

                                val isTomorrow = dueDateMillis != null && abs(dueDateMillis!! - tomorrow) < 43200000L
                                val isIn3Days = dueDateMillis != null && abs(dueDateMillis!! - in3Days) < 43200000L
                                val isIn1Week = dueDateMillis != null && abs(dueDateMillis!! - in1Week) < 43200000L
                                val isMonthly10 = dueDayOfMonth == 10

                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isTomorrow) ForestGreen else CardWhite,
                                    border = BorderStroke(1.dp, if (isTomorrow) ForestGreen else BorderGray),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            dueDateMillis = tomorrow
                                            dueDayOfMonth = null
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                        Text("कल", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isTomorrow) CardWhite else TextPrimary)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isIn3Days) ForestGreen else CardWhite,
                                    border = BorderStroke(1.dp, if (isIn3Days) ForestGreen else BorderGray),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            dueDateMillis = in3Days
                                            dueDayOfMonth = null
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                        Text("3 दिन", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isIn3Days) CardWhite else TextPrimary)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isIn1Week) ForestGreen else CardWhite,
                                    border = BorderStroke(1.dp, if (isIn1Week) ForestGreen else BorderGray),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            dueDateMillis = in1Week
                                            dueDayOfMonth = null
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                        Text("हफ्ता", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isIn1Week) CardWhite else TextPrimary)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isMonthly10) ForestGreen else CardWhite,
                                    border = BorderStroke(1.dp, if (isMonthly10) ForestGreen else BorderGray),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            dueDayOfMonth = 10
                                            dueDateMillis = null
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                        Text("10 तारीख", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isMonthly10) CardWhite else TextPrimary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "दोबारा याद दिलाएं (Repeat Interval):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(3, 5, 7, 10).forEach { days ->
                                    val isSelected = repeatDays == days
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (isSelected) AmberGold else CardWhite,
                                        border = BorderStroke(1.dp, if (isSelected) AmberGold else BorderGray),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { repeatDays = days }
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                            Text(
                                                text = if (days == 5) "5 दिन (मुख्य)" else "$days दिन",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) CardWhite else TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(90.dp)) }
        }
    }

    // Duplicate Check Warning Dialog
    showDuplicateWarningDialog?.let { duplicateKisan ->
        AlertDialog(
            onDismissRequest = { showDuplicateWarningDialog = null },
            title = {
                Text("यह किसान पहले से दर्ज हो सकता है", fontWeight = FontWeight.Bold, color = AmberGold)
            },
            text = {
                Text("किसान '${duplicateKisan.name}' और मोबाइल '${duplicateKisan.phone}' पहले से सूची में मौजूद है। क्या आप नया बनाना चाहते हैं या मौजूदा किसान खोलना चाहते हैं?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDuplicateWarningDialog = null
                        proceedSave(forceAnyway = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("फिर भी सेव करें", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val targetId = duplicateKisan.id
                    showDuplicateWarningDialog = null
                    onOpenExisting(targetId)
                }) {
                    Text("मौजूदा खोलें", color = ForestGreen, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Voice Dialogs
    if (showVoiceDialogForName) {
        VoiceInputDialog(
            onDismiss = { showVoiceDialogForName = false },
            onResultConfirmed = { draft ->
                val txt = when (draft) {
                    is VoiceParsedResult.PaymentDraft -> draft.customerName
                    is VoiceParsedResult.JobDraft -> draft.customerName ?: draft.originalText
                    is VoiceParsedResult.Unrecognized -> draft.originalText
                }
                if (txt.isNotBlank()) nameInput = txt
            },
            lang = lang
        )
    }

    if (showVoiceDialogForAddress) {
        VoiceInputDialog(
            onDismiss = { showVoiceDialogForAddress = false },
            onResultConfirmed = { draft ->
                val txt = when (draft) {
                    is VoiceParsedResult.Unrecognized -> draft.originalText
                    is VoiceParsedResult.JobDraft -> draft.originalText
                    is VoiceParsedResult.PaymentDraft -> draft.originalText
                }
                if (txt.isNotBlank()) addressInput = txt
            },
            lang = lang
        )
    }
}
