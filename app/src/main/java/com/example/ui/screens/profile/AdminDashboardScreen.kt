package com.example.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.data.local.ModerationReportEntity
import com.example.data.local.SubscriptionTransactionEntity
import com.example.data.repository.AdminDashboardStats
import com.example.ui.components.GlassCard
import com.example.ui.components.LoveConnectTopBar
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    stats: AdminDashboardStats,
    reports: List<ModerationReportEntity>,
    transactions: List<SubscriptionTransactionEntity>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { LoveConnectTopBar(title = "Admin Insights", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Platform Analytics", style = MaterialTheme.typography.headlineLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Total Users", stats.totalUsers.toString(), Icons.Rounded.Group, RosePrimary, Modifier.weight(1f))
                StatTile("Active Now", stats.activeUsers.toString(), Icons.Rounded.Bolt, EmeraldOnline, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Matches", stats.totalMatches.toString(), Icons.Rounded.Favorite, OrchidPurple, Modifier.weight(1f))
                StatTile("Messages", stats.totalMessages.toString(), Icons.Rounded.ChatBubble, SuperLikeCyan, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Premium", stats.premiumSubscribers.toString(), Icons.Rounded.WorkspacePremium, ChampagneGold, Modifier.weight(1f))
                StatTile("Verified", stats.verifiedAccounts.toString(), Icons.Rounded.Verified, SuperLikeCyan, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Reported", stats.reportedAccounts.toString(), Icons.Rounded.Flag, CoralAlert, Modifier.weight(1f))
                StatTile("Blocked", stats.blockedAccounts.toString(), Icons.Rounded.Block, CoralAlert, Modifier.weight(1f))
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Moderation Reports (${reports.size})", style = MaterialTheme.typography.titleMedium)
                    if (reports.isEmpty()) {
                        Text(
                            "No safety reports filed yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        reports.forEach { report ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("${report.targetName} · ${report.reason}", style = MaterialTheme.typography.titleSmall)
                                if (report.details.isNotBlank()) {
                                    Text(report.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Recent Payments (${transactions.size})", style = MaterialTheme.typography.titleMedium)
                    if (transactions.isEmpty()) {
                        Text(
                            "No subscription transactions recorded yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        transactions.forEach { tx ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${tx.planTitle} (${tx.status})", style = MaterialTheme.typography.bodySmall)
                                Text("₹${tx.amountInr}", style = MaterialTheme.typography.titleSmall, color = EmeraldOnline)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(value, style = MaterialTheme.typography.headlineMedium)
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(28.dp))
        }
    }
}
