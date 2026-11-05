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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImplementsScreen(
    viewModel: KhetSaathiViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val implements by viewModel.implements.collectAsStateWithLifecycle()
    val undoMessage by viewModel.undoMessage.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            debouncedQuery = ""
        } else {
            kotlinx.coroutines.delay(250)
            debouncedQuery = searchQuery
        }
    }

    // Undo snackbar trigger
    LaunchedEffect(undoMessage) {
        undoMessage?.let { msg ->
            if (msg.contains("Implement")) {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = Strings.get("deleted_success", lang),
                        actionLabel = Strings.get("undo", lang),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDeleteImplement()
                    } else {
                        viewModel.clearUndoMessage()
                    }
                }
            }
        }
    }

    val filteredList = remember(implements, debouncedQuery) {
        if (debouncedQuery.isBlank()) implements
        else implements.filter {
            it.name.contains(debouncedQuery, ignoreCase = true) ||
            it.hindiName.contains(debouncedQuery, ignoreCase = true) ||
            it.details.contains(debouncedQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.get("implements_title", lang),
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
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
                onClick = onNavigateToAdd,
                containerColor = ForestGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .testTag("add_implement_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Implement", modifier = Modifier.size(28.dp))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search bar
            KhetInputField(
                label = "खोजें (Search)",
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "यंत्र का नाम या विवरण...",
                testTag = "implement_search_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Implement Cards List or Empty state
            if (filteredList.isEmpty()) {
                // Section 1: Empty state: friendly illustration + "Add your first implement"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    KhetCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ForestGreenLight,
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Agriculture,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = Strings.get("empty_implements", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "रोटावेटर, कल्टीवेटर, प्लाउ आदि जोड़ें और काम की दरें तय करें।",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = onNavigateToAdd,
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(22.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Strings.get("add_implement_btn", lang),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { imp ->
                        val unitLabel = when (imp.rateType) {
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
                            "CUSTOM" -> imp.customUnitName?.ifEmpty { "यूनिट" } ?: "यूनिट"
                            else -> imp.rateType
                        }

                        // List card: small round photo (or icon if no photo), name, short details line (e.g. "8 feet"), rate line (e.g. "₹800 / hour")
                        KhetCard(
                            onClick = { onNavigateToDetail(imp.id) },
                            testTag = "implement_item_${imp.id}"
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Small round photo (or icon if no photo)
                                    if (!imp.photo.isNullOrBlank()) {
                                        AsyncImage(
                                            model = imp.photo,
                                            contentDescription = imp.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Surface(
                                            shape = CircleShape,
                                            color = ForestGreenLight,
                                            modifier = Modifier.size(52.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Agriculture,
                                                    contentDescription = null,
                                                    tint = ForestGreen,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        // Name
                                        Text(
                                            text = imp.hindiName.ifEmpty { imp.name },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )

                                        // Short details line (e.g. "8 feet" or details)
                                        if (imp.details.isNotBlank()) {
                                            Text(
                                                text = imp.details,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary,
                                                maxLines = 1
                                            )
                                        }

                                        // Rate line: e.g. "₹800 / hour"
                                        Text(
                                            text = "₹${imp.defaultRate.toInt()} / $unitLabel",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Details",
                                    tint = TextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(30.dp)) }
                }
            }
        }
    }
}
