package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberLight
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Strings
import com.example.util.VoiceParsedResult
import com.example.util.VoiceParser
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceInputDialog(
    onDismiss: () -> Unit,
    onResultConfirmed: (VoiceParsedResult) -> Unit,
    lang: String = "hi"
) {
    var transcript by remember { mutableStateOf("") }
    var parsedResult by remember { mutableStateOf<VoiceParsedResult?>(null) }
    var isListening by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                transcript = spokenText
                parsedResult = VoiceParser.parse(spokenText)
            }
        }
    }

    fun startListening() {
        try {
            isListening = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lang == "hi") "hi-IN" else "en-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, Strings.get("listening", lang))
            }
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            isListening = false
            // Speech recognizer not installed or unavailable in emulator container
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("voice_input_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.get("voice_assistant", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Microphone animated circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(if (isListening) pulseScale else 1f)
                        .background(if (isListening) AmberGold else ForestGreen, CircleShape)
                        .clickable { startListening() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isListening) Strings.get("listening", lang) else "माइक दबाएं और बोलें (Tap mic to speak)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isListening) AmberGold else TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Example presets if user wants to test by tapping
                Text(
                    text = "त्वरित उदाहरण (या सीधे बोलें):",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                val samplePhrases = listOf(
                    "रोटावेटर 2 घंटा 30 मिनट",
                    "कल्टीवेटर 1 घंटा",
                    "सुरेश ने 2000 दिए नकद",
                    "ट्राली 3 चक्कर",
                    "लेज़र लेवलर 4 घंटा"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    samplePhrases.forEach { phrase ->
                        Surface(
                            color = CreamBackground,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable {
                                transcript = phrase
                                parsedResult = VoiceParser.parse(phrase)
                            }
                        ) {
                            Text(
                                text = phrase,
                                style = MaterialTheme.typography.bodySmall,
                                color = ForestGreen,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detected result preview card
                if (transcript.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ForestGreenLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "🗣️ सुना गया: \"$transcript\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            when (val res = parsedResult) {
                                is VoiceParsedResult.JobDraft -> {
                                    Surface(
                                        color = PaidGreenLight,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "🚜 नया काम: ${res.implementName}",
                                                fontWeight = FontWeight.Bold,
                                                color = PaidGreen,
                                                fontSize = 14.sp
                                            )
                                            val timeStr = if (res.minutes > 0) "${res.hours} घंटा ${res.minutes} मिनट" else "${res.hours} घंटा"
                                            Text(
                                                text = "समय: $timeStr",
                                                color = TextPrimary,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                                is VoiceParsedResult.PaymentDraft -> {
                                    Surface(
                                        color = AmberLight,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "💰 भुगतान मिला: ₹${res.amount.toInt()} (${res.method})",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF8A5A00),
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "किसान: ${res.customerName}",
                                                color = TextPrimary,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "कृपया साफ बोलें, जैसे: 'रोटावेटर 2 घंटा'",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(text = Strings.get("cancel", lang), color = TextPrimary)
                    }
                    Button(
                        onClick = {
                            parsedResult?.let { res ->
                                onResultConfirmed(res)
                                onDismiss()
                            }
                        },
                        enabled = parsedResult != null && parsedResult !is VoiceParsedResult.Unrecognized,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Strings.get("confirm_apply", lang), color = Color.White)
                    }
                }
            }
        }
    }
}
