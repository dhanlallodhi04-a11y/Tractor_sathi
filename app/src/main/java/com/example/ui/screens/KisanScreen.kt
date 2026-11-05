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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CustomerLedger
import com.example.ui.KhetSaathiViewModel
import com.example.ui.components.KhetCard
import com.example.ui.components.KhetInputField
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberLight
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.DueRed
import com.example.ui.theme.DueRedLight
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Strings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KisanScreen(
    viewModel: KhetSaathiViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddKisan: () -> Unit,
    onNavigateToKisanDetail: (String) -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val ledgers by viewModel.customerLedgers.collectAsStateWithLifecycle()
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
            if (msg.contains("Kisan") || msg.contains("Customer")) {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = Strings.get("deleted_success", lang),
                        actionLabel = Strings.get("undo", lang),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDeleteJob() // or undo delete kisan
                    } else {
                        viewModel.clearUndoMessage()
                    }
                }
            }
        }
    }

    val filteredList = remember(ledgers, debouncedQuery) {
        val query = debouncedQuery.trim().lowercase()
        if (query.isEmpty()) {
            ledgers
        } else {
            ledgers.filter {
                it.customer.name.lowercase().contains(query) ||
                it.customer.phone.lowercase().contains(query) ||
                it.customer.village.lowercase().contains(query)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.get("kisan_title", lang),
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
                onClick = onNavigateToAddKisan,
                containerColor = ForestGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .testTag("kisan_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Kisan", modifier = Modifier.size(28.dp))
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

            // Search bar (searches name, mobile and address)
            KhetInputField(
                label = "खोजें (Search)",
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "किसान का नाम, मोबाइल नंबर या गाँव...",
                testTag = "kisan_search_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Kisan Cards List or Empty state
            if (filteredList.isEmpty()) {
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
                                .padding(vertical = 32.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ForestGreenLight,
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = Strings.get("empty_kisan", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "किसान जोड़ें, काम का खाता रखें और सीधे व्हाट्सएप पर तगादा भेजें।",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = onNavigateToAddKisan,
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(22.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Strings.get("add_kisan", lang),
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
                    items(filteredList, key = { it.customer.id }) { ledger ->
                        val c = ledger.customer
                        // List card: round initials avatar, name, mobile & address (only if filled), baki amount in red (or Clear in green if zero)
                        KhetCard(
                            onClick = { onNavigateToKisanDetail(c.id) },
                            testTag = "kisan_item_${c.id}"
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
                                    // Round initials avatar
                                    Surface(
                                        shape = CircleShape,
                                        color = if (ledger.totalDue > 0) AmberLight else ForestGreenLight,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = c.name.take(1).uppercase(),
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (ledger.totalDue > 0) AmberGold else ForestGreen,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        // Name
                                        Text(
                                            text = c.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )

                                        // Mobile (only if filled)
                                        val cleanPhone = c.phone.replace("+91", "").trim()
                                        if (cleanPhone.isNotEmpty() && cleanPhone != "+91") {
                                            Text(
                                                text = c.phone,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }

                                        // Address / Village (only if filled)
                                        if (c.village.isNotBlank()) {
                                            Text(
                                                text = "गाँव: ${c.village}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Right side: baki amount in red (or "Clear" / हिसाब साफ़ in green if zero)
                                Column(horizontalAlignment = Alignment.End) {
                                    if (ledger.totalDue > 0) {
                                        Text(
                                            text = "₹${ledger.totalDue.toInt()}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = DueRed
                                        )
                                        Text(
                                            text = Strings.get("baki", lang),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = DueRed
                                        )
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PaidGreenLight
                                        ) {
                                            Text(
                                                text = Strings.get("clear_khata", lang),
                                                color = PaidGreen,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(30.dp)) }
                }
            }
        }
    }
}
