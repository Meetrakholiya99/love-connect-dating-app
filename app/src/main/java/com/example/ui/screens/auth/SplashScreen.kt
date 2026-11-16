package com.example.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LoveConnectGradients
import com.example.ui.theme.OrchidPurple
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VelvetTextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isAuthenticated: Boolean,
    isOnboardingComplete: Boolean,
    onNavigateNext: (authenticated: Boolean, onboarded: Boolean) -> Unit
) {
    val scale = remember { Animatable(0.72f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow))
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(500))
        delay(950)
        onNavigateNext(isAuthenticated, isOnboardingComplete)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoveConnectGradients.DarkAtmosphere),
        contentAlignment = Alignment.Center
    ) {
        // Ambient radial glow behind emblem
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            RosePrimary.copy(alpha = 0.28f),
                            OrchidPurple.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .padding(32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_loveconnect_1794419097509),
                contentDescription = "LOVEConnect Emblem",
                modifier = Modifier
                    .size(116.dp)
                    .clip(CircleShape)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.brand_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = VelvetTextSecondary
            )
        }
    }
}
