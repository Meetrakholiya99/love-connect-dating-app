package com.example.ui.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProfileEntity
import com.example.data.repository.AiCompatibilityAnalysis
import com.example.ui.components.AiSampleNoticeBanner
import com.example.ui.components.BrandGradientButton
import com.example.ui.components.SwipeProfileCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AiPanelState
import com.example.ui.viewmodel.DiscoverCandidate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    candidates: List<DiscoverCandidate>,
    dailyLikesRemaining: Int,
    dailySuperLikesRemaining: Int,
    isPremium: Boolean,
    isDiscoveryPaused: Boolean,
    unreadNotifications: Int,
    aiCompatibilityState: AiPanelState<AiCompatibilityAnalysis>,
    onSwipe: (UserProfileEntity, String) -> Unit,
    onRewind: () -> Unit,
    onAnalyzeCompatibility: (UserProfileEntity) -> Unit,
    onResetCompatibility: () -> Unit,
    onOpenFilters: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPremium: () -> Unit,
    onReportBlock: (UserProfileEntity) -> Unit
) {
    var inspectedCandidate by remember { mutableStateOf<UserProfileEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LOVEConnect",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isPremium) "Premium · Unlimited Likes"
                    else "$dailyLikesRemaining likes · $dailySuperLikesRemaining super left",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPremium) ChampagneGold else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isPremium) {
                    Surface(
                        onClick = onOpenPremium,
                        shape = RoundedCornerShape(50),
                        color = ChampagneGold.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, ChampagneGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(16.dp))
                            Text("VIP", style = MaterialTheme.typography.labelSmall, color = ChampagneGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                IconButton(
                    onClick = onOpenFilters,
                    modifier = Modifier.testTag("discover_filters_button")
                ) {
                    Icon(Icons.Rounded.Tune, contentDescription = "Discovery Filters")
                }

                BadgedBox(
                    badge = {
                        if (unreadNotifications > 0) {
                            Badge { Text(unreadNotifications.toString()) }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier.testTag("discover_notifications_button")
                    ) {
                        Icon(Icons.Rounded.Notifications, contentDescription = "Notifications")
                    }
                }
            }
        }

        // Card Stack Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .widthIn(max = 520.dp)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            when {
                isDiscoveryPaused -> {
                    EmptyDiscoveryState(
                        title = "Discovery Paused",
                        subtitle = "Your profile is hidden from discovery. Resume anytime in Settings.",
                        primaryActionLabel = "Open Filters",
                        onPrimaryAction = onOpenFilters
                    )
                }
                candidates.isEmpty() -> {
                    EmptyDiscoveryState(
                        title = "You're All Caught Up",
                        subtitle = "No more profiles match your current filters right now.",
                        primaryActionLabel = "Adjust Filters",
                        onPrimaryAction = onOpenFilters,
                        secondaryActionLabel = "Rewind Last Swipe",
                        onSecondaryAction = onRewind
                    )
                }
                else -> {
                    // Render next card behind for smooth depth
                    if (candidates.size > 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(0.95f)
                                .offset(y = 10.dp)
                        ) {
                            SwipeProfileCard(
                                candidate = candidates[1],
                                onSwipeRight = {},
                                onSwipeLeft = {},
                                onSuperLike = {},
                                onOpenAiCompatibility = {},
                                onReportBlock = {}
                            )
                        }
                    }
                    val top = candidates.first()
                    SwipeProfileCard(
                        candidate = top,
                        onSwipeRight = { onSwipe(top.profile, "LIKE") },
                        onSwipeLeft = { onSwipe(top.profile, "PASS") },
                        onSuperLike = { onSwipe(top.profile, "SUPER_LIKE") },
                        onOpenAiCompatibility = {
                            inspectedCandidate = top.profile
                            onAnalyzeCompatibility(top.profile)
                        },
                        onReportBlock = { onReportBlock(top.profile) }
                    )
                }
            }
        }

        // Action Buttons Row
        val topCandidate = candidates.firstOrNull()?.profile
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SwipeActionCircle(
                icon = Icons.Rounded.Replay,
                contentDescription = "Rewind",
                tint = ChampagneGold,
                size = 48.dp,
                enabled = true,
                onClick = onRewind,
                testTag = "swipe_rewind_button"
            )
            SwipeActionCircle(
                icon = Icons.Rounded.Close,
                contentDescription = "Pass",
                tint = CoralAlert,
                size = 62.dp,
                enabled = topCandidate != null && !isDiscoveryPaused,
                onClick = { topCandidate?.let { onSwipe(it, "PASS") } },
                testTag = "swipe_pass_button"
            )
            SwipeActionCircle(
                icon = Icons.Rounded.Star,
                contentDescription = "Super Like",
                tint = SuperLikeCyan,
                size = 52.dp,
                enabled = topCandidate != null && !isDiscoveryPaused,
                onClick = { topCandidate?.let { onSwipe(it, "SUPER_LIKE") } },
                testTag = "swipe_super_like_button"
            )
            SwipeActionCircle(
                icon = Icons.Rounded.Favorite,
                contentDescription = "Like",
                tint = RosePrimary,
                size = 62.dp,
                enabled = topCandidate != null && !isDiscoveryPaused,
                onClick = { topCandidate?.let { onSwipe(it, "LIKE") } },
                testTag = "swipe_like_button"
            )
            SwipeActionCircle(
                icon = Icons.Rounded.AutoAwesome,
                contentDescription = "AI Compatibility",
                tint = OrchidPurple,
                size = 48.dp,
                enabled = topCandidate != null && !isDiscoveryPaused,
                onClick = {
                    topCandidate?.let {
                        inspectedCandidate = it
                        onAnalyzeCompatibility(it)
                    }
                },
                testTag = "swipe_ai_button"
            )
        }
    }

    if (inspectedCandidate != null) {
        ModalBottomSheet(
            onDismissRequest = {
                inspectedCandidate = null
                onResetCompatibility()
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "AI Compatibility with ${inspectedCandidate?.fullName?.substringBefore(" ")}",
                    style = MaterialTheme.typography.headlineLarge
                )
                AiSampleNoticeBanner()

                when (aiCompatibilityState) {
                    is AiPanelState.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    is AiPanelState.Ready -> {
                        val analysis = aiCompatibilityState.data
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "${analysis.scorePercent}% Match Potential",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(analysis.summary, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Text("Shared Strengths", style = MaterialTheme.typography.titleSmall)
                        analysis.sharedStrengths.forEach { strength ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EmeraldOnline, modifier = Modifier.size(18.dp))
                                Text(strength, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = analysis.suggestedDateIdea,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                    is AiPanelState.Error -> {
                        Text(aiCompatibilityState.message, color = MaterialTheme.colorScheme.error)
                    }
                    AiPanelState.Idle -> Unit
                }
            }
        }
    }
}

@Composable
private fun SwipeActionCircle(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    size: Dp,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(1.5.dp, if (enabled) tint.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .size(size)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) tint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(size * 0.46f)
            )
        }
    }
}

@Composable
private fun EmptyDiscoveryState(
    title: String,
    subtitle: String,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(24.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.size(84.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.Explore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BrandGradientButton(text = primaryActionLabel, onClick = onPrimaryAction)
        if (secondaryActionLabel != null && onSecondaryAction != null) {
            OutlinedButton(onClick = onSecondaryAction, shape = RoundedCornerShape(24.dp)) {
                Text(secondaryActionLabel)
            }
        }
    }
}
