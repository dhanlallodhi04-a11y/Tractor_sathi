package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.KhetSaathiViewModel
import com.example.ui.ReportPeriod
import com.example.ui.components.KhetCard
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
import com.example.ui.theme.PaidGreenLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShareHelper
import com.example.util.Strings

@Composable
fun ReportsScreen(
    viewModel: KhetSaathiViewModel
) {
    val context = LocalContext.current
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val analytics by viewModel.analytics.collectAsStateWithLifecycle()
    val compareWithPrevious by viewModel.compareWithPrevious.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()

    var showPdfPreviewDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 16.dp)
            .testTag("reports_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Strings.get("reports_analytics", lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Text(
                        text = "विस्तृत आय, खर्च एवं उपकरण विश्लेषण",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                // Share Button
                IconButton(
                    onClick = {
                        val text = generateFullReportText(analytics, selectedPeriod, lang)
                        ShareHelper.shareWhatsApp(context, text)
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = ForestGreen)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Period Selection Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    ReportPeriod.TODAY to Strings.get("period_today", lang),
                    ReportPeriod.WEEK to Strings.get("period_week", lang),
                    ReportPeriod.MONTH to Strings.get("period_month", lang),
                    ReportPeriod.KHARIF to Strings.get("period_season_kharif", lang),
                    ReportPeriod.RABI to Strings.get("period_season_rabi", lang),
                    ReportPeriod.FINANCIAL_YEAR to Strings.get("period_financial_year", lang)
                ).forEach { (p, label) ->
                    KhetPillChip(
                        text = label,
                        selected = selectedPeriod == p,
                        onClick = { viewModel.setPeriod(p) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Period Comparison Toggle (e.g. This Period vs Previous Period)
        item {
            KhetCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = AmberGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = Strings.get("period_comparison", lang),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "पिछली अवधि (Previous Period) से तुलना",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                    Switch(
                        checked = compareWithPrevious,
                        onCheckedChange = { viewModel.setCompareWithPrevious(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = ForestGreenLight)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Key Financial Overview Cards
        item {
            KhetCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "वित्तीय सारांश (Financial Summary)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = Strings.get("total_billed", lang),
                            amount = analytics.totalBilled,
                            prevAmount = if (compareWithPrevious) analytics.prevBilled else null,
                            color = ForestGreenDark
                        )
                        MetricItem(
                            label = Strings.get("total_paid", lang),
                            amount = analytics.totalCollected,
                            prevAmount = if (compareWithPrevious) analytics.prevCollected else null,
                            color = PaidGreen
                        )
                        MetricItem(
                            label = Strings.get("total_due", lang),
                            amount = analytics.totalDue,
                            prevAmount = null,
                            color = DueRed
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Net Profit Row
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ForestGreenLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "🏆 शुद्ध बचत (Net Profit)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenDark
                                )
                                Text(
                                    text = "खर्च घटाने के बाद (Spent: ₹${analytics.totalSpent.toInt()})",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${analytics.netProfit.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (analytics.netProfit >= 0) ForestGreen else DueRed
                                )
                                if (compareWithPrevious && analytics.prevNetProfit > 0) {
                                    val deltaPercent = ((analytics.netProfit - analytics.prevNetProfit) / analytics.prevNetProfit * 100).toInt()
                                    val isUp = deltaPercent >= 0
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = if (isUp) PaidGreen else DueRed,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "${if (isUp) "+" else ""}$deltaPercent% vs पिछला",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUp) PaidGreen else DueRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Chart 1: Profitability Trend Chart
        item {
            KhetCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.get("profitability_trend", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = AmberGold)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    ProfitabilityTrendCanvas(points = analytics.trendPoints)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Chart 2: Earnings per Implement
        item {
            KhetCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = Strings.get("earnings_by_implement", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (analytics.implementEarnings.isEmpty()) {
                        Text("इस अवधि में कोई यंत्र कार्य नहीं", color = TextMuted, fontSize = 13.sp)
                    } else {
                        val maxEarning = analytics.implementEarnings.maxOfOrNull { it.second } ?: 1.0
                        analytics.implementEarnings.forEach { (impName, amount) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(impName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text("₹${amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ForestGreen)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // Progress bar
                                val fraction = (amount / maxEarning).toFloat().coerceIn(0.05f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .background(CreamBackground, RoundedCornerShape(5.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction)
                                            .height(10.dp)
                                            .background(AmberGold, RoundedCornerShape(5.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Chart 3: Top Customers
        item {
            KhetCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = Strings.get("top_customers", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (analytics.topCustomers.isEmpty()) {
                        Text("कोई ग्राहक डेटा नहीं", color = TextMuted, fontSize = 13.sp)
                    } else {
                        val maxCust = analytics.topCustomers.maxOfOrNull { it.second } ?: 1.0
                        analytics.topCustomers.forEachIndexed { idx, (custName, amount) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${idx + 1}. $custName",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text("₹${amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ForestGreen)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val fraction = (amount / maxCust).toFloat().coerceIn(0.05f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .background(CreamBackground, RoundedCornerShape(5.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction)
                                            .height(10.dp)
                                            .background(ForestGreen, RoundedCornerShape(5.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Chart 4: Expense Breakdown
        item {
            KhetCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = Strings.get("expense_breakdown", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (analytics.expenseBreakdown.isEmpty()) {
                        Text("इस अवधि में कोई खर्च दर्ज नहीं", color = TextMuted, fontSize = 13.sp)
                    } else {
                        analytics.expenseBreakdown.forEach { (catName, amt) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(if (catName.contains("डीजल")) AmberGold else DueRed, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(catName, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                }
                                Text("₹${amt.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DueRed)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Export Actions: PDF Statement & WhatsApp Report
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val report = generateFullReportText(analytics, selectedPeriod, lang)
                        ShareHelper.shareText(context, report)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Strings.get("export_pdf", lang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        val report = generateFullReportText(analytics, selectedPeriod, lang)
                        ShareHelper.shareWhatsApp(context, report)
                    },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = ForestGreen)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Strings.get("share_summary", lang), color = ForestGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    amount: Double,
    prevAmount: Double?,
    color: Color
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "₹${amount.toInt()}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        if (prevAmount != null) {
            val delta = (amount - prevAmount).toInt()
            val sign = if (delta >= 0) "+" else ""
            Text(
                text = "$sign$delta vs पिछला",
                fontSize = 11.sp,
                color = if (delta >= 0) PaidGreen else DueRed,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ProfitabilityTrendCanvas(
    points: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val maxVal = points.maxOfOrNull { it.second }?.takeIf { it > 0.0 } ?: 1000.0

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height - 25.dp.toPx()
            val barWidth = 28.dp.toPx()
            val spacing = (canvasWidth - (points.size * barWidth)) / (points.size + 1)

            // Horizontal guideline
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(0f, canvasHeight),
                end = Offset(canvasWidth, canvasHeight),
                strokeWidth = 2f
            )

            points.forEachIndexed { idx, (_, value) ->
                val barHeight = ((value / maxVal) * (canvasHeight - 15.dp.toPx())).toFloat().coerceAtLeast(10f)
                val left = spacing + idx * (barWidth + spacing)
                val top = canvasHeight - barHeight

                // Draw Bar
                drawRoundRect(
                    color = if (idx == points.lastIndex) ForestGreen else AmberGold,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
        }

        // Labels under bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            points.forEach { (lbl, valAmount) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(lbl, fontSize = 11.sp, color = TextMuted)
                    Text("₹${valAmount.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                }
            }
        }
    }
}

fun generateFullReportText(
    analytics: com.example.ui.AnalyticsState,
    period: ReportPeriod,
    lang: String
): String {
    val periodName = when (period) {
        ReportPeriod.TODAY -> "आज (Today)"
        ReportPeriod.WEEK -> "इस हफ्ते (This Week)"
        ReportPeriod.MONTH -> "इस महीने (This Month)"
        ReportPeriod.KHARIF -> "खरीफ सीजन (Kharif)"
        ReportPeriod.RABI -> "रबी सीजन (Rabi)"
        ReportPeriod.FINANCIAL_YEAR -> "वित्तीय वर्ष (FY)"
        ReportPeriod.ALL_TIME -> "संपूर्ण (All Time)"
    }

    val impList = analytics.implementEarnings.joinToString("\n") { "• ${it.first}: ₹${it.second.toInt()}" }
    val custList = analytics.topCustomers.joinToString("\n") { "• ${it.first}: ₹${it.second.toInt()}" }

    return """
    📊 *खेत साथी - विस्तृत बिजनेस व एनालिटिक्स रिपोर्ट*
    ━━━━━━━━━━━━━━━━━━━━━━━━━━
    📅 अवधि: $periodName
    
    💰 *वित्तीय स्थिति:*
    • कुल बिल (Billed): ₹${analytics.totalBilled.toInt()}
    • जमा राशि (Collected): ₹${analytics.totalCollected.toInt()}
    • बाकी उधारी (Due): ₹${analytics.totalDue.toInt()}
    • कुल खर्चा (Diesel/Maint): ₹${analytics.totalSpent.toInt()}
    ---------------------------------
    🏆 *शुद्ध मुनाफा (Net Profit): ₹${analytics.netProfit.toInt()}*
    ━━━━━━━━━━━━━━━━━━━━━━━━━━
    🌾 *यंत्र अनुसार कमाई (Earnings by Implement):*
    $impList
    ━━━━━━━━━━━━━━━━━━━━━━━━━━
    👥 *शीर्ष किसान (Top Customers):*
    $custList
    ━━━━━━━━━━━━━━━━━━━━━━━━━━
    _खेत साथी ऐप - ट्रैक्टर मालिक का अपना डिजिटल साथी_
    """.trimIndent()
}
