package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.DiscoverCandidate
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwipeProfileCard(
    candidate: DiscoverCandidate,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSuperLike: () -> Unit,
    onOpenAiCompatibility: () -> Unit,
    onReportBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = candidate.profile
    val photos = profile.photos.ifEmpty { listOf("drawable:sophia") }
    var photoIndex by remember(profile.id) { mutableIntStateOf(0) }
    var expandedDetails by remember(profile.id) { mutableStateOf(false) }

    val offsetX = remember(profile.id) { Animatable(0f) }
    val offsetY = remember(profile.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val likeAlpha = (offsetX.value / 260f).coerceIn(0f, 1f)
    val nopeAlpha = (-offsetX.value / 260f).coerceIn(0f, 1f)
    val superAlpha = (-offsetY.value / 240f).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .graphicsLayer {
                rotationZ = (offsetX.value / 28f).coerceIn(-16f, 16f)
            }
            .pointerInput(profile.id) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            when {
                                offsetX.value > 260f -> {
                                    offsetX.animateTo(1200f, spring(stiffness = Spring.StiffnessMedium))
                                    onSwipeRight()
                                }
                                offsetX.value < -260f -> {
                                    offsetX.animateTo(-1200f, spring(stiffness = Spring.StiffnessMedium))
                                    onSwipeLeft()
                                }
                                offsetY.value < -260f -> {
                                    offsetY.animateTo(-1400f, spring(stiffness = Spring.StiffnessMedium))
                                    onSuperLike()
                                }
                                else -> {
                                    launch { offsetX.animateTo(0f, spring(dampingRatio = 0.65f)) }
                                    launch { offsetY.animateTo(0f, spring(dampingRatio = 0.65f)) }
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            launch { offsetX.animateTo(0f, spring()) }
                            launch { offsetY.animateTo(0f, spring()) }
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    scope.launch {
                        offsetX.snapTo(offsetX.value + dragAmount.x)
                        offsetY.snapTo(offsetY.value + dragAmount.y)
                    }
                }
            },
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ProfilePhoto(
                photoKey = photos.getOrNull(photoIndex),
                contentDescription = "${profile.fullName} photo",
                modifier = Modifier.fillMaxSize()
            )

            // Left / Right photo tap zones
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (photoIndex > 0) photoIndex--
                        }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (photoIndex < photos.lastIndex) photoIndex++
                        }
                )
            }

            // Bottom gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.62f)
                    .align(Alignment.BottomCenter)
                    .background(LoveConnectGradients.CardScrim)
            )

            // Photo indicator bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                photos.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (idx == photoIndex) Color.White else Color.White.copy(alpha = 0.35f)
                            )
                    )
                }
            }

            // Compatibility & Boost badges top row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 28.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, RosePrimary.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .clickable(onClick = onOpenAiCompatibility)
                        .testTag("ai_match_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AI Match",
                            tint = ChampagneGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${candidate.compatibilityScore}% Match",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (profile.isBoosted) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = OrchidPurple.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = "Boosted",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.45f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        IconButton(onClick = onReportBlock) {
                            Icon(
                                imageVector = Icons.Rounded.Shield,
                                contentDescription = "Safety Options",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Swipe stamps
            if (likeAlpha > 0.05f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 74.dp, start = 24.dp)
                        .alpha(likeAlpha)
                        .border(3.dp, EmeraldOnline, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "LIKE",
                        style = MaterialTheme.typography.headlineMedium,
                        color = EmeraldOnline,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            if (nopeAlpha > 0.05f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 74.dp, end = 24.dp)
                        .alpha(nopeAlpha)
                        .border(3.dp, CoralAlert, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "NOPE",
                        style = MaterialTheme.typography.headlineMedium,
                        color = CoralAlert,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            if (superAlpha > 0.05f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 170.dp)
                        .alpha(superAlpha)
                        .border(3.dp, SuperLikeCyan, RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SUPER LIKE",
                        style = MaterialTheme.typography.titleLarge,
                        color = SuperLikeCyan,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Profile details overlay at bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${profile.fullName.substringBefore(" ")}, ${profile.age}",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White
                        )
                        if (profile.isVerified) {
                            VerifiedBadge()
                        }
                        if (profile.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldOnline)
                            )
                        }
                    }

                    IconButton(
                        onClick = { expandedDetails = !expandedDetails },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                    ) {
                        Icon(
                            imageVector = if (expandedDetails) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.Info,
                            contentDescription = "Profile Info",
                            tint = Color.White
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.WorkOutline, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(15.dp))
                        Text(
                            text = profile.profession.ifBlank { profile.education },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = RoseSoft, modifier = Modifier.size(15.dp))
                        Text(
                            text = "~${profile.distanceKm} km · ${profile.city}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Text(
                    text = profile.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = if (expandedDetails) 5 else 2
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    profile.interests.take(if (expandedDetails) 6 else 3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = expandedDetails) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Goal: ${profile.relationshipGoal} · ${profile.heightCm} cm",
                            style = MaterialTheme.typography.bodySmall,
                            color = ChampagneGold
                        )
                        if (profile.education.isNotBlank()) {
                            Text(
                                text = "Education: ${profile.education}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        if (profile.languages.isNotEmpty()) {
                            Text(
                                text = "Languages: ${profile.languages.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCelebrationDialog(
    partner: UserProfileEntity,
    myPhotoKey: String?,
    isSuperLike: Boolean,
    onSendMessage: () -> Unit,
    onKeepSwiping: () -> Unit
) {
    Dialog(
        onDismissRequest = onKeepSwiping,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xEB0E0618))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.widthIn(max = 420.dp)
            ) {
                Icon(
                    imageVector = if (isSuperLike) Icons.Rounded.Star else Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = if (isSuperLike) SuperLikeCyan else RosePrimary,
                    modifier = Modifier.size(56.dp)
                )

                Text(
                    text = if (isSuperLike) "Super Match!" else "It's a Match!",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )

                Text(
                    text = "You and ${partner.fullName.substringBefore(" ")} liked each other.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = VelvetTextSecondary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy((-18).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfilePhoto(
                        photoKey = myPhotoKey ?: "drawable:sophia",
                        contentDescription = "Your photo",
                        modifier = Modifier
                            .size(116.dp)
                            .clip(CircleShape)
                            .border(3.dp, RosePrimary, CircleShape)
                    )
                    ProfilePhoto(
                        photoKey = partner.photos.firstOrNull(),
                        contentDescription = partner.fullName,
                        modifier = Modifier
                            .size(116.dp)
                            .clip(CircleShape)
                            .border(3.dp, OrchidPurple, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                BrandGradientButton(
                    text = "Send a Message",
                    onClick = onSendMessage,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "match_send_message_button"
                )

                OutlinedButton(
                    onClick = onKeepSwiping,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    Text("Keep Swiping", color = Color.White)
                }
            }
        }
    }
}
