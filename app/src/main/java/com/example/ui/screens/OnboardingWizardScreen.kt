package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ImplementEntity
import com.example.data.UserEntity
import com.example.data.VehicleEntity
import com.example.ui.KhetSaathiViewModel
import com.example.ui.components.ImplementPickerSheet
import com.example.ui.components.TractorPickerSheet
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BorderNeutral
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.MintTint
import com.example.ui.theme.RedDue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarmCreamBg
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWizardScreen(
    viewModel: KhetSaathiViewModel,
    onOnboardingFinished: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentStep by remember { mutableIntStateOf(1) } // 1: Auth, 2: Profile, 3: Tractor, 4: Implements

    // Step 1: Auth state
    var isPhoneOtpSheetOpen by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var otpDigits by remember { mutableStateOf(listOf("", "", "", "", "", "")) }
    var otpSent by remember { mutableStateOf(false) }
    var resendTimer by remember { mutableIntStateOf(30) }
    var isAuthLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    // Step 2: Profile state
    var profileName by remember { mutableStateOf("") }
    var profileVillage by remember { mutableStateOf("") }
    var profileDob by remember { mutableStateOf("1995-05-15") }
    var profilePhotoUri by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    // Voice recognition for profile
    var voiceConfirmationData by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Step 3: Tractors
    val chosenTractors = remember { mutableStateListOf<VehicleEntity>() }
    var isTractorSheetOpen by remember { mutableStateOf(false) }

    // Step 4: Implements
    val chosenImplements = remember { mutableStateListOf<ImplementEntity>() }
    var isImplementSheetOpen by remember { mutableStateOf(false) }

    // Back handler for wizard
    BackHandler(enabled = currentStep > 1) {
        currentStep -= 1
    }

    // Voice recognizer launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                scope.launch {
                    val extracted = viewModel.aiAssistantManager.extractProfileDetails(spoken)
                    voiceConfirmationData = extracted
                }
            }
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            profilePhotoUri = uri.toString()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_wizard_screen"),
        containerColor = WarmCreamBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        IconButton(onClick = { currentStep -= 1 }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = DeepForestGreen)
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    Text(
                        text = "कदम $currentStep / 4",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepForestGreen
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { currentStep / 4f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = DeepForestGreen,
                    trackColor = BorderNeutral
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentStep) {
                // ==========================================
                // STEP 1: WELCOME & AUTH
                // ==========================================
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))
                            // App Logo Circle
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(DeepForestGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Agriculture,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(48.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "किसान साथी",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepForestGreen
                            )
                            Text(
                                text = "ट्रेक्टर मालिक का डिजिटल बहीखाता",
                                fontSize = 14.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CardWhite,
                                border = BorderStroke(1.dp, BorderNeutral),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("✓ रोटावेटर, कल्टीवेटर और ट्रॉली का हिसाब", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("✓ किसान बहीखाता और WhatsApp तकादा", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("✓ डीजल और मिस्त्री खर्च ट्रैकिंग", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("✓ 100% सुरक्षित क्लाउड बैकअप", fontSize = 13.sp, color = DeepForestGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Big Action Buttons
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (authError != null) {
                                Text(
                                    text = authError ?: "",
                                    color = RedDue,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            // Google Button
                            Button(
                                onClick = {
                                    isAuthLoading = true
                                    authError = null
                                    viewModel.authManager.signInWithGoogle(
                                        activity = context as Activity,
                                        onSuccess = { user ->
                                            isAuthLoading = false
                                            // Check if user has existing cloud data to restore
                                            viewModel.restoreCloudData(user.uid) { restored ->
                                                if (restored) {
                                                    onOnboardingFinished()
                                                } else {
                                                    profileName = user.displayName ?: ""
                                                    currentStep = 2
                                                }
                                            }
                                        },
                                        onError = { err ->
                                            isAuthLoading = false
                                            authError = err
                                        },
                                        onCancelled = {
                                            isAuthLoading = false
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("google_login_btn"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DeepForestGreen,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Google से जारी रखें (Sign in)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Phone OTP Button
                            OutlinedButton(
                                onClick = {
                                    isPhoneOtpSheetOpen = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("phone_login_btn"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, DeepForestGreen),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepForestGreen)
                            ) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("मोबाइल नंबर से जारी रखें (Mobile OTP)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ==========================================
                // STEP 2: PROFILE
                // ==========================================
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Text("अपनी प्रोफाइल बनाएं (Profile)", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DeepForestGreen)
                                Text("आपका नाम किसान बहीखाते और बिलिंग रसीद पर दिखेगा", fontSize = 12.sp, color = TextMuted)
                            }

                            // Voice quick fill card
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MintTint,
                                    border = BorderStroke(1.dp, DeepForestGreen.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("आसान बोलकर भरें (Voice Input)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                                            Text("बोलें: \"मेरा नाम रामसिंह है और मैं रामपुर से हूँ\"", fontSize = 11.sp, color = TextMuted)
                                        }
                                        Button(
                                            onClick = {
                                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "अपना नाम और गांव बोलें...")
                                                }
                                                try { voiceLauncher.launch(intent) } catch (_: Exception) {}
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen, contentColor = Color.White)
                                        ) {
                                            Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("बोलें", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            // Voice confirmation modal card
                            if (voiceConfirmationData != null) {
                                val (vName, vVillage) = voiceConfirmationData!!
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                                        border = BorderStroke(1.5.dp, AmberAccent)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("क्या यह जानकारी सही है?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("नाम: $vName • गांव: $vVillage", fontSize = 13.sp, color = TextPrimary)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        if (vName.isNotBlank()) profileName = vName
                                                        if (vVillage.isNotBlank()) profileVillage = vVillage
                                                        voiceConfirmationData = null
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen, contentColor = Color.White)
                                                ) {
                                                    Text("हाँ, यही है (Confirm)", fontSize = 12.sp)
                                                }
                                                OutlinedButton(onClick = { voiceConfirmationData = null }) {
                                                    Text("बदलें (Edit)", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Avatar photo upload
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(CardWhite)
                                            .border(BorderStroke(1.5.dp, BorderNeutral), CircleShape)
                                            .clickable { photoPickerLauncher.launch("image/*") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    OutlinedButton(
                                        onClick = { photoPickerLauncher.launch("image/*") },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("फोटो जोड़ें (Optional)", fontSize = 12.sp, color = DeepForestGreen)
                                    }
                                }
                            }

                            // Name field
                            item {
                                OutlinedTextField(
                                    value = profileName,
                                    onValueChange = {
                                        profileName = it
                                        if (it.length >= 2) nameError = null
                                    },
                                    label = { Text("किसान / मालिक का नाम * (Required)") },
                                    isError = nameError != null,
                                    supportingText = nameError?.let { { Text(it, color = RedDue) } },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = CardWhite,
                                        unfocusedContainerColor = CardWhite
                                    )
                                )
                            }

                            // Village field
                            item {
                                OutlinedTextField(
                                    value = profileVillage,
                                    onValueChange = { profileVillage = it },
                                    label = { Text("गांव / शहर (Village / Town)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = CardWhite,
                                        unfocusedContainerColor = CardWhite
                                    )
                                )
                            }

                            // DOB field (Validation >= 10 yrs old)
                            item {
                                OutlinedTextField(
                                    value = profileDob,
                                    onValueChange = {
                                        profileDob = it
                                        dobError = null
                                    },
                                    label = { Text("जन्म तिथि * YYYY-MM-DD (DOB)") },
                                    isError = dobError != null,
                                    supportingText = dobError?.let { { Text(it, color = RedDue) } },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = CardWhite,
                                        unfocusedContainerColor = CardWhite
                                    )
                                )
                            }
                        }

                        // Next Button
                        Button(
                            onClick = {
                                var valid = true
                                if (profileName.trim().length < 2) {
                                    nameError = "नाम कम से कम 2 अक्षरों का होना चाहिए"
                                    valid = false
                                }
                                // Age check >= 10 years old
                                val parts = profileDob.split("-")
                                if (parts.size == 3) {
                                    val year = parts[0].toIntOrNull() ?: 2000
                                    val curYear = Calendar.getInstance().get(Calendar.YEAR)
                                    if (curYear - year < 10) {
                                        dobError = "उम्र कम से कम 10 वर्ष होनी चाहिए"
                                        valid = false
                                    }
                                } else {
                                    dobError = "कृपया सही प्रारूप भरें (उदा: 1995-05-15)"
                                    valid = false
                                }

                                if (valid) {
                                    currentStep = 3
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("profile_next_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen, contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
                        ) {
                            Text("आगे बढ़ें (Next: Tractor)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ==========================================
                // STEP 3: CHOOSE TRACTOR
                // ==========================================
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("अपना ट्रेक्टर चुनें (My Tractors)", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DeepForestGreen)
                            Text("आप एक या अधिक ट्रेक्टर जोड़ सकते हैं (या बाद में जोड़ें)", fontSize = 12.sp, color = TextMuted)

                            Spacer(modifier = Modifier.height(16.dp))

                            // List of added tractors
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(chosenTractors) { t ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                                        border = BorderStroke(1.dp, BorderNeutral),
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
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(MintTint),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(imageVector = Icons.Default.Agriculture, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(20.dp))
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(text = t.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                    Text(text = "${t.hp.toInt()} HP • ${t.regNo.ifBlank { "नंबर नहीं" }}", fontSize = 12.sp, color = TextMuted)
                                                }
                                            }
                                            IconButton(onClick = { chosenTractors.remove(t) }) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = RedDue)
                                            }
                                        }
                                    }
                                }

                                item {
                                    OutlinedButton(
                                        onClick = { isTractorSheetOpen = true },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.5.dp, DeepForestGreen),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DeepForestGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("+ नया ट्रेक्टर जोड़ें (Add Tractor)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                                    }
                                }
                            }
                        }

                        // Bottom Actions: Skip or Next
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 4 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BorderNeutral)
                            ) {
                                Text("बाद में जोड़ें (Skip)", color = TextMuted, fontSize = 14.sp)
                            }

                            Button(
                                onClick = { currentStep = 4 },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen, contentColor = Color.White)
                            ) {
                                Text("आगे बढ़ें (Next: Implements)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ==========================================
                // STEP 4: CHOOSE IMPLEMENTS
                // ==========================================
                4 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("उपकरण / इंप्लीमेंट जोड़ें (Implements)", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DeepForestGreen)
                            Text("रोटावेटर, हल, ट्रॉली आदि जोड़कर अपनी रेट सेट करें", fontSize = 12.sp, color = TextMuted)

                            Spacer(modifier = Modifier.height(16.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(chosenImplements) { imp ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                                        border = BorderStroke(1.dp, BorderNeutral),
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
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(MintTint),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(20.dp))
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(text = imp.hindiName.ifBlank { imp.name }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                    Text(text = "₹${imp.defaultRate.toInt()} / ${imp.rateType.lowercase()} • ${imp.details}", fontSize = 12.sp, color = TextMuted)
                                                }
                                            }
                                            IconButton(onClick = { chosenImplements.remove(imp) }) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = RedDue)
                                            }
                                        }
                                    }
                                }

                                item {
                                    OutlinedButton(
                                        onClick = { isImplementSheetOpen = true },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.5.dp, DeepForestGreen),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DeepForestGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("+ नया इंप्लीमेंट जोड़ें (Add Implement)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                                    }
                                }
                            }
                        }

                        // Finish Onboarding Button
                        Button(
                            onClick = {
                                val user = UserEntity(
                                    uid = viewModel.authManager.getUserId(),
                                    name = profileName.ifBlank { "Kisan Owner" },
                                    village = profileVillage,
                                    dob = profileDob,
                                    phone = phoneNumber.ifBlank { "+91 9826012345" },
                                    photoUri = profilePhotoUri,
                                    termsAccepted = true,
                                    onboardingDone = true
                                )
                                viewModel.completeOnboarding(user, chosenTractors, chosenImplements)
                                onOnboardingFinished()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("finish_onboarding_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen, contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
                        ) {
                            Text("शुरू करें (Enter Kisan Saathi)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Tractor Picker Sheet
    if (isTractorSheetOpen) {
        TractorPickerSheet(
            onDismiss = { isTractorSheetOpen = false },
            onTractorSelected = { tractor ->
                chosenTractors.add(tractor)
                isTractorSheetOpen = false
            }
        )
    }

    // Implement Picker Sheet
    if (isImplementSheetOpen) {
        ImplementPickerSheet(
            onDismiss = { isImplementSheetOpen = false },
            onImplementSelected = { implement ->
                chosenImplements.add(implement)
                isImplementSheetOpen = false
            }
        )
    }

    // Mobile Phone OTP Bottom Sheet
    if (isPhoneOtpSheetOpen) {
        val phoneSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { isPhoneOtpSheetOpen = false },
            sheetState = phoneSheetState,
            containerColor = WarmCreamBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (!otpSent) "मोबाइल नंबर दर्ज करें" else "OTP दर्ज करें",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepForestGreen
                )
                Text(
                    text = if (!otpSent) "+91 के साथ 10 अंकों का मोबाइल नंबर भरें" else "+91 $phoneNumber पर 6-अंकों का OTP भेजा गया है",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!otpSent) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { if (it.length <= 10) phoneNumber = it },
                        placeholder = { Text("98765 43210") },
                        prefix = { Text("+91  ", fontWeight = FontWeight.Bold, color = DeepForestGreen) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (phoneNumber.length == 10) {
                                otpSent = true
                                resendTimer = 30
                            }
                        },
                        enabled = phoneNumber.length == 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen, contentColor = Color.White)
                    ) {
                        Text("OTP भेजें (Send OTP)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // 6-digit OTP boxes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0..5) {
                            OutlinedTextField(
                                value = otpDigits[i],
                                onValueChange = { char ->
                                    if (char.length <= 1) {
                                        val newDigits = otpDigits.toMutableList()
                                        newDigits[i] = char
                                        otpDigits = newDigits
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(52.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CardWhite,
                                    unfocusedContainerColor = CardWhite
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (resendTimer > 0) "पुनः भेजें: ${resendTimer}s" else "OTP प्राप्त नहीं हुआ?",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        if (resendTimer == 0) {
                            Text(
                                text = "फिर से भेजें",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepForestGreen,
                                modifier = Modifier.clickable { resendTimer = 30 }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isPhoneOtpSheetOpen = false
                            currentStep = 2
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen, contentColor = Color.White)
                    ) {
                        Text("सत्यापित करें (Verify & Proceed)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
