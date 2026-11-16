package com.example.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.UserAccountEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.InterestPill
import com.example.ui.components.ProfilePhoto
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.LoveConnectGradients
import com.example.ui.theme.OrchidPurple
import com.example.ui.theme.RosePrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen(
    account: UserAccountEntity?,
    profile: UserProfileEntity?,
    onEditProfile: () -> Unit,
    onManagePhotos: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenFilters: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPrivacySecurity: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onToggleBoost: () -> Unit
) {
    val isPremium = account?.subscriptionPlan != null && account.subscriptionPlan != "FREE"
    val completion = listOfNotNull(
        profile?.bio?.takeIf { it.isNotBlank() },
        profile?.profession?.takeIf { it.isNotBlank() },
        profile?.education?.takeIf { it.isNotBlank() },
        profile?.photos?.takeIf { it.size >= 2 },
        profile?.isVerified?.takeIf { it }
    ).size
    val completionRatio = ((completion + 2) / 7f).coerceIn(0.3f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Profile", style = MaterialTheme.typography.headlineLarge)
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("profile_settings_button")
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings")
            }
        }

        // Profile Hero Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    ProfilePhoto(
                        photoKey = profile?.photos?.firstOrNull(),
                        contentDescription = profile?.fullName ?: "Your Avatar",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(3.dp, RosePrimary, CircleShape)
                            .clickable(onClick = onManagePhotos)
                    )
                    Surface(
                        onClick = onManagePhotos,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = "Edit Photos", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${profile?.fullName ?: account?.fullName ?: "Member"}, ${profile?.age ?: 24}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (profile?.isVerified == true) VerifiedBadge()
                }

                Text(
                    text = "${profile?.profession?.ifBlank { "Member" }} · ${profile?.city ?: "San Francisco"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!profile?.bio.isNullOrBlank()) {
                    Text(
                        text = profile!!.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Profile Completeness", style = MaterialTheme.typography.labelSmall)
                        Text("${(completionRatio * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = RosePrimary)
                    }
                    LinearProgressIndicator(
                        progress = { completionRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }

                if (!profile?.interests.isNullOrEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        profile!!.interests.take(4).forEach { tag ->
                            InterestPill(label = tag)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditProfile,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_edit_button"),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Edit Info")
                    }
                    OutlinedButton(
                        onClick = onManagePhotos,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_photos_button"),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Photos (${profile?.photos?.size ?: 1}/6)")
                    }
                }
            }
        }

        // Premium & Boost Banner
        Surface(
            onClick = if (isPremium) onToggleBoost else onOpenPremium,
            shape = RoundedCornerShape(22.dp),
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(if (isPremium) LoveConnectGradients.BrandPrimary else LoveConnectGradients.PremiumGold)
                .testTag("profile_premium_banner")
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isPremium) {
                            if (account?.isBoostActive == true) "Boost Active · ${account.subscriptionPlan}" else "LOVEConnect ${account?.subscriptionPlan}"
                        } else {
                            "Upgrade to LOVEConnect Premium"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = if (isPremium) "Tap to toggle Profile Boost priority in Discover"
                        else "Unlimited likes, see who liked you, rewind & advanced filters",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Icon(
                    imageVector = if (isPremium) Icons.Rounded.Bolt else Icons.Rounded.WorkspacePremium,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Action Menu List
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ProfileMenuRow(
                    icon = Icons.Rounded.Tune,
                    title = "Discovery Filters",
                    subtitle = "Age, distance radius, relationship goal",
                    onClick = onOpenFilters
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ProfileMenuRow(
                    icon = Icons.Rounded.WorkspacePremium,
                    title = "Subscription & Plans",
                    subtitle = "Current plan: ${account?.subscriptionPlan ?: "FREE"}",
                    onClick = onOpenPremium
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ProfileMenuRow(
                    icon = Icons.Rounded.VerifiedUser,
                    title = "Privacy & Security",
                    subtitle = "18+ check, verification badge, blocked accounts",
                    onClick = onOpenPrivacySecurity
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ProfileMenuRow(
                    icon = Icons.Rounded.Analytics,
                    title = "Admin Analytics Dashboard",
                    subtitle = "Community metrics, moderation & payment logs",
                    onClick = onOpenAdminDashboard
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
