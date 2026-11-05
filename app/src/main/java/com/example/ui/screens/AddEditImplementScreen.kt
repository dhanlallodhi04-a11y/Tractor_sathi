package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.ImplementEntity
import com.example.ui.KhetSaathiViewModel
import com.example.ui.components.KhetCard
import com.example.ui.components.KhetInputField
import com.example.ui.components.VoiceInputDialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberLight
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.DueRed
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ImageCompressor
import com.example.util.Strings
import com.example.util.VoiceParsedResult
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditImplementScreen(
    implementId: String?, // null if adding new
    viewModel: KhetSaathiViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val implements by viewModel.implements.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSaving by remember { mutableStateOf(false) }

    val existingImplement = remember(implementId, implements) {
        if (implementId != null) implements.find { it.id == implementId } else null
    }

    // Form state values
    var photoUriString by remember { mutableStateOf<String?>(existingImplement?.photo) }
    var nameInput by remember { mutableStateOf(existingImplement?.name ?: "") }
    var rateInput by remember { mutableStateOf(existingImplement?.defaultRate?.toInt()?.toString() ?: "800") }
    var selectedRateType by remember { mutableStateOf(existingImplement?.rateType ?: "HOUR") }
    var customUnitNameInput by remember { mutableStateOf(existingImplement?.customUnitName ?: "") }
    var detailsInput by remember { mutableStateOf(existingImplement?.details ?: "") }
    var linkedVehiclesIds by remember { mutableStateOf(existingImplement?.vehicleIds ?: "ALL") }
    var activeToggle by remember { mutableStateOf(existingImplement?.active ?: true) }

    // Dialogs
    var showCustomUnitDialog by remember { mutableStateOf(false) }
    var showPhotoOptionsSheet by remember { mutableStateOf(false) }
    var showVoiceDialogForRate by remember { mutableStateOf(false) }
    var showVoiceDialogForDetails by remember { mutableStateOf(false) }

    // Extract saved custom units across implements
    val existingCustomUnits = remember(implements) {
        implements.mapNotNull { it.customUnitName?.trim() }.filter { it.isNotBlank() }.distinct()
    }

    // Photo picker with safe downsampling and compression
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val compressedUri = ImageCompressor.compressAndSaveImage(context, uri, maxDimension = 800)
                photoUriString = compressedUri ?: uri.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingImplement == null) Strings.get("add_implement", lang) else Strings.get("edit", lang) + " " + (existingImplement.hindiName.ifEmpty { existingImplement.name }),
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Sticky Save Button
            Surface(
                color = CardWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            if (isSaving) return@Button
                            isSaving = true
                            val trimmedName = nameInput.trim()
                            val parsedRate = rateInput.trim().toDoubleOrNull()
                            if (trimmedName.isEmpty()) {
                                isSaving = false
                                scope.launch { snackbarHostState.showSnackbar("कृपया इम्प्लीमेंट का नाम दर्ज करें") }
                                return@Button
                            }
                            if (parsedRate == null || parsedRate <= 0) {
                                isSaving = false
                                scope.launch { snackbarHostState.showSnackbar("कृपया वैध डिफ़ॉल्ट रेट दर्ज करें") }
                                return@Button
                            }

                            val toSave = ImplementEntity(
                                id = existingImplement?.id ?: UUID.randomUUID().toString(),
                                name = trimmedName,
                                hindiName = trimmedName,
                                photo = photoUriString,
                                icon = existingImplement?.icon ?: "rotavator",
                                rateType = selectedRateType,
                                customUnitName = if (selectedRateType == "CUSTOM") customUnitNameInput.trim() else null,
                                defaultRate = parsedRate,
                                details = detailsInput.trim(),
                                vehicleIds = linkedVehiclesIds,
                                active = activeToggle,
                                sortOrder = existingImplement?.sortOrder ?: 0,
                                usageCount = existingImplement?.usageCount ?: 0,
                                createdAt = existingImplement?.createdAt ?: System.currentTimeMillis()
                            )

                            viewModel.saveImplement(toSave)
                            onNavigateBack()
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_implement_button")
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

            // 1. Photo (optional): big rectangular upload area at top ("Add photo": Take photo / Choose from gallery)
            item {
                KhetCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(ForestGreenLight)
                                .border(BorderStroke(1.5.dp, BorderGray), RoundedCornerShape(16.dp))
                                .clickable { showPhotoOptionsSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!photoUriString.isNullOrBlank()) {
                                AsyncImage(
                                    model = photoUriString,
                                    contentDescription = "Implement Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Clear Photo Button
                                IconButton(
                                    onClick = { photoUriString = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(36.dp)
                                        .background(CardWhite.copy(alpha = 0.9f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = DueRed, modifier = Modifier.size(18.dp))
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = Strings.get("add_photo", lang),
                                        tint = ForestGreen,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = Strings.get("add_photo", lang),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                    Text(
                                        text = "गैलरी या कैमरे से फोटो अपलोड करें",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Implement name (required), e.g. Rotavator, Cultivator, Trolley
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        KhetInputField(
                            label = "${Strings.get("implement_name", lang)} *",
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            placeholder = "उदा: रोटावेटर (Rotavator)",
                            testTag = "implement_name_input"
                        )
                    }
                }
            }

            // 3. Rate (₹) (required): default rate, numeric keypad, mic button
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        KhetInputField(
                            label = "${Strings.get("default_rate", lang)} *",
                            value = rateInput,
                            onValueChange = { rateInput = it },
                            placeholder = "800",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = {
                                IconButton(onClick = { showVoiceDialogForRate = true }) {
                                    Icon(Icons.Default.Mic, contentDescription = "Voice input rate", tint = AmberGold)
                                }
                            },
                            testTag = "implement_rate_input"
                        )
                    }
                }
            }

            // 4. Rate type (required): wrap-style chips, single select:
            // Per hour (default), Per acre, Per bigha, Per hectare, Per guntha, Per kanal, Per marla, Per cent, Per chakkar (trip), Per day, Fixed price, + Custom unit
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${Strings.get("rate_type", lang)} *",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val standardUnits = listOf(
                            "HOUR" to Strings.get("per_hour", lang),
                            "ACRE" to Strings.get("per_acre", lang),
                            "BIGHA" to Strings.get("per_bigha", lang),
                            "HECTARE" to Strings.get("per_hectare", lang),
                            "GUNTHA" to Strings.get("per_guntha", lang),
                            "KANAL" to Strings.get("per_kanal", lang),
                            "MARLA" to Strings.get("per_marla", lang),
                            "CENT" to Strings.get("per_cent", lang),
                            "CHAKKAR" to Strings.get("per_chakkar", lang),
                            "DAY" to Strings.get("per_day", lang),
                            "FIXED" to Strings.get("fixed", lang)
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            standardUnits.forEach { (unitKey, label) ->
                                val isSelected = selectedRateType == unitKey
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) ForestGreen else CardWhite,
                                    border = BorderStroke(1.5.dp, ForestGreen),
                                    modifier = Modifier.clickable { selectedRateType = unitKey }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else ForestGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    )
                                }
                            }

                            // Saved Custom Units chips
                            existingCustomUnits.forEach { customName ->
                                val isSelected = selectedRateType == "CUSTOM" && customUnitNameInput.equals(customName, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) ForestGreen else AmberLight,
                                    border = BorderStroke(1.5.dp, if (isSelected) ForestGreen else AmberGold),
                                    modifier = Modifier.clickable {
                                        selectedRateType = "CUSTOM"
                                        customUnitNameInput = customName
                                    }
                                ) {
                                    Text(
                                        text = customName,
                                        color = if (isSelected) Color.White else Color(0xFF8A5A00),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    )
                                }
                            }

                            // "+ Custom unit" chip
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = CardWhite,
                                border = BorderStroke(1.5.dp, AmberGold),
                                modifier = Modifier.clickable { showCustomUnitDialog = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = Strings.get("custom_unit", lang),
                                        color = Color(0xFF8A5A00),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        if (selectedRateType == "CUSTOM" && customUnitNameInput.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = ForestGreenLight,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "चुना गया कस्टम यूनिट: \"$customUnitNameInput\"",
                                    color = ForestGreenDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Details (optional): multi-line text box with mic button and quick-fill suggestion chips
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Strings.get("details", lang),
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                            IconButton(onClick = { showVoiceDialogForDetails = true }) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice details", tint = AmberGold)
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
                            if (detailsInput.isEmpty()) {
                                Text(
                                    text = "उदा: 8 feet wide • 42 blades या 9 tines",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                            }
                            BasicTextField(
                                value = detailsInput,
                                onValueChange = { detailsInput = it },
                                textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
                                cursorBrush = SolidColor(ForestGreen),
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "त्वरित सुझाव (Quick-fill chips):",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val suggestionChips = listOf(
                            "__ feet wide",
                            "__ tines",
                            "__ blades",
                            "__ discs",
                            "__ HP required"
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            suggestionChips.forEach { chipText ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = CreamBackground,
                                    border = BorderStroke(1.dp, BorderGray),
                                    modifier = Modifier.clickable {
                                        detailsInput = if (detailsInput.isBlank()) chipText else "$detailsInput • $chipText"
                                    }
                                ) {
                                    Text(
                                        text = "+ $chipText",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Linked vehicles (optional, "All" by default) and Active toggle
            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = Strings.get("linked_vehicles", lang),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isAll = linkedVehiclesIds == "ALL" || linkedVehiclesIds.isBlank()
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isAll) ForestGreen else CardWhite,
                                border = BorderStroke(1.5.dp, ForestGreen),
                                modifier = Modifier.clickable { linkedVehiclesIds = "ALL" }
                            ) {
                                Text(
                                    text = Strings.get("all_vehicles", lang),
                                    color = if (isAll) Color.White else ForestGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }

                            vehicles.forEach { v ->
                                val isSelected = linkedVehiclesIds.contains(v.id)
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isSelected) ForestGreen else CardWhite,
                                    border = BorderStroke(1.5.dp, ForestGreen),
                                    modifier = Modifier.clickable {
                                        linkedVehiclesIds = if (isSelected) {
                                            "ALL"
                                        } else {
                                            v.id
                                        }
                                    }
                                ) {
                                    Text(
                                        text = v.name,
                                        color = if (isSelected) Color.White else ForestGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Active Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = Strings.get("active_status", lang),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "काम जोड़ते समय यह यंत्र सूची में दिखेगा",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                            Switch(
                                checked = activeToggle,
                                onCheckedChange = { activeToggle = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ForestGreen,
                                    checkedTrackColor = ForestGreenLight
                                )
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(90.dp)) }
        }
    }

    // Modal Photo Options Bottom Sheet
    if (showPhotoOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoOptionsSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = CardWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = Strings.get("add_photo", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Choose from Gallery
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CreamBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPhotoOptionsSheet = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = ForestGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("गैलरी से चुनें (Choose from gallery)", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Take Photo
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CreamBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPhotoOptionsSheet = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AmberGold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("कैमरे से फोटो लें (Take photo)", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Modal Dialog: Add Custom Unit
    if (showCustomUnitDialog) {
        var tempUnitInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomUnitDialog = false },
            title = {
                Text(Strings.get("custom_unit", lang), fontWeight = FontWeight.Bold, color = ForestGreen)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "नया किराया यूनिट नाम दर्ज करें (उदा: per bag, per quintal, प्रति बोरी):",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    KhetInputField(
                        label = "यूनिट नाम",
                        value = tempUnitInput,
                        onValueChange = { tempUnitInput = it },
                        placeholder = "उदा: प्रति क्विंटल"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempUnitInput.isNotBlank()) {
                            selectedRateType = "CUSTOM"
                            customUnitNameInput = tempUnitInput.trim()
                            showCustomUnitDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("जोड़ें (Add)", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomUnitDialog = false }) {
                    Text("रद्द करें", color = TextPrimary)
                }
            }
        )
    }

    // Voice Dialog for Rate
    if (showVoiceDialogForRate) {
        VoiceInputDialog(
            onDismiss = { showVoiceDialogForRate = false },
            onResultConfirmed = { draft ->
                when (draft) {
                    is VoiceParsedResult.PaymentDraft -> {
                        rateInput = draft.amount.toInt().toString()
                    }
                    is VoiceParsedResult.JobDraft -> {
                        // Rate might be spoken as number
                    }
                    is VoiceParsedResult.Unrecognized -> {
                        val digitRegex = Regex("""(\d+)""")
                        val match = digitRegex.find(draft.originalText)
                        if (match != null) {
                            rateInput = match.groupValues[1]
                        }
                    }
                }
            },
            lang = lang
        )
    }

    // Voice Dialog for Details
    if (showVoiceDialogForDetails) {
        VoiceInputDialog(
            onDismiss = { showVoiceDialogForDetails = false },
            onResultConfirmed = { draft ->
                val txt = when (draft) {
                    is VoiceParsedResult.JobDraft -> draft.originalText
                    is VoiceParsedResult.PaymentDraft -> draft.originalText
                    is VoiceParsedResult.Unrecognized -> draft.originalText
                }
                if (txt.isNotBlank()) {
                    detailsInput = if (detailsInput.isBlank()) txt else "$detailsInput • $txt"
                }
            },
            lang = lang
        )
    }
}
