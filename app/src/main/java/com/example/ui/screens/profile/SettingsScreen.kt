package com.example.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.UserAccountEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.LoveConnectTopBar
import com.example.ui.theme.CoralAlert

@Composable
fun SettingsScreen(
    account: UserAccountEntity?,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleDiscoveryPaused: (Boolean) -> Unit,
    onUpdateNotificationPrefs: (matches: Boolean, messages: Boolean, likes: Boolean, security: Boolean) -> Unit,
    onOpenPrivacySecurity: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onLogout: () -> Unit,
    onLogoutAllDevices: () -> Unit,
    onDeleteAccount: () -> Unit,
    onBack: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { LoveConnectTopBar(title = "Settings", onBack = onBack) }
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Appearance & Discovery", style = MaterialTheme.typography.titleMedium)

                    SettingsSwitchRow(
                        title = "Dark Luxury Theme",
                        subtitle = "Obsidian velvet night mode",
                        checked = isDarkMode,
                        testTag = "settings_dark_mode_switch",
                        onCheckedChange = onToggleDarkMode
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    SettingsSwitchRow(
                        title = "Pause Discovery",
                        subtitle = "Hide your card while keeping existing matches",
                        checked = account?.isDiscoveryPaused == true,
                        onCheckedChange = onToggleDiscoveryPaused
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Notifications", style = MaterialTheme.typography.titleMedium)
                    val m = account?.pushMatchesEnabled ?: true
                    val msg = account?.pushMessagesEnabled ?: true
                    val l = account?.pushLikesEnabled ?: true
                    val s = account?.securityAlertsEnabled ?: true

                    SettingsSwitchRow("New Matches", "Alert when mutual spark happens", m) {
                        onUpdateNotificationPrefs(it, msg, l, s)
                    }
                    SettingsSwitchRow("New Messages", "Real-time chat notifications", msg) {
                        onUpdateNotificationPrefs(m, it, l, s)
                    }
                    SettingsSwitchRow("Profile Likes & Super Likes", "See when admirers tap", l) {
                        onUpdateNotificationPrefs(m, msg, it, s)
                    }
                    SettingsSwitchRow("Security Alerts", "Login, OTP & password updates", s) {
                        onUpdateNotificationPrefs(m, msg, l, it)
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Account & Safety", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(
                        onClick = onOpenPrivacySecurity,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Rounded.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Privacy, Verification & Blocked Users")
                    }
                    OutlinedButton(
                        onClick = onOpenAdminDashboard,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Rounded.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Admin Analytics Dashboard")
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Session & Data Controls", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_logout_button"),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Log Out")
                    }

                    OutlinedButton(
                        onClick = onLogoutAllDevices,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Rounded.DevicesOther, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Log Out From All Devices")
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_delete_account_button"),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, CoralAlert),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralAlert)
                    ) {
                        Icon(Icons.Rounded.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Account & All Data")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account Permanently?") },
            text = {
                Text("This permanently erases your profile, matches, chat history, and session data from this device.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralAlert)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
        )
    }
}
