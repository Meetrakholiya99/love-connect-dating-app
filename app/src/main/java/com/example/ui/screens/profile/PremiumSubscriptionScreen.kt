package com.example.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SubscriptionTransactionEntity
import com.example.ui.components.BrandGradientButton
import com.example.ui.components.GlassCard
import com.example.ui.components.LoveConnectTopBar
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.LoveConnectGradients
import com.example.ui.theme.RosePrimary

data class PremiumPlanOption(
    val id: String,
    val title: String,
    val priceLabel: String,
    val amountInr: Int,
    val badge: String
)

@Composable
fun PremiumSubscriptionScreen(
    currentPlanId: String,
    transactions: List<SubscriptionTransactionEntity>,
    onSubscribe: (planId: String, planTitle: String, amountInr: Int) -> Unit,
    onBack: () -> Unit
) {
    val plans = listOf(
        PremiumPlanOption("MONTHLY", "Premium Monthly", "₹499 / month", 499, "Flexible"),
        PremiumPlanOption("QUARTERLY", "Premium Quarterly", "₹1,199 / 3 mos", 1199, "Save 20%"),
        PremiumPlanOption("YEARLY", "Premium Yearly", "₹2,999 / year", 2999, "Best Value · Save 50%")
    )
    var selectedPlan by remember { mutableStateOf(plans[1]) }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    val benefits = listOf(
        "Unlimited Daily Likes",
        "See Everyone Who Liked You",
        "Priority Profile Boost",
        "Unlimited Rewind Swipes",
        "Advanced Goal & Verified Filters",
        "5 Daily Super Likes"
    )

    Scaffold(
        topBar = { LoveConnectTopBar(title = "LOVEConnect Premium", onBack = onBack) }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(28.dp))
                        Column {
                            Text("Elevate Your Matches", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Current Tier: $currentPlanId",
                                style = MaterialTheme.typography.labelMedium,
                                color = ChampagneGold
                            )
                        }
                    }

                    benefits.forEach { perk ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EmeraldOnline, modifier = Modifier.size(18.dp))
                            Text(perk, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Text("Choose Your Plan", style = MaterialTheme.typography.titleMedium)

            plans.forEach { plan ->
                val isSelected = selectedPlan.id == plan.id
                val isCurrent = currentPlanId == plan.id
                Surface(
                    onClick = { selectedPlan = plan },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) RosePrimary else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(plan.title, style = MaterialTheme.typography.titleMedium)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = ChampagneGold.copy(alpha = 0.22f)
                                ) {
                                    Text(
                                        text = if (isCurrent) "ACTIVE" else plan.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ChampagneGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(plan.priceLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        RadioButton(selected = isSelected, onClick = { selectedPlan = plan })
                    }
                }
            }

            BrandGradientButton(
                text = "Continue · ${selectedPlan.priceLabel}",
                onClick = { showCheckoutDialog = true },
                brush = LoveConnectGradients.PremiumGold,
                modifier = Modifier.fillMaxWidth(),
                testTag = "subscribe_continue_button"
            )

            if (currentPlanId != "FREE") {
                OutlinedButton(
                    onClick = { onSubscribe("FREE", "Free Plan", 0) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Switch Back to Free Tier")
                }
            }

            if (transactions.isNotEmpty()) {
                Text("Payment History", style = MaterialTheme.typography.titleMedium)
                transactions.forEach { tx ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tx.planTitle, style = MaterialTheme.typography.titleSmall)
                                Text("Ref: ${tx.paymentRef}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("₹${tx.amountInr}", style = MaterialTheme.typography.titleMedium, color = EmeraldOnline)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("Confirm Plan Activation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Plan: ${selectedPlan.title} (${selectedPlan.priceLabel})")
                    Text(
                        "Local Sandbox Checkout: No external payment server is configured in this offline build. Confirming unlocks all Premium features immediately in your local database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCheckoutDialog = false
                        onSubscribe(selectedPlan.id, selectedPlan.title, selectedPlan.amountInr)
                    },
                    modifier = Modifier.testTag("confirm_subscription_button")
                ) {
                    Text("Activate Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}
