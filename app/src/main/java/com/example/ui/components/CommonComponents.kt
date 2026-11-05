package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BorderDarkEdge
import com.example.ui.theme.BorderNeutral
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamYellowEdge
import com.example.ui.theme.CreamYellowTint
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.InputBorder
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintEdge
import com.example.ui.theme.MintTint
import com.example.ui.theme.MoneyDueDarkEdge
import com.example.ui.theme.MoneyDueLightTint
import com.example.ui.theme.MoneyDueRed
import com.example.ui.theme.MoneyInDarkEdge
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.MoneyInLightTint
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSub

// ==========================================
// 1. 3D Raised Card (20px radius, 1.5px border, solid 4px bottom edge)
// ==========================================
@Composable
fun KhetCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = CardWhite,
    borderColor: Color = BorderNeutral,
    bottomEdgeColor: Color = BorderDarkEdge,
    cornerRadius: Dp = 20.dp,
    bottomEdgeHeight: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    testTag: String = "",
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(cornerRadius),
        color = bottomEdgeColor,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = bottomEdgeHeight)
                .background(backgroundColor, RoundedCornerShape(cornerRadius))
                .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(cornerRadius))
                .padding(16.dp)
        ) {
            content()
        }
    }
}

// ==========================================
// 2. 3D Raised Pill Chip (Unselected = white raised; Selected = Amber)
// ==========================================
@Composable
fun KhetPillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val bg = if (selected) AmberAccent else CardWhite
    val border = if (selected) AmberDark else BorderNeutral
    val edge = if (selected) AmberDark else BorderDarkEdge
    val textCol = if (selected) Color.White else TextMain

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = edge,
        shadowElevation = 1.dp,
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 3.dp)
                .background(bg, RoundedCornerShape(999.dp))
                .border(BorderStroke(1.5.dp, border), RoundedCornerShape(999.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = textCol,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// 3. Input Field (White, 1.5px border #D9D3C3, 52px height, 12px radius)
// ==========================================
@Composable
fun KhetInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    testTag: String = ""
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = TextMain,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(CardWhite, RoundedCornerShape(12.dp))
                .border(BorderStroke(1.5.dp, InputBorder), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp)
                .testTag(testTag),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = TextMuted
                            )
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = TextMain,
                            fontWeight = FontWeight.Medium
                        ),
                        singleLine = true,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        cursorBrush = SolidColor(DeepForestGreen),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        }
    }
}

// ==========================================
// 4. Status Badge (Paid = Green, Part = Amber, Due = Red)
// ==========================================
@Composable
fun StatusBadge(
    status: String, // "PAID", "PART", "DUE"
    total: Double,
    paid: Double,
    modifier: Modifier = Modifier
) {
    val due = (total - paid).coerceAtLeast(0.0)
    val (bgColor, textColor, label, edgeColor) = when {
        paid >= total && total > 0 -> Quadruple(
            MoneyInLightTint,
            MoneyInGreen,
            "चुकाया (Paid)",
            MoneyInDarkEdge
        )
        paid > 0 && due > 0 -> Quadruple(
            CreamYellowTint,
            AmberDark,
            "आंशिक (₹${paid.toInt()})",
            AmberDark
        )
        else -> Quadruple(
            MoneyDueLightTint,
            MoneyDueRed,
            "बाकी (₹${due.toInt()})",
            MoneyDueDarkEdge
        )
    }

    Surface(
        color = edgeColor,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 2.dp)
                .background(bgColor, RoundedCornerShape(10.dp))
                .border(BorderStroke(1.dp, textColor.copy(alpha = 0.4f)), RoundedCornerShape(10.dp))
                .padding(horizontal = 9.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// ==========================================
// 5. 3D Raised Button Helper
// ==========================================
@Composable
fun RaisedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = DeepForestGreen,
    bottomEdgeColor: Color = ForestGreenDark,
    borderColor: Color = bottomEdgeColor,
    contentColor: Color = Color.White,
    cornerRadius: Dp = 999.dp,
    bottomEdgeHeight: Dp = 4.dp,
    testTag: String = "",
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(cornerRadius),
        color = bottomEdgeColor,
        shadowElevation = 2.dp,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = bottomEdgeHeight)
                .background(backgroundColor, RoundedCornerShape(cornerRadius))
                .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(cornerRadius))
                .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

// ==========================================
// 6. Stepper Control (3D raised)
// ==========================================
@Composable
fun StepperControl(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Int = 0,
    maxValue: Int = 999,
    step: Int = 1
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BorderDarkEdge,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 3.dp)
                .background(CardWhite, RoundedCornerShape(12.dp))
                .border(BorderStroke(1.5.dp, BorderNeutral), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            IconButton(
                onClick = { if (value - step >= minValue) onValueChange(value - step) },
                modifier = Modifier.size(44.dp)
            ) {
                Text(text = "−", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
            }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            IconButton(
                onClick = { if (value + step <= maxValue) onValueChange(value + step) },
                modifier = Modifier.size(44.dp)
            ) {
                Text(text = "+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
            }
        }
    }
}

// ==========================================
// 7. Textures: Decorative Field Wavy Lines
// ==========================================
fun Modifier.wavyFieldTexture(lineColor: Color): Modifier = this.drawWithContent {
    drawContent()
    val yBase = size.height - 10.dp.toPx()
    val waveHeight = 14.dp.toPx()

    // Wavy line 1
    val path1 = Path().apply {
        moveTo(0f, yBase)
        cubicTo(
            size.width * 0.3f, yBase - waveHeight,
            size.width * 0.7f, yBase + waveHeight * 0.4f,
            size.width, yBase - waveHeight * 0.3f
        )
    }
    drawPath(path1, color = lineColor, style = Stroke(width = 1.5.dp.toPx()))

    // Wavy line 2
    val path2 = Path().apply {
        moveTo(0f, yBase + 4.dp.toPx())
        cubicTo(
            size.width * 0.25f, yBase + 2.dp.toPx(),
            size.width * 0.65f, yBase - waveHeight * 0.4f,
            size.width, yBase + 1.dp.toPx()
        )
    }
    drawPath(path2, color = lineColor.copy(alpha = 0.5f), style = Stroke(width = 1.dp.toPx()))

    // Tiny decorative dots
    drawCircle(
        color = lineColor,
        radius = 2.dp.toPx(),
        center = Offset(size.width * 0.88f, yBase - 8.dp.toPx())
    )
    drawCircle(
        color = lineColor.copy(alpha = 0.7f),
        radius = 1.5.dp.toPx(),
        center = Offset(size.width * 0.94f, yBase - 4.dp.toPx())
    )
}
