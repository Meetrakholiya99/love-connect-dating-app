package com.example.ui.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProfileEntity
import com.example.ui.components.BrandGradientButton
import com.example.ui.components.ProfilePhoto
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.LoveConnectGradients
import com.example.ui.theme.RosePrimary

@Composable
fun LikesScreen(
    whoLikedYou: List<UserProfileEntity>,
    isPremium: Boolean,
    onLikeBack: (UserProfileEntity) -> Unit,
    onPass: (UserProfileEntity) -> Unit,
    onUpgradePremium: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Likes You", style = MaterialTheme.typography.headlineLarge)
                Text(
                    "${whoLikedYou.size} people already liked your profile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isPremium) {
                Surface(
                    onClick = onUpgradePremium,
                    shape = RoundedCornerShape(50),
                    color = ChampagneGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, ChampagneGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(16.dp))
                        Text("Unlock All", style = MaterialTheme.typography.labelSmall, color = ChampagneGold)
                    }
                }
            }
        }

        if (whoLikedYou.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Favorite, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(48.dp))
                    Text("No Pending Likes Yet", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Activate a Profile Boost or keep swiping to attract more admirers.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 156.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(whoLikedYou, key = { it.id }) { profile ->
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.72f)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ProfilePhoto(
                                photoKey = profile.photos.firstOrNull(),
                                contentDescription = profile.fullName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .let { if (!isPremium) it.blur(18.dp) else it }
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(LoveConnectGradients.CardScrim)
                            )

                            if (!isPremium) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.6f),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Rounded.Lock, contentDescription = "Locked", tint = ChampagneGold)
                                        }
                                    }
                                    Text(
                                        "Premium",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            "${profile.fullName.substringBefore(" ")}, ${profile.age}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                        if (profile.isVerified) VerifiedBadge(modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        "~${profile.distanceKm} km · ${profile.city}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        FilledTonalIconButton(
                                            onClick = { onPass(profile) },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Rounded.Close, contentDescription = "Pass")
                                        }
                                        IconButton(
                                            onClick = { onLikeBack(profile) },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(RosePrimary, CircleShape)
                                        ) {
                                            Icon(Icons.Rounded.Favorite, contentDescription = "Match", tint = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!isPremium) {
                BrandGradientButton(
                    text = "See Who Likes You · Upgrade",
                    onClick = onUpgradePremium,
                    brush = LoveConnectGradients.PremiumGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    testTag = "likes_upgrade_button"
                )
            }
        }
    }
}
