package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.KhetSaathiViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BorderDarkEdge
import com.example.ui.theme.BorderNeutral
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamYellowTint
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.MintTint
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSub
import com.example.ui.theme.WarmCreamBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AchievementItem(
    val key: String,
    val title: String,
    val desc: String,
    val emoji: String,
    val targetDesc: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: KhetSaathiViewModel,
    onBack: () -> Unit
) {
    val milestones by viewModel.milestones.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val implements by viewModel.implements.collectAsStateWithLifecycle()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    val milestoneMap = remember(milestones) {
        milestones.associateBy { it.key }
    }

    val achievementCatalog = listOf(
        AchievementItem("kisan_first", "पहला किसान", "पहला ग्राहक जोड़ा", "🌱", "1 किसान"),
        AchievementItem("first_tractor", "पहला ट्रैक्टर", "वाहन बेड़े में शामिल", "🚜", "1 ट्रैक्टर"),
        AchievementItem("first_implement", "पहला औज़ार", "औज़ारों का खाता शुरू", "🛠️", "1 औज़ार"),
        AchievementItem("first_job", "पहला काम", "काम का पक्का ब्यौरा", "📝", "1 काम"),
        AchievementItem("kisan_10", "10 किसान", "कारोबार का विस्तार", "🌱", "10 किसान"),
        AchievementItem("kisan_50", "50 किसान", "मजबूत ग्राहक संबंध", "🎉", "50 किसान"),
        AchievementItem("kisan_100", "100 किसान", "इलाके का भरोसेमंद नाम", "🏆", "100 किसान"),
        AchievementItem("kisan_200", "200 किसान", "विशाल नेटवर्क", "🥇", "200 किसान"),
        AchievementItem("kisan_500", "500 किसान", "सच्चे किसान साथी", "👑", "500 किसान"),
        AchievementItem("kisan_1000", "1000 किसान", "ऐतिहासिक कीर्तिमान", "🌟", "1000 किसान"),
        AchievementItem("jobs_100", "शतकवीर (100 काम)", "100 काम सफलतापूर्वक दर्ज", "🎯", "100 काम"),
        AchievementItem("earnings_100000", "लाखपति ट्रैक्टर मालिक", "कुल काम ₹1,00,000 पार", "💰", "₹1,00,000 कमाई")
    )

    // Evaluate live unlocks
    val totalEarnings = remember(jobs) { jobs.sumOf { it.total } }

    val unlockedCount = achievementCatalog.count { item ->
        val entity = milestoneMap[item.key]
        val isLiveUnlocked = when (item.key) {
            "kisan_first" -> customers.isNotEmpty()
            "first_tractor" -> vehicles.isNotEmpty()
            "first_implement" -> implements.isNotEmpty()
            "first_job" -> jobs.isNotEmpty()
            "kisan_10" -> customers.size >= 10
            "kisan_50" -> customers.size >= 50
            "kisan_100" -> customers.size >= 100
            "kisan_200" -> customers.size >= 200
            "kisan_500" -> customers.size >= 500
            "kisan_1000" -> customers.size >= 1000
            "jobs_100" -> jobs.size >= 100
            "earnings_100000" -> totalEarnings >= 100000.0
            else -> false
        }
        entity != null || isLiveUnlocked
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (lang == "hi") "उपलब्धियां (Achievements)" else "Achievements",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DeepForestGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DeepForestGreen)
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
            // Header Stats Banner
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = BorderDarkEdge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                        .background(CardWhite, RoundedCornerShape(18.dp))
                        .border(BorderStroke(1.dp, BorderNeutral), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CreamYellowTint,
                            border = BorderStroke(1.5.dp, AmberAccent),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AmberDark, modifier = Modifier.size(30.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "$unlockedCount / ${achievementCatalog.size} मेडल हासिल किए",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DeepForestGreen
                            )
                            Text(
                                text = "जैसे-जैसे आपका काम और किसान बढ़ेंगे, नए मेडल अनलॉक होंगे।",
                                fontSize = 12.sp,
                                color = TextSub
                            )
                        }
                    }
                }
            }

            // Grid of Badges
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(achievementCatalog, key = { it.key }) { item ->
                    val entity = milestoneMap[item.key]
                    val isLiveUnlocked = when (item.key) {
                        "kisan_first" -> customers.isNotEmpty()
                        "first_tractor" -> vehicles.isNotEmpty()
                        "first_implement" -> implements.isNotEmpty()
                        "first_job" -> jobs.isNotEmpty()
                        "kisan_10" -> customers.size >= 10
                        "kisan_50" -> customers.size >= 50
                        "kisan_100" -> customers.size >= 100
                        "kisan_200" -> customers.size >= 200
                        "kisan_500" -> customers.size >= 500
                        "kisan_1000" -> customers.size >= 1000
                        "jobs_100" -> jobs.size >= 100
                        "earnings_100000" -> totalEarnings >= 100000.0
                        else -> false
                    }
                    val isUnlocked = entity != null || isLiveUnlocked
                    val unlockDateStr = entity?.let {
                        SimpleDateFormat("dd MMM yyyy", Locale("hi", "IN")).format(Date(it.achievedAt))
                    }

                    BadgeCard(item = item, isUnlocked = isUnlocked, unlockDate = unlockDateStr)
                }
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun BadgeCard(
    item: AchievementItem,
    isUnlocked: Boolean,
    unlockDate: String?
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isUnlocked) BorderDarkEdge else Color(0xFFE0DDD5),
        shadowElevation = if (isUnlocked) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("badge_${item.key}")
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = if (isUnlocked) 3.dp else 1.dp)
                .background(
                    if (isUnlocked) CardWhite else Color(0xFFF2EFE8),
                    RoundedCornerShape(18.dp)
                )
                .border(
                    BorderStroke(
                        1.dp,
                        if (isUnlocked) AmberAccent.copy(alpha = 0.5f) else BorderNeutral
                    ),
                    RoundedCornerShape(18.dp)
                )
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Round Emoji Badge
                Surface(
                    shape = CircleShape,
                    color = if (isUnlocked) CreamYellowTint else Color(0xFFE8E5DD),
                    border = BorderStroke(1.5.dp, if (isUnlocked) AmberAccent else Color(0xFFD0CCC2)),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isUnlocked) {
                            Text(text = item.emoji, fontSize = 26.sp)
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = TextMuted, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isUnlocked) DeepForestGreen else TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isUnlocked) item.desc else "लक्ष्य: ${item.targetDesc}",
                    fontSize = 11.sp,
                    color = if (isUnlocked) TextSub else TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )

                if (isUnlocked && unlockDate != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "प्राप्त: $unlockDate",
                        fontSize = 10.sp,
                        color = MoneyInGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
