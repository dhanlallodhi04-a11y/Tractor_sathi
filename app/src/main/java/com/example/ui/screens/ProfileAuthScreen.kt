package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.KhetSaathiViewModel
import com.example.ui.components.KhetCard
import com.example.ui.components.KhetInputField
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberLight
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.DueRed
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Strings
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAuthScreen(
    viewModel: KhetSaathiViewModel,
    onNavigateBack: () -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var nameInput by remember { mutableStateOf(user?.name ?: "रामसिंह लोधी") }
    var dobInput by remember { mutableStateOf(user?.dob ?: "1990-05-15") }
    var phoneInput by remember { mutableStateOf(user?.phone ?: "+91 9826012345") }
    var termsAccepted by remember { mutableStateOf(user?.termsAccepted ?: true) }
    var ageValidationError by remember { mutableStateOf<String?>(null) }

    fun validateAge(dob: String): Boolean {
        // dob format: YYYY-MM-DD
        try {
            val parts = dob.split("-")
            if (parts.size == 3) {
                val year = parts[0].toIntOrNull() ?: return true
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                if (currentYear - year < 10) {
                    ageValidationError = "न्यूनतम आयु 10 वर्ष होनी चाहिए (Must be at least 10 years old)"
                    return false
                }
            }
        } catch (_: Exception) {}
        ageValidationError = null
        return true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(Strings.get("profile", lang), fontWeight = FontWeight.Bold, color = ForestGreen)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ForestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Profile Avatar Banner
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AmberLight,
                        border = androidx.compose.foundation.BorderStroke(2.dp, AmberGold),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = nameInput.take(1).ifEmpty { "R" },
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberGold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ट्रैक्टर मालिक प्रोफाइल",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Text(
                        text = "ऑफलाइन सुरक्षित • सारा डेटा आपके फोन में सुरक्षित",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            item {
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        KhetInputField(
                            label = "पूरा नाम (Full Name) *",
                            value = nameInput,
                            onValueChange = { nameInput = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        KhetInputField(
                            label = "जन्म तिथि (DOB) YYYY-MM-DD *",
                            value = dobInput,
                            onValueChange = {
                                dobInput = it
                                validateAge(it)
                            },
                            placeholder = "उदा: 1990-05-15"
                        )
                        if (ageValidationError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ageValidationError!!,
                                color = DueRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Strings.get("dob_hint", lang),
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        KhetInputField(
                            label = "मोबाइल नंबर (+91) *",
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = termsAccepted,
                                onCheckedChange = { termsAccepted = it },
                                colors = CheckboxDefaults.colors(checkedColor = ForestGreen)
                            )
                            Text(
                                text = "नियम एवं शर्तें स्वीकार हैं (Terms & Offline Ledger)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (validateAge(dobInput) && nameInput.isNotBlank()) {
                            viewModel.saveUser(
                                name = nameInput.trim(),
                                dob = dobInput.trim(),
                                phone = phoneInput.trim()
                            )
                            scope.launch {
                                snackbarHostState.showSnackbar("प्रोफाइल सफलतापूर्वक सुरक्षित हो गई!")
                            }
                        }
                    },
                    enabled = ageValidationError == null && nameInput.isNotBlank() && termsAccepted,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_profile_button")
                ) {
                    Text(
                        text = "प्रोफाइल सेव करें (Save)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
