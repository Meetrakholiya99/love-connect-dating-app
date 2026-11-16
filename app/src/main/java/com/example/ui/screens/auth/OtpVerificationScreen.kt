package com.example.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.components.BrandGradientButton
import com.example.ui.components.GlassCard
import com.example.ui.components.LoveConnectTopBar
import com.example.ui.viewmodel.AuthUiState
import kotlinx.coroutines.delay

@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    authState: AuthUiState,
    onVerifyOtp: (String) -> Unit,
    onBack: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    var resendSeconds by remember { mutableIntStateOf(30) }

    LaunchedEffect(resendSeconds) {
        if (resendSeconds > 0) {
            delay(1000)
            resendSeconds--
        }
    }

    Scaffold(
        topBar = { LoveConnectTopBar(title = "Verify Phone", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("6-Digit Verification", style = MaterialTheme.typography.displayMedium)
                Text(
                    "We prepared a verification challenge for ${phoneNumber.ifBlank { "your phone" }}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Local Verification Code: 482910",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6) otpCode = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Enter 6-digit OTP") },
                        leadingIcon = { Icon(Icons.Rounded.Sms, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_code_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    if (authState is AuthUiState.Error) {
                        Text(
                            text = authState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    BrandGradientButton(
                        text = "Verify & Continue",
                        isLoading = authState is AuthUiState.Loading,
                        enabled = otpCode.length == 6,
                        onClick = { onVerifyOtp(otpCode) },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "otp_verify_button"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { otpCode = "482910" }) {
                            Text("Auto-fill 482910")
                        }
                        TextButton(
                            enabled = resendSeconds == 0,
                            onClick = { resendSeconds = 30 }
                        ) {
                            Text(
                                if (resendSeconds > 0) "Resend in ${resendSeconds}s" else "Resend Code"
                            )
                        }
                    }
                }
            }
        }
    }
}
