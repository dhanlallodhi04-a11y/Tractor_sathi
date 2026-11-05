package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.ImplementEntity
import com.example.ui.KhetSaathiViewModel
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
import com.example.util.Strings

@Composable
fun ImplementDetailScreen(
    implementId: String,
    viewModel: KhetSaathiViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val implements by viewModel.implements.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()

    val implement = implements.find { it.id == implementId }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (implement == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("इम्प्लीमेंट नहीं मिला (Implement not found)", color = TextMuted)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("वापस जाएं", color = Color.White)
                }
            }
        }
        return
    }

    val stats = remember(implementId) {
        viewModel.getImplementStats(implementId)
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val photoHeight = (screenHeight * 0.40f).coerceIn(240.dp, 360.dp)

    // Format Rate string with Unit
    val unitLabel = when (implement.rateType) {
        "HOUR" -> Strings.get("per_hour", lang)
        "ACRE" -> Strings.get("per_acre", lang)
        "BIGHA" -> Strings.get("per_bigha", lang)
        "HECTARE" -> Strings.get("per_hectare", lang)
        "GUNTHA" -> Strings.get("per_guntha", lang)
        "KANAL" -> Strings.get("per_kanal", lang)
        "MARLA" -> Strings.get("per_marla", lang)
        "CENT" -> Strings.get("per_cent", lang)
        "CHAKKAR" -> Strings.get("per_chakkar", lang)
        "DAY" -> Strings.get("per_day", lang)
        "FIXED" -> Strings.get("fixed", lang)
        "CUSTOM" -> implement.customUnitName?.ifEmpty { "यूनिट" } ?: "यूनिट"
        else -> implement.rateType
    }

    // Linked vehicle string
    val linkedVehiclesText = remember(implement.vehicleIds, vehicles) {
        if (implement.vehicleIds == "ALL" || implement.vehicleIds.isBlank()) {
            Strings.get("all_vehicles", lang)
        } else {
            val vNames = vehicles.filter { implement.vehicleIds.contains(it.id) }.map { it.name }
            if (vNames.isNotEmpty()) vNames.joinToString(", ") else Strings.get("all_vehicles", lang)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // At the very bottom: two buttons only, side by side: "Edit" (outlined green) and "Delete" (red)
            Surface(
                color = CardWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Edit Button (outlined green)
                    OutlinedButton(
                        onClick = { onNavigateToEdit(implement.id) },
                        border = BorderStroke(1.5.dp, ForestGreen),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("edit_implement_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Strings.get("edit", lang),
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Delete Button (red)
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DueRed),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("delete_implement_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Strings.get("delete", lang),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. The LARGEST element: full-width photo at the top (~40% of screen height, rounded bottom corners)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(photoHeight)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(ForestGreenLight, Color(0xFFD6E4DB))
                        )
                    )
            ) {
                if (!implement.photo.isNullOrBlank()) {
                    AsyncImage(
                        model = implement.photo,
                        contentDescription = implement.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Large placeholder illustration with the implement's icon on pastel background
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = CardWhite,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(110.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Agriculture,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = implement.hindiName.ifEmpty { implement.name },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenDark
                            )
                        }
                    }
                }

                // Back arrow overlay at top-left
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 12.dp, top = 8.dp)
                        .size(44.dp)
                        .background(CardWhite.copy(alpha = 0.9f), CircleShape)
                        .testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = ForestGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Below photo: implement name (large, bold), rate chip, details, linked vehicles & active status, stats
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Implement Name (large, bold)
                Text(
                    text = implement.hindiName.ifEmpty { implement.name },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                if (implement.name != implement.hindiName && implement.hindiName.isNotEmpty()) {
                    Text(
                        text = implement.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rate Chip: "₹800 / hour"
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ForestGreenLight,
                    border = BorderStroke(1.dp, ForestGreen)
                ) {
                    Text(
                        text = "₹${implement.defaultRate.toInt()} / $unitLabel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ForestGreen,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Details text in full
                if (implement.details.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = Strings.get("details", lang),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = implement.details,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Linked vehicles and "Active" status
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Strings.get("linked_vehicles", lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted
                            )
                            Text(
                                text = linkedVehiclesText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "स्थिति (Status):",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (implement.active) PaidGreenLight else DueRedLight
                            ) {
                                Text(
                                    text = if (implement.active) Strings.get("active_status", lang) else Strings.get("inactive_status", lang),
                                    color = if (implement.active) PaidGreen else DueRed,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Small stats (times used, total hours/area worked, total earned) if data exists
                if (stats.timesUsed > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "उपयोग आंकड़े (Usage Stats)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(Strings.get("times_used", lang), fontSize = 12.sp, color = TextMuted)
                                    Text("${stats.timesUsed} बार", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                }
                                if (stats.totalWorkedStr.isNotEmpty()) {
                                    Column {
                                        Text(Strings.get("total_work_done", lang), fontSize = 12.sp, color = TextMuted)
                                        Text(stats.totalWorkedStr, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ForestGreen)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(Strings.get("total_earned", lang), fontSize = 12.sp, color = TextMuted)
                                    Text("₹${stats.totalEarned.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PaidGreen)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(Strings.get("delete", lang), fontWeight = FontWeight.Bold, color = DueRed)
            },
            text = {
                Text(
                    text = Strings.get("delete_confirm_msg", lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteImplementWithUndo(implement)
                        showDeleteConfirmDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(Strings.get("delete", lang), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(Strings.get("cancel", lang), color = TextPrimary)
                }
            }
        )
    }
}
