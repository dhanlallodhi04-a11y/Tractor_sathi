package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BorderDarkEdge
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamYellowTint
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.MintTint
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub
import com.example.ui.theme.WarmCreamBg
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class CelebrationData(
    val title: String,
    val message: String,
    val buttonText: String = "आगे बढ़ें",
    val shareText: String? = null,
    val iconEmoji: String = "🏆"
)

private data class ConfettiParticle(
    val color: Color,
    val angle: Double,
    val speed: Float,
    val size: Float,
    val radius: Float
)

@Composable
fun CelebrationDialog(
    data: CelebrationData,
    onDismiss: () -> Unit,
    onShare: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val progress = remember { Animatable(0f) }

    // Generate max 50 lightweight particles
    val particles = remember {
        val colors = listOf(
            DeepForestGreen,
            AmberAccent,
            MoneyInGreen,
            Color(0xFFD99A1F),
            Color(0xFF2E8B57),
            Color(0xFFDCEBF7),
            Color(0xFFFADDE4),
            Color(0xFFFBEFCB)
        )
        List(50) {
            ConfettiParticle(
                color = colors[it % colors.size],
                angle = Random.nextDouble(0.0, Math.PI * 2),
                speed = Random.nextFloat() * 260f + 140f,
                size = Random.nextFloat() * 6f + 4f,
                radius = Random.nextFloat() * 180f + 60f
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            // Confetti canvas behind the card
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f - 60f
                val t = progress.value
                val alpha = (1f - t * 0.7f).coerceIn(0f, 1f)

                particles.forEach { p ->
                    val dist = p.speed * t
                    val gravity = 180f * (t * t)
                    val x = cx + (cos(p.angle) * dist).toFloat()
                    val y = cy + (sin(p.angle) * dist).toFloat() + gravity

                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size * (1f - t * 0.3f),
                        center = Offset(x, y)
                    )
                }
            }

            // Celebration Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BorderDarkEdge,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .clickable(enabled = false) {}
                    .testTag("celebration_dialog_card")
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .background(CardWhite, RoundedCornerShape(24.dp))
                        .border(BorderStroke(1.5.dp, AmberAccent), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Badge / Trophy Icon in amber circle
                        Surface(
                            shape = CircleShape,
                            color = CreamYellowTint,
                            border = BorderStroke(2.dp, AmberAccent),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Achievement Trophy",
                                    tint = AmberDark,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title
                        Text(
                            text = data.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepForestGreen,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Message
                        Text(
                            text = data.message,
                            fontSize = 15.sp,
                            color = TextMain,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Primary Action Button (Flat pill deep forest green)
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("celebration_proceed_button")
                        ) {
                            Text(
                                text = data.buttonText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CardWhite
                            )
                        }

                        // Optional WhatsApp share button
                        if (!data.shareText.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.5.dp, MoneyInGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, data.shareText)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Achievement"))
                                        onShare?.invoke()
                                    }
                                    .testTag("celebration_share_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = MoneyInGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WhatsApp पर शेयर करें",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MoneyInGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
