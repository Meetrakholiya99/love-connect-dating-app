package com.example.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.UserAccountEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.LoveConnectTopBar
import com.example.ui.components.ProfilePhoto
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.SuperLikeCyan

@Composable
fun PrivacySecurityScreen(
    account: UserAccountEntity?,
    profile: UserProfileEntity?,
    blockedProfiles: List<UserProfileEntity>,
    onToggleVerification: () -> Unit,
    onUnblockUser: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { LoveConnectTopBar(title = "Privacy & Security", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = EmeraldOnline)
                        Text("18+ & OTP Verification", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        "Adult Age Gate: ${if (account?.isAdultVerified == true) "Verified 18+" else "Checked"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Phone OTP Status: ${if (account?.isOtpVerified == true) "Verified" else "Pending"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Verified, contentDescription = null, tint = SuperLikeCyan)
                        Text("Profile Verification Badge", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        "Verified profiles receive higher trust and can appear in Verified-Only discovery filters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onToggleVerification,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(if (profile?.isVerified == true) "Verification Badge Active (Tap to Toggle)" else "Verify My Profile Now")
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Location & Data Privacy", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = stringResource(R.string.distance_privacy_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Blocked Accounts (${blockedProfiles.size})", style = MaterialTheme.typography.titleMedium)
                    if (blockedProfiles.isEmpty()) {
                        Text(
                            "You have not blocked anyone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        blockedProfiles.forEach { blocked ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ProfilePhoto(
                                        photoKey = blocked.photos.firstOrNull(),
                                        contentDescription = blocked.fullName,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                    Text(blocked.fullName, style = MaterialTheme.typography.bodyMedium)
                                }
                                OutlinedButton(onClick = { onUnblockUser(blocked.id) }) {
                                    Text("Unblock")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
