package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.KhetSaathiViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BorderNeutral
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.MintTint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import org.json.JSONArray

data class TourStepData(
    val id: String,
    val target: String,
    val title: String,
    val description: String,
    val speechText: String
)

fun loadTourSteps(context: Context, lang: String): List<TourStepData> {
    val file = if (lang == "hi") "tour_hi.json" else "tour_en.json"
    return try {
        val stream = context.assets.open(file)
        val jsonStr = stream.bufferedReader().use { it.readText() }
        val arr = JSONArray(jsonStr)
        val list = mutableListOf<TourStepData>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                TourStepData(
                    id = obj.getString("id"),
                    target = obj.getString("target"),
                    title = obj.getString("title"),
                    description = obj.getString("description"),
                    speechText = obj.getString("speechText")
                )
            )
        }
        list
    } catch (_: Exception) {
        listOf(
            TourStepData(
                id = "summary_card",
                target = "summary_card",
                title = "हिसाब कार्ड (Summary)",
                description = "यहाँ आपके कुल काम, मिला हुआ पैसा और मुनाफे का पूरा हिसाब दिखता है।",
                speechText = "यहाँ आपके कुल काम और मुनाफे का हिसाब दिखता है।"
            )
        )
    }
}

@Composable
fun GuidedTourOverlay(
    viewModel: KhetSaathiViewModel,
    stepIndex: Int,
    isMuted: Boolean,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onDismiss: () -> Unit,
    onToggleMute: () -> Unit
) {
    val context = LocalContext.current
    val currentLang = viewModel.currentLanguage.value
    val steps = remember(currentLang) { loadTourSteps(context, currentLang) }

    val safeIndex = stepIndex.coerceIn(0, (steps.size - 1).coerceAtLeast(0))
    val currentStep = steps.getOrNull(safeIndex) ?: return

    // Speak narration if not muted
    LaunchedEffect(safeIndex, isMuted) {
        if (!isMuted) {
            viewModel.aiAssistantManager.speak(currentStep.speechText)
        } else {
            viewModel.aiAssistantManager.stopSpeaking()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* block background clicks */ }
            .testTag("guided_tour_overlay")
    ) {
        // Top Banner with Dismiss & Mute
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ऐप मार्गदर्शन • Step ${safeIndex + 1} of ${steps.size}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Mute Voice",
                        tint = if (isMuted) Color.White.copy(alpha = 0.6f) else AmberAccent
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Skip Tour",
                        tint = Color.White
                    )
                }
            }
        }

        // Spotlight highlight placeholder banner
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AmberAccent.copy(alpha = 0.25f),
                border = BorderStroke(2.dp, AmberAccent),
                modifier = Modifier.fillMaxWidth().height(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "★ फोकस: ${currentStep.title} ★",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Bottom Raised 3D Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.5.dp, BorderNeutral)
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
                    Text(
                        text = currentStep.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepForestGreen
                    )
                    Surface(
                        color = MintTint,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${safeIndex + 1} / ${steps.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepForestGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentStep.description,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Actions: Back, Skip, Next
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (safeIndex > 0) {
                        OutlinedButton(
                            onClick = onPrev,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderNeutral)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextMuted)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("पीछे", color = TextMuted, fontSize = 13.sp)
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextMuted)
                        ) {
                            Text("छोड़ें (Skip)", fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = onNext,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepForestGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
                    ) {
                        Text(
                            text = if (safeIndex == steps.size - 1) "शुरू करें (Done)" else "आगे (Next)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
