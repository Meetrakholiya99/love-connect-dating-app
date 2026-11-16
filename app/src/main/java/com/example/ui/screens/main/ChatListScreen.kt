package com.example.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.components.ProfilePhoto
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.RosePrimary
import com.example.ui.viewmodel.MatchWithProfile
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatListScreen(
    matches: List<MatchWithProfile>,
    onOpenChat: (matchId: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    val filtered = remember(matches, searchQuery) {
        if (searchQuery.isBlank()) matches
        else matches.filter {
            it.profile.fullName.contains(searchQuery, ignoreCase = true) ||
                it.match.lastMessagePreview.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Messages",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 12.dp, bottom = 10.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search conversations...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Rounded.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("No Conversations Yet", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Only matched members can message each other for safety.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtered, key = { it.match.id }) { item ->
                    GlassCard(
                        onClick = { onOpenChat(item.match.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_thread_${item.match.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box {
                                ProfilePhoto(
                                    photoKey = item.profile.photos.firstOrNull(),
                                    contentDescription = item.profile.fullName,
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                )
                                if (item.profile.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldOnline)
                                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                            .align(Alignment.BottomEnd)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.profile.fullName,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.profile.isVerified) VerifiedBadge(modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = item.match.lastMessagePreview,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (item.match.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (item.match.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = timeFormatter.format(Date(item.match.lastMessageTime)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (item.match.unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(RosePrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.match.unreadCount.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
