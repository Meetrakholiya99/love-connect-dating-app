package com.example.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.BrandGradientButton
import com.example.ui.components.ProfilePhoto
import com.example.ui.theme.*

@Composable
fun WelcomeScreen(
    onCreateAccount: () -> Unit,
    onLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Hero layered portrait collage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(RosePrimary.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                )

                ProfilePhoto(
                    photoKey = "drawable:marcus",
                    contentDescription = "Featured member Marcus",
                    modifier = Modifier
                        .offset(x = (-72).dp, y = 16.dp)
                        .rotate(-9f)
                        .size(width = 148.dp, height = 200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, OrchidPurple.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                )

                ProfilePhoto(
                    photoKey = "drawable:elena",
                    contentDescription = "Featured member Elena",
                    modifier = Modifier
                        .offset(x = 72.dp, y = 20.dp)
                        .rotate(9f)
                        .size(width = 148.dp, height = 200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, ChampagneGold.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                )

                ProfilePhoto(
                    photoKey = "drawable:sophia",
                    contentDescription = "Featured member Sophia",
                    modifier = Modifier
                        .size(width = 168.dp, height = 232.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .border(2.5.dp, RosePrimary, RoundedCornerShape(26.dp))
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = VelvetObsidian.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, RosePrimary.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-8).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(16.dp))
                        Text(
                            text = "94% Compatibility Match",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.widthIn(max = 480.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.age_notice),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = "Where Chemistry Meets Clarity",
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Discover verified profiles nearby, enjoy intelligent compatibility insights, and spark real conversations.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                BrandGradientButton(
                    text = "Create 18+ Account",
                    onClick = onCreateAccount,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "welcome_create_account_button"
                )

                OutlinedButton(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("welcome_login_button"),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Sign In to Existing Account",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
