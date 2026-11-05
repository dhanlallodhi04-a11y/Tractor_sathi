package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.NotificationEntity
import com.example.ui.KhetSaathiViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BlushPinkTint
import com.example.ui.theme.BorderDarkEdge
import com.example.ui.theme.BorderNeutral
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamYellowTint
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.MintTint
import com.example.ui.theme.MoneyDueDarkEdge
import com.example.ui.theme.MoneyDueLightTint
import com.example.ui.theme.MoneyDueRed
import com.example.ui.theme.MoneyInDarkEdge
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.MoneyInLightTint
import com.example.ui.theme.SkyTint
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSub
import com.example.ui.theme.WarmCreamBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    viewModel: KhetSaathiViewModel,
    onBack: () -> Unit,
    onNavigateDeepLink: (String) -> Unit
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

    val tabs = listOf(
        Pair("सब", "All"),
        Pair("बाकी", "Payments"),
        Pair("बधाई", "Milestones"),
        Pair("रिमाइंडर", "Reminders")
    )

    val filteredNotifications = remember(notifications, selectedTab) {
        when (selectedTab) {
            0 -> notifications // All
            1 -> notifications.filter { it.type == "PAYMENT" || it.channelId == "payments" }
            2 -> notifications.filter { it.type == "MILESTONE" || it.channelId == "milestones" }
            3 -> notifications.filter { it.type == "REMINDER" || it.type == "SUMMARY" || it.type == "ALERT" || it.type == "TIP" || it.channelId == "reminders" || it.channelId == "alerts" }
            else -> notifications
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (lang == "hi") "सूचना केंद्र (Notifications)" else "Notifications",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DeepForestGreen
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = MoneyDueRed,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                        color = CardWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("notification_center_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DeepForestGreen)
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.testTag("notification_menu_button")) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = DeepForestGreen)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("सब पढ़ा हुआ करें (Mark all read)") },
                            leadingIcon = { Icon(Icons.Default.DoneAll, contentDescription = null, tint = DeepForestGreen) },
                            onClick = {
                                viewModel.markAllNotificationsAsRead()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("सब हटाएं (Clear all)") },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MoneyDueRed) },
                            onClick = {
                                viewModel.clearAllNotifications()
                                showMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmCreamBg)
            )
        },
        containerColor = WarmCreamBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = WarmCreamBg,
                contentColor = DeepForestGreen,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = DeepForestGreen,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, pair ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = if (lang == "hi") pair.first else pair.second,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (isSelected) DeepForestGreen else TextSub
                            )
                        }
                    )
                }
            }

            // Notification List
            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = CreamYellowTint,
                            border = BorderStroke(1.5.dp, BorderNeutral),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    tint = DeepForestGreen.copy(alpha = 0.6f),
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == "hi") "कोई सूचना नहीं है" else "No Notifications",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextMain
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (lang == "hi") "यहाँ आपके बाकी पैसे, बधाई और काम की समय पर याद दिखाई देगी।" else "Reminders, milestone celebrations, and due notices will appear here.",
                            fontSize = 13.sp,
                            color = TextSub,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { item ->
                        NotificationCard(
                            notification = item,
                            onTap = {
                                viewModel.markNotificationAsRead(item.id)
                                if (item.deepLink.isNotEmpty()) {
                                    onNavigateDeepLink(item.deepLink)
                                }
                            },
                            onDelete = {
                                viewModel.deleteNotification(item.id)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationEntity,
    onTap: () -> Unit,
    onDelete: () -> Unit
) {
    val isUnread = notification.readAt == null

    val (badgeBg, iconTint, icon) = when (notification.type) {
        "MILESTONE" -> Triple(MoneyInLightTint, MoneyInGreen, Icons.Default.EmojiEvents)
        "PAYMENT" -> Triple(MoneyDueLightTint, MoneyDueRed, Icons.Default.Payment)
        "REMINDER" -> Triple(CreamYellowTint, AmberDark, Icons.Default.Schedule)
        "ALERT" -> Triple(BlushPinkTint, MoneyDueRed, Icons.Default.Info)
        "TIP" -> Triple(SkyTint, DeepForestGreen, Icons.Default.Info)
        else -> Triple(MintTint, DeepForestGreen, Icons.Default.Notifications)
    }

    val timeAgo = remember(notification.createdAt) {
        val diff = System.currentTimeMillis() - notification.createdAt
        val mins = diff / 60000L
        val hours = mins / 60L
        val days = hours / 24L
        when {
            mins < 1 -> "अभी"
            mins < 60 -> "$mins मिनट पहले"
            hours < 24 -> "$hours घंटे पहले"
            days == 1L -> "कल"
            else -> "$days दिन पहले"
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isUnread) BorderDarkEdge else BorderNeutral,
        shadowElevation = if (isUnread) 2.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .testTag("notification_item_${notification.id}")
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = if (isUnread) 3.dp else 2.dp)
                .background(
                    if (isUnread) CardWhite else Color(0xFFFAFAF7),
                    RoundedCornerShape(16.dp)
                )
                .border(
                    BorderStroke(
                        if (isUnread) 1.5.dp else 1.dp,
                        if (isUnread) AmberAccent.copy(alpha = 0.5f) else BorderNeutral
                    ),
                    RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Colored round icon badge
                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = notification.title,
                                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = DeepForestGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isUnread) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(AmberAccent, CircleShape)
                                )
                            }
                        }

                        Text(
                            text = timeAgo,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = notification.body,
                        fontSize = 13.sp,
                        color = TextMain,
                        maxLines = 2,
                        lineHeight = 18.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
