package com.example.ui.screens.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProfileEntity
import com.example.ui.components.ProfilePhoto
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.LoveConnectGradients
import com.example.ui.theme.RosePrimary
import kotlinx.coroutines.delay

@Composable
fun VideoCallScreen(
    partner: UserProfileEntity?,
    initialVideoEnabled: Boolean,
    onEndCall: (durationSec: Int) -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var isVideoEnabled by remember { mutableStateOf(initialVideoEnabled) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var callSeconds by remember { mutableIntStateOf(0) }
    var isConnected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1200)
        isConnected = true
        while (true) {
            delay(1000)
            callSeconds++
        }
    }

    val pulse = rememberInfiniteTransition(label = "call_pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val formattedDuration = "%02d:%02d".format(callSeconds / 60, callSeconds % 60)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoveConnectGradients.DarkAtmosphere)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        if (isVideoEnabled && partner != null) {
            ProfilePhoto(
                photoKey = partner.photos.firstOrNull(),
                contentDescription = "${partner.fullName} video feed",
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Call Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Text(
                        text = if (isVideoEnabled) "Video Preview · ${if (isFrontCamera) "Front" else "Rear"} Cam" else "Voice Call Preview",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
                Text(
                    text = partner?.fullName ?: "LOVEConnect Call",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                Text(
                    text = if (isConnected) formattedDuration else "Connecting...",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isConnected) EmeraldOnline else Color.White.copy(alpha = 0.8f)
                )
            }

            if (!isVideoEnabled) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(168.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(RosePrimary.copy(alpha = 0.25f))
                    )
                    ProfilePhoto(
                        photoKey = partner?.photos?.firstOrNull(),
                        contentDescription = partner?.fullName ?: "Avatar",
                        modifier = Modifier
                            .size(136.dp)
                            .clip(CircleShape)
                            .border(3.dp, RosePrimary, CircleShape)
                    )
                }
            } else {
                // Local PIP preview card
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF201333),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(width = 104.dp, height = 144.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Person, contentDescription = "You", tint = Color.White)
                                Text(
                                    if (isFrontCamera) "Front" else "Rear",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Call Controls
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.Black.copy(alpha = 0.62f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) Color.White else Color.White.copy(alpha = 0.16f))
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                            contentDescription = "Mute",
                            tint = if (isMuted) Color.Black else Color.White
                        )
                    }

                    IconButton(
                        onClick = { isVideoEnabled = !isVideoEnabled },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isVideoEnabled) Color.White.copy(alpha = 0.16f) else Color.White)
                    ) {
                        Icon(
                            imageVector = if (isVideoEnabled) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff,
                            contentDescription = "Toggle Video",
                            tint = if (isVideoEnabled) Color.White else Color.Black
                        )
                    }

                    IconButton(
                        onClick = { isFrontCamera = !isFrontCamera },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { isSpeakerOn = !isSpeakerOn },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isSpeakerOn) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = if (isSpeakerOn) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                            contentDescription = "Speaker",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { onEndCall(callSeconds) },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(CoralAlert)
                            .testTag("end_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
