package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.KhetSaathiViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BorderNeutral
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.MintTint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarmCreamBg
import org.json.JSONObject

data class FaqItem(val question: String, val answer: String)

fun loadFaqs(context: Context): List<FaqItem> {
    return try {
        val stream = context.assets.open("help.json")
        val jsonStr = stream.bufferedReader().use { it.readText() }
        val root = JSONObject(jsonStr)
        val arr = root.optJSONArray("faqs") ?: return emptyList()
        val list = mutableListOf<FaqItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(FaqItem(obj.getString("q"), obj.getString("a")))
        }
        list
    } catch (_: Exception) {
        emptyList()
    }
}

val ISSUE_TYPES = listOf(
    "हिसाब और बिलिंग समस्या",
    "ट्रेक्टर या इंप्लीमेंट जोड़ना",
    "सिंक और बैकअप समस्या",
    "सुझाव / नया फीचर अनुरोध",
    "अन्य पूछताछ"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    viewModel: KhetSaathiViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val faqs = remember { loadFaqs(context) }
    val supportTickets by viewModel.supportTickets.collectAsStateWithLifecycle()

    var selectedIssueType by remember { mutableStateOf(ISSUE_TYPES[0]) }
    var isIssueDropdownExpanded by remember { mutableStateOf(false) }
    var complaintMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionSuccess by remember { mutableStateOf<String?>(null) }

    // Search query for FAQs
    var faqSearchQuery by remember { mutableStateOf("") }
    val filteredFaqs = remember(faqs, faqSearchQuery) {
        if (faqSearchQuery.isBlank()) faqs
        else {
            val q = faqSearchQuery.lowercase().trim()
            faqs.filter { it.question.lowercase().contains(q) || it.answer.lowercase().contains(q) }
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                complaintMessage = if (complaintMessage.isBlank()) spoken else "$complaintMessage $spoken"
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("help_support_screen"),
        containerColor = WarmCreamBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "सहायता और सपोर्ट (Help & Support)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepForestGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = DeepForestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmCreamBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Quick contact channels
            item {
                Text("हमसे सीधे संपर्क करें (Contact Us)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // WhatsApp
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val url = "https://wa.me/919876543210?text=${Uri.encode("नमस्ते किसान साथी टीम, मुझे सहायता चाहिए।")}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, BorderNeutral)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MintTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("WA", fontWeight = FontWeight.Bold, color = DeepForestGreen, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("तुरंत चैट करें", fontSize = 10.sp, color = TextMuted)
                        }
                    }

                    // Call
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919876543210"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, BorderNeutral)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MintTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("कॉल करें", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("टोल-फ्री सपोर्ट", fontSize = 10.sp, color = TextMuted)
                        }
                    }

                    // Email
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@khetsaathi.in"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, BorderNeutral)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MintTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("ईमेल", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("समस्या भेजें", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }

            // Complaint / Feedback Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.5.dp, BorderNeutral),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.SupportAgent, contentDescription = null, tint = DeepForestGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("शिकायत या सुझाव दर्ज करें (Send Ticket)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = isIssueDropdownExpanded,
                            onExpandedChange = { isIssueDropdownExpanded = !isIssueDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedIssueType,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isIssueDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = WarmCreamBg,
                                    unfocusedContainerColor = WarmCreamBg
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = isIssueDropdownExpanded,
                                onDismissRequest = { isIssueDropdownExpanded = false }
                            ) {
                                ISSUE_TYPES.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type, fontSize = 13.sp) },
                                        onClick = {
                                            selectedIssueType = type
                                            isIssueDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Message Text Area with Voice
                        OutlinedTextField(
                            value = complaintMessage,
                            onValueChange = { complaintMessage = it },
                            placeholder = { Text("अपनी समस्या या सुझाव यहाँ विस्तार से लिखें या माइक दबाकर बोलें...", fontSize = 13.sp) },
                            minLines = 3,
                            maxLines = 5,
                            trailingIcon = {
                                IconButton(onClick = {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "अपनी समस्या बोलें...")
                                    }
                                    try { speechLauncher.launch(intent) } catch (_: Exception) {}
                                }) {
                                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice", tint = DeepForestGreen)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = WarmCreamBg,
                                unfocusedContainerColor = WarmCreamBg
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (submissionSuccess != null) {
                            Text(
                                text = submissionSuccess ?: "",
                                color = DeepForestGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (complaintMessage.isNotBlank()) {
                                    isSubmitting = true
                                    viewModel.submitSupportTicket(selectedIssueType, complaintMessage) { ok ->
                                        isSubmitting = false
                                        if (ok) {
                                            submissionSuccess = "आपकी शिकायत दर्ज कर ली गई है! टिकट नंबर जारी कर दिया गया है।"
                                            complaintMessage = ""
                                        }
                                    }
                                }
                            },
                            enabled = complaintMessage.isNotBlank() && !isSubmitting,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen, contentColor = Color.White)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("टिकट सबमिट करें (Submit Ticket)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Past Submitted Tickets
            if (supportTickets.isNotEmpty()) {
                item {
                    Text("आपके दर्ज किए गए टिकट (My Tickets)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                }
                items(supportTickets) { ticket ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, BorderNeutral)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = ticket.issueType, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Surface(
                                    color = if (ticket.status == "OPEN") AmberAccent.copy(alpha = 0.25f) else MintTint,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (ticket.status == "OPEN") "ओपन (Open)" else "समीक्षा में (In Review)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepForestGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = ticket.message, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }

            // FAQs Search & Accordion List
            item {
                Text("अक्सर पूछे जाने वाले सवाल (FAQs)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = faqSearchQuery,
                    onValueChange = { faqSearchQuery = it },
                    placeholder = { Text("सवाल खोजें (उदा: बैकअप, रेट)...", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite
                    )
                )
            }

            items(filteredFaqs) { faq ->
                var isExpanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, BorderNeutral)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = faq.question, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = DeepForestGreen
                            )
                        }
                        AnimatedVisibility(visible = isExpanded) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = faq.answer, fontSize = 13.sp, color = TextMuted, lineHeight = 19.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
