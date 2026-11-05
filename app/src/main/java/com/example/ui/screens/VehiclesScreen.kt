package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    viewModel: KhetSaathiViewModel,
    onNavigateBack: () -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val vehicleMetrics by viewModel.vehicleMetrics.collectAsStateWithLifecycle()

    var showAddVehicleDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(Strings.get("my_vehicles", lang), fontWeight = FontWeight.Bold, color = ForestGreen)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ForestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddVehicleDialog = true },
                containerColor = ForestGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .testTag("add_vehicle_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Vehicle", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "अपने सभी ट्रैक्टरों का हिसाब और डीजल खपत देखें",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(vehicles, key = { it.id }) { vehicle ->
                val metric = vehicleMetrics.find { it.vehicle.id == vehicle.id }
                KhetCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (vehicle.isDefault) ForestGreen else CreamBackground,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Agriculture,
                                            contentDescription = null,
                                            tint = if (vehicle.isDefault) AmberGold else ForestGreen
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(vehicle.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                        if (vehicle.isDefault) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = AmberLight,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "★ मुख्य (Default)",
                                                    color = Color(0xFF8A5A00),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text("${vehicle.type} • ${vehicle.regNo}", fontSize = 13.sp, color = TextMuted)
                                }
                            }

                            if (!vehicle.isDefault) {
                                TextButton(onClick = { viewModel.setDefaultVehicle(vehicle.id) }) {
                                    Text(Strings.get("set_default", lang), color = ForestGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ForestGreenLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("कुल काम", fontSize = 11.sp, color = TextSecondary)
                                    Text("${metric?.jobsCount ?: 0} जॉब्स", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ForestGreenDark)
                                }
                                Column {
                                    Text("कुल कमाई", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${(metric?.totalEarnings ?: 0.0).toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ForestGreen)
                                }
                                Column {
                                    Text("डीजल खपत", fontSize = 11.sp, color = TextSecondary)
                                    Text("${(metric?.fuelLitres ?: 0.0).toInt()} L", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberGold)
                                }
                                Column {
                                    Text("औसत (L/h)", fontSize = 11.sp, color = TextSecondary)
                                    Text("${"%.1f".format(metric?.dieselAvgLitresPerHour ?: 0.0)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ForestGreenDark)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Modal Add Tractor Sheet
    if (showAddVehicleDialog) {
        com.example.ui.components.TractorPickerSheet(
            onDismiss = { showAddVehicleDialog = false },
            onTractorSelected = { tractor ->
                viewModel.addVehicleWithSpecs(
                    brand = tractor.brand,
                    model = tractor.model,
                    hp = tractor.hp,
                    nickname = tractor.name,
                    regNo = tractor.regNo,
                    year = tractor.year,
                    photoUri = tractor.photoUri,
                    isDefault = tractor.isDefault || vehicles.isEmpty()
                )
                showAddVehicleDialog = false
            }
        )
    }
}
