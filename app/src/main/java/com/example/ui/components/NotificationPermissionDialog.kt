package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BorderDarkEdge
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamYellowTint
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.MintTint
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub

@Composable
fun NotificationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BorderDarkEdge,
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .testTag("notification_permission_dialog")
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 3.dp)
                    .background(CardWhite, RoundedCornerShape(24.dp))
                    .padding(22.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Bell icon
                    Surface(
                        shape = CircleShape,
                        color = MintTint,
                        border = BorderStroke(1.5.dp, DeepForestGreen),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = DeepForestGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "सूचनाएं चालू करें (Notifications)",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepForestGreen,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "हम आपको बाकी पैसे, बधाई और काम की समय पर याद दिलाएंगे:",
                        fontSize = 14.sp,
                        color = TextSub,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Feature highlights
                    PermissionFeatureRow(
                        icon = Icons.Default.Payment,
                        title = "बाकी पैसे की वसूली",
                        desc = "तारीख आने पर समय पर याद दिलाएं ताकि कोई पैसा न छूटे"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PermissionFeatureRow(
                        icon = Icons.Default.Agriculture,
                        title = "काम और ट्रैक्टर सर्विस",
                        desc = "कल के काम और ट्रैक्टर सर्विस के घंटे का अलर्ट"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PermissionFeatureRow(
                        icon = Icons.Default.EmojiEvents,
                        title = "उपलब्धियां और बधाई",
                        desc = "किसानों की संख्या और कमाई के नए मुकाम पर बधाई"
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("allow_notification_button")
                    ) {
                        Text(
                            text = "चालू करें (Allow)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CardWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "बाद में (Not Now)",
                            fontSize = 14.sp,
                            color = TextSub
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionFeatureRow(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = CreamYellowTint,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AmberDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = TextSub
            )
        }
    }
}
