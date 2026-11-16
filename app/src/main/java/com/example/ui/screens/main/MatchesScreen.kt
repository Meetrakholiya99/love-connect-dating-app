package com.example.ui.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProfileEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.ProfilePhoto
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.SuperLikeCyan
import com.example.ui.viewmodel.MatchWithProfile
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MatchesScreen(
    matches: List<MatchWithProfile>,
    currentUserProfile: UserProfileEntity?,
    onOpenChat: (matchId: String) -> Unit,
    onOpenProfileDetail: (UserProfileEntity) -> Unit = {}
) {
    val formatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Matches",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )
        Text(
            text = "${matches.size} active connections",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (matches.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Favorite, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(48.dp))
                    Text("No Matches Yet", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Swipe right in Discover to spark your first mutual connection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text("New Sparks", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(matches, key = { it.match.id }) { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .width(78.dp)
                            .clickable { onOpenChat(item.match.id) }
                    ) {
                        Box {
                            ProfilePhoto(
                                photoKey = item.profile.photos.firstOrNull(),
                                contentDescription = item.profile.fullName,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.5.dp,
                                        if (item.match.isSuperLike) SuperLikeCyan else RosePrimary,
                                        CircleShape
                                    )
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
                        Text(
                            text = item.profile.fullName.substringBefore(" "),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Text("Match History & Shared Interests", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(matches, key = { "hist_${it.match.id}" }) { item ->
                    val myInterests = currentUserProfile?.interests?.map { it.lowercase() }?.toSet() ?: emptySet()
                    val shared = item.profile.interests.filter { it.lowercase() in myInterests }

                    GlassCard(
                        onClick = { onOpenChat(item.match.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("match_card_${item.profile.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            ProfilePhoto(
                                photoKey = item.profile.photos.firstOrNull(),
                                contentDescription = item.profile.fullName,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(18.dp))
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "${item.profile.fullName}, ${item.profile.age}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (item.profile.isVerified) VerifiedBadge(modifier = Modifier.size(16.dp))
                                    if (item.match.isSuperLike) {
                                        Icon(Icons.Rounded.Star, contentDescription = "Super Like", tint = SuperLikeCyan, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(
                                    text = if (shared.isNotEmpty()) {
                                        "Shared: ${shared.joinToString(", ")}"
                                    } else {
                                        "${item.profile.profession} · ~${item.profile.distanceKm} km"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Matched ${formatter.format(Date(item.match.matchedAt))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            FilledTonalIconButton(onClick = { onOpenChat(item.match.id) }) {
                                Icon(Icons.Rounded.ChatBubble, contentDescription = "Message")
                            }
                        }
                    }
                }
            }
        }
    }
}
