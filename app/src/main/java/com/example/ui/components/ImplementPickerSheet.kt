package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ImplementEntity
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BorderNeutral
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.MintTint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarmCreamBg
import org.json.JSONArray

data class ImplementSpecField(
    val key: String,
    val label: String,
    val options: List<String>,
    val default: String
)

data class ImplementTemplateItem(
    val id: String,
    val name: String,
    val hindiName: String,
    val icon: String,
    val defaultRate: Double,
    val defaultRateType: String,
    val customUnitName: String? = null,
    val specFields: List<ImplementSpecField> = emptyList()
)

data class ImplementCategoryGroup(
    val category: String,
    val categoryName: String,
    val items: List<ImplementTemplateItem>
)

fun loadImplementsFromAssets(context: Context): List<ImplementCategoryGroup> {
    return try {
        val stream = context.assets.open("implements.json")
        val jsonStr = stream.bufferedReader().use { it.readText() }
        val arr = JSONArray(jsonStr)
        val list = mutableListOf<ImplementCategoryGroup>()
        for (i in 0 until arr.length()) {
            val catObj = arr.getJSONObject(i)
            val cat = catObj.getString("category")
            val catName = catObj.getString("categoryName")
            val itemsArr = catObj.getJSONArray("items")
            val items = mutableListOf<ImplementTemplateItem>()
            for (j in 0 until itemsArr.length()) {
                val itemObj = itemsArr.getJSONObject(j)
                val specArr = itemObj.optJSONArray("specFields")
                val specs = mutableListOf<ImplementSpecField>()
                if (specArr != null) {
                    for (k in 0 until specArr.length()) {
                        val sObj = specArr.getJSONObject(k)
                        val optArr = sObj.getJSONArray("options")
                        val opts = (0 until optArr.length()).map { optArr.getString(it) }
                        specs.add(
                            ImplementSpecField(
                                key = sObj.getString("key"),
                                label = sObj.getString("label"),
                                options = opts,
                                default = sObj.optString("default", opts.firstOrNull() ?: "")
                            )
                        )
                    }
                }
                items.add(
                    ImplementTemplateItem(
                        id = itemObj.getString("id"),
                        name = itemObj.getString("name"),
                        hindiName = itemObj.getString("hindiName"),
                        icon = itemObj.optString("icon", "tool"),
                        defaultRate = itemObj.optDouble("defaultRate", 800.0),
                        defaultRateType = itemObj.optString("defaultRateType", "HOUR"),
                        customUnitName = itemObj.optString("customUnitName").takeIf { it.isNotBlank() },
                        specFields = specs
                    )
                )
            }
            list.add(ImplementCategoryGroup(cat, catName, items))
        }
        list
    } catch (_: Exception) {
        emptyList()
    }
}

