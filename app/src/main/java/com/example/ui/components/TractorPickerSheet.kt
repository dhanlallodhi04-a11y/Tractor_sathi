package com.example.ui.components

import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleEntity
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BorderNeutral
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.MintTint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarmCreamBg
import org.json.JSONArray
import kotlin.math.roundToInt

data class TractorModelItem(val name: String, val hp: Double, val series: String = "")
data class TractorBrandItem(val brand: String, val hindiBrand: String, val models: List<TractorModelItem>)

fun loadTractorsFromAssets(context: Context): List<TractorBrandItem> {
    return try {
        val stream = context.assets.open("tractors.json")
        val jsonStr = stream.bufferedReader().use { it.readText() }
        val arr = JSONArray(jsonStr)
        val list = mutableListOf<TractorBrandItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val brand = obj.getString("brand")
            val hindiBrand = obj.optString("hindiBrand", brand)
            val modelsArr = obj.getJSONArray("models")
            val models = mutableListOf<TractorModelItem>()
            for (j in 0 until modelsArr.length()) {
                val mObj = modelsArr.getJSONObject(j)
                models.add(
                    TractorModelItem(
                        name = mObj.getString("name"),
                        hp = mObj.optDouble("hp", 45.0),
                        series = mObj.optString("series", "")
                    )
                )
            }
            list.add(TractorBrandItem(brand, hindiBrand, models))
        }
        list
    } catch (_: Exception) {
        emptyList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TractorPickerSheet(
    onDismiss: () -> Unit,
    onTractorSelected: (VehicleEntity) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val brands = remember { loadTractorsFromAssets(context) }
    var selectedBrand by remember { mutableStateOf(brands.firstOrNull()?.brand ?: "Mahindra") }
    var searchQuery by remember { mutableStateOf("") }
    var isCustomMode by remember { mutableStateOf(false) }

    // Selected vehicle fields
    var chosenModelName by remember { mutableStateOf("") }
    var chosenHp by remember { mutableDoubleStateOf(45.0) }
    var nickname by remember { mutableStateOf("") }
    var regNo by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2022") }
    var isDefault by remember { mutableStateOf(false) }

    // Custom flow state
    var customBrand by remember { mutableStateOf("") }
    var customModel by remember { mutableStateOf("") }

    val currentBrandObj = brands.find { it.brand.equals(selectedBrand, ignoreCase = true) }
    val modelsForBrand = currentBrandObj?.models ?: emptyList()

    val filteredModels = remember(modelsForBrand, searchQuery) {
        if (searchQuery.isBlank()) modelsForBrand
        else {
            val q = searchQuery.lowercase().trim()
            modelsForBrand.filter {
                it.name.lowercase().contains(q) || it.series.lowercase().contains(q) || it.hp.toInt().toString().contains(q)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmCreamBg,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("tractor_picker_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = DeepForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isCustomMode) "अपना ट्रेक्टर जोड़ें" else "ट्रेक्टर चुनें (Select Tractor)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepForestGreen
                        )
                        Text(
                            text = "ब्रांड और मॉडल चुनकर विवरण भरें",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isCustomMode) {
                // Brand Horizontal Scroll Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(brands) { b ->
                        val isSel = b.brand.equals(selectedBrand, ignoreCase = true)
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedBrand = b.brand
                                searchQuery = ""
                            },
                            label = {
                                Text(
                                    text = "${b.brand} (${b.hindiBrand})",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberAccent,
                                selectedLabelColor = DeepForestGreen,
                                containerColor = CardWhite,
                                labelColor = TextPrimary
                            ),
                            border = BorderStroke(1.dp, if (isSel) AmberAccent else BorderNeutral)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("मॉडल या HP खोजें...", fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = DeepForestGreen,
                        unfocusedBorderColor = BorderNeutral
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Model Selection List (Height constrained)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredModels) { m ->
                        val isChosen = chosenModelName == m.name
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    chosenModelName = m.name
                                    chosenHp = m.hp
                                    if (nickname.isBlank()) {
                                        nickname = "$selectedBrand ${m.name}"
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChosen) MintTint else CardWhite
                            ),
                            border = BorderStroke(1.dp, if (isChosen) DeepForestGreen else BorderNeutral)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "$selectedBrand ${m.name}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (m.series.isNotBlank()) {
                                        Text(text = m.series, fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AmberAccent.copy(alpha = 0.2f),
                                        modifier = Modifier.padding(end = 6.dp)
                                    ) {
                                        Text(
                                            text = "${m.hp.toInt()} HP",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepForestGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (isChosen) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = DeepForestGreen)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Switch to custom model button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { isCustomMode = true },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "मेरा मॉडल नहीं मिला? यहाँ क्लिक करें (+ Custom)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepForestGreen
                            )
                        }
                    }
                }
            } else {
                // Custom model inputs
                OutlinedTextField(
                    value = customBrand,
                    onValueChange = { customBrand = it },
                    label = { Text("कंपनी / ब्रांड (e.g. Swaraj, Sonalika)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customModel,
                    onValueChange = { customModel = it },
                    label = { Text("मॉडल नाम (e.g. 744 FE, 575 DI)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.clickable { isCustomMode = false },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ब्रांड सूची पर वापस जाएं", fontSize = 12.sp, color = DeepForestGreen, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // HP Slider Picker (10 to 120 HP)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardWhite, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ट्रेक्टर की पावर (HP)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Text("${chosenHp.roundToInt()} HP", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                }
                Slider(
                    value = chosenHp.toFloat(),
                    onValueChange = { chosenHp = it.toDouble() },
                    valueRange = 15f..120f,
                    steps = 20,
                    colors = SliderDefaults.colors(
                        thumbColor = DeepForestGreen,
                        activeTrackColor = DeepForestGreen,
                        inactiveTrackColor = BorderNeutral
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Extra Fields: Nickname & Registration
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("उपनाम (Nickname)", fontSize = 12.sp) },
                    placeholder = { Text("बड़ा ट्रेक्टर", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite
                    )
                )

                OutlinedTextField(
                    value = regNo,
                    onValueChange = { regNo = it.uppercase() },
                    label = { Text("नंबर (Reg No.)", fontSize = 12.sp) },
                    placeholder = { Text("MP 04 AB 1234", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Year and Default checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("मॉडल वर्ष (Year)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(130.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite
                    )
                )

                Row(
                    modifier = Modifier
                        .clickable { isDefault = !isDefault }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDefault) DeepForestGreen else CardWhite)
                            .border(BorderStroke(1.5.dp, DeepForestGreen)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDefault) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("मुख्य ट्रेक्टर (Default)", fontSize = 13.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Save Button
            val canSave = if (isCustomMode) customModel.isNotBlank() else chosenModelName.isNotBlank()
            Button(
                onClick = {
                    val finalBrand = if (isCustomMode) customBrand.ifBlank { "Custom" } else selectedBrand
                    val finalModel = if (isCustomMode) customModel else chosenModelName
                    val finalName = nickname.ifBlank { "$finalBrand $finalModel".trim() }
                    val vehicle = VehicleEntity(
                        name = finalName,
                        brand = finalBrand,
                        model = finalModel,
                        hp = chosenHp,
                        regNo = regNo,
                        type = "${chosenHp.roundToInt()} HP Tractor",
                        year = year,
                        isDefault = isDefault
                    )
                    onTractorSelected(vehicle)
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_tractor_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepForestGreen,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
            ) {
                Text("ट्रेक्टर सेव करें (Save Tractor)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
