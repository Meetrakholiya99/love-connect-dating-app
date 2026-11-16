package com.example.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProfileEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.LoveConnectTopBar
import com.example.ui.theme.CoralAlert

@Composable
fun ReportBlockScreen(
    targetProfile: UserProfileEntity?,
    onSubmitReport: (target: UserProfileEntity, reason: String, details: String, blockAlso: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val reasons = listOf(
        "Fake profile or impersonation",
        "Spam or promotional links",
        "Harassment or abusive behavior",
        "Underage user concern (under 18)",
        "Inappropriate photos or bio"
    )
    var selectedReason by remember { mutableStateOf(reasons.first()) }
    var details by remember { mutableStateOf("") }
    var blockAlso by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { LoveConnectTopBar(title = "Safety Report & Block", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (targetProfile == null) {
                Text("Profile not found.", style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(
                    text = "Report ${targetProfile.fullName}",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "Your report is confidential and helps keep LOVEConnect safe.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Select a Reason", style = MaterialTheme.typography.titleMedium)
                        reasons.forEach { reason ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = selectedReason == reason,
                                    onClick = { selectedReason = reason }
                                )
                                Text(reason, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Additional Details (Optional)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Block ${targetProfile.fullName.substringBefore(" ")}", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Removes them from your matches, chat, and discovery immediately.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = blockAlso, onCheckedChange = { blockAlso = it })
                }

                Button(
                    onClick = { onSubmitReport(targetProfile, selectedReason, details, blockAlso) },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralAlert),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_report_button")
                ) {
                    Text(if (blockAlso) "Submit Report & Block User" else "Submit Report")
                }
            }
        }
    }
}