val ALL_RATE_TYPES = listOf(
    "HOUR" to "प्रति घंटा (Per hour)",
    "ACRE" to "प्रति एकड़ (Per acre)",
    "BIGHA" to "प्रति बीघा (Per bigha)",
    "HECTARE" to "प्रति हेक्टेयर (Per hectare)",
    "GUNTHA" to "प्रति गुंठा (Per guntha)",
    "KANAL" to "प्रति कनाल (Per kanal)",
    "MARLA" to "प्रति मरला (Per marla)",
    "CENT" to "प्रति सेंट (Per cent)",
    "CHAKKAR" to "प्रति चक्कर / ट्रिप (Per trip)",
    "DAY" to "प्रति दिन (Per day)",
    "FIXED" to "फिक्स रेट (Fixed price)",
    "CUSTOM" to "कस्टम यूनिट (Custom unit)"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ImplementPickerSheet(
    onDismiss: () -> Unit,
    onImplementSelected: (ImplementEntity) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categories = remember { loadImplementsFromAssets(context) }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()?.category ?: "TILLAGE") }
    var searchQuery by remember { mutableStateOf("") }

    // Selected implement for spec configuration
    var selectedItem by remember { mutableStateOf<ImplementTemplateItem?>(null) }

    // Spec configuration state
    val chosenSpecs = remember { mutableStateMapOf<String, String>() }
    var rateStr by remember { mutableStateOf("800") }
    var selectedRateType by remember { mutableStateOf("HOUR") }
    var customUnitName by remember { mutableStateOf("") }
    var customImplementName by remember { mutableStateOf("") }

    val currentCatObj = categories.find { it.category == selectedCategory }
    val itemsForCategory = currentCatObj?.items ?: emptyList()

    val filteredItems = remember(itemsForCategory, searchQuery) {
        if (searchQuery.isBlank()) itemsForCategory
        else {
            val q = searchQuery.lowercase().trim()
            itemsForCategory.filter {
                it.name.lowercase().contains(q) || it.hindiName.lowercase().contains(q)
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
                .testTag("implement_picker_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedItem != null) {
                        IconButton(onClick = { selectedItem = null }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = DeepForestGreen)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MintTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                        Text(
                            text = if (selectedItem != null) "${selectedItem?.hindiName} विवरण (Specs)" else "इंप्लीमेंट चुनें (Select Implement)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepForestGreen
                        )
                        Text(
                            text = if (selectedItem != null) "नाप, ब्लेड, रेट और यूनिट तय करें" else "कृषि उपकरण चुनकर रेट तय करें",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedItem == null) {
                // CATEGORY TABS
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { cat ->
                        val isSel = cat.category == selectedCategory
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedCategory = cat.category },
                            label = {
                                Text(
                                    text = cat.categoryName,
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

                Spacer(modifier = Modifier.height(8.dp))

                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("उपकरण खोजें (रोटावेटर, हल, ट्रॉली)...", fontSize = 13.sp) },
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

                // 2-Column Grid of implements
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedItem = item
                                    rateStr = item.defaultRate.toInt().toString()
                                    selectedRateType = item.defaultRateType
                                    customUnitName = item.customUnitName ?: ""
                                    customImplementName = item.name
                                    chosenSpecs.clear()
                                    item.specFields.forEach { f ->
                                        chosenSpecs[f.key] = f.default
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = BorderStroke(1.dp, BorderNeutral)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MintTint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Agriculture, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.hindiName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = item.name,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "₹${item.defaultRate.toInt()} / ${item.defaultRateType.lowercase()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepForestGreen
                                )
                            }
                        }
                    }
                }
            } else {
                // SPEC SHEET FOR CHOSEN IMPLEMENT
                val item = selectedItem!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Spec Fields (Width, Blades, Tines, etc.)
                    items(item.specFields) { spec ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardWhite, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = spec.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                spec.options.forEach { opt ->
                                    val isOptSel = chosenSpecs[spec.key] == opt
                                    FilterChip(
                                        selected = isOptSel,
                                        onClick = { chosenSpecs[spec.key] = opt },
                                        label = { Text(opt, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AmberAccent,
                                            selectedLabelColor = DeepForestGreen,
                                            containerColor = WarmCreamBg,
                                            labelColor = TextPrimary
                                        ),
                                        border = BorderStroke(1.dp, if (isOptSel) AmberAccent else BorderNeutral)
                                    )
                                }
                            }
                        }
                    }

                    // Rate Input
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardWhite, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text("डिफ़ॉल्ट किराया / रेट (₹)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = rateStr,
                                onValueChange = { rateStr = it },
                                placeholder = { Text("800") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = {
                                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice", tint = DeepForestGreen)
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = WarmCreamBg,
                                    unfocusedContainerColor = WarmCreamBg
                                )
                            )
                        }
                    }

                    // Rate Type Chips
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardWhite, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text("किराया प्रकार (Rate Type)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ALL_RATE_TYPES.forEach { (typeKey, label) ->
                                    val isSel = selectedRateType == typeKey
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedRateType = typeKey },
                                        label = { Text(label, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DeepForestGreen,
                                            selectedLabelColor = Color.White,
                                            containerColor = WarmCreamBg,
                                            labelColor = TextPrimary
                                        ),
                                        border = BorderStroke(1.dp, if (isSel) DeepForestGreen else BorderNeutral)
                                    )
                                }
                            }

                            if (selectedRateType == "CUSTOM") {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customUnitName,
                                    onValueChange = { customUnitName = it },
                                    label = { Text("यूनिट का नाम (उदा. प्रति कट्टा, प्रति बोरी)", fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    // Auto-built Details Preview
                    item {
                        val specText = chosenSpecs.values.joinToString(" • ")
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MintTint,
                            border = BorderStroke(1.dp, BorderNeutral),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("ऑटो विवरण सारांश (Details Summary):", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = if (specText.isNotBlank()) specText else "मानक उपकरण (Standard)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepForestGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sticky Save Button
                Button(
                    onClick = {
                        val itemVal = selectedItem ?: return@Button
                        val detailsText = chosenSpecs.values.joinToString(" • ")
                        val finalRate = rateStr.toDoubleOrNull() ?: itemVal.defaultRate
                        val entity = ImplementEntity(
                            name = itemVal.name,
                            hindiName = itemVal.hindiName,
                            category = itemVal.specFields.firstOrNull()?.key ?: "TILLAGE",
                            rateType = selectedRateType,
                            defaultRate = finalRate,
                            customUnitName = if (selectedRateType == "CUSTOM") customUnitName else null,
                            details = detailsText,
                            icon = itemVal.icon
                        )
                        onImplementSelected(entity)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_implement_spec_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepForestGreen,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
                ) {
                    Text("यह उपकरण जोड़ें (Save Implement)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
