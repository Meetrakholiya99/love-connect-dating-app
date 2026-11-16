package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.local.AppNotificationEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.LoveConnectTopBar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    notifications: List<AppNotificationEntity>,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onBack: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM d · h:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            LoveConnectTopBar(
                title = "Notifications",
                onBack = onBack,
                actions = {
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = onMarkAllRead) {
                            Text("Mark All Read")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.NotificationsNone, contentDescription = null, modifier = Modifier.size(48.dp))
                    Text("You're All Caught Up", style = MaterialTheme.typography.titleLarge)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    val (icon, tint) = when (notif.type) {
                        "MATCH" -> Icons.Rounded.Favorite to RosePrimary
                        "SUPER_LIKE" -> Icons.Rounded.Star to SuperLikeCyan
                        "LIKE" -> Icons.Rounded.FavoriteBorder to OrchidPurple
                        "CALL" -> Icons.Rounded.Call to EmeraldOnline
                        "SECURITY" -> Icons.Rounded.Shield to ChampagneGold
                        else -> Icons.Rounded.Notifications to RosePrimary
                    }

                    GlassCard(
                        onClick = { onMarkRead(notif.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = tint.copy(alpha = 0.16f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, contentDescription = null, tint = tint)
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(notif.title, style = MaterialTheme.typography.titleSmall)
                                Text(notif.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    formatter.format(Date(notif.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!notif.isRead) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(RosePrimary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
