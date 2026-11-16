package com.example.ui.screens.onboarding

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProfileEntity
import com.example.ui.components.BrandGradientButton
import com.example.ui.components.LoveConnectTopBar
import com.example.ui.components.ProfilePhoto

@Composable
fun UploadPhotosScreen(
    currentUserProfile: UserProfileEntity?,
    isOnboarding: Boolean,
    onSavePhotos: (UserProfileEntity) -> Unit,
    onFinish: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var photos by remember(currentUserProfile) {
        mutableStateOf(
            currentUserProfile?.photos?.take(6)?.toMutableList()
                ?: mutableListOf("drawable:sophia")
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && photos.size < 6) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            photos = (photos + uri.toString()).take(6).toMutableList()
        }
    }

    Scaffold(
        topBar = {
            LoveConnectTopBar(
                title = "Your Photos (${photos.size}/6)",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 540.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Show Your Best Self",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    "Add up to 6 photos using Android Photo Picker (no broad storage permission required).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 540.dp)
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(6) { index ->
                    val photoKey = photos.getOrNull(index)
                    Surface(
                        modifier = Modifier
                            .aspectRatio(0.75f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                if (photoKey == null) {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            },
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.5.dp,
                            if (photoKey != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (photoKey != null) {
                                ProfilePhoto(
                                    photoKey = photoKey,
                                    contentDescription = "Profile photo ${index + 1}",
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (index == 0) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            "Main",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                if (photos.size > 1) {
                                    IconButton(
                                        onClick = {
                                            photos = photos.filterIndexed { i, _ -> i != index }.toMutableList()
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.65f))
                                    ) {
                                        Icon(
                                            Icons.Rounded.Close,
                                            contentDescription = "Remove photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.AddAPhoto,
                                        contentDescription = "Add Photo",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text("Add", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            // Quick studio preset photos for emulator testing without gallery photos
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 540.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Or add a studio sample portrait:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "drawable:sophia" to "Sunset",
                        "drawable:marcus" to "Cafe",
                        "drawable:elena" to "Gallery",
                        "drawable:liam" to "Coast"
                    ).forEach { (key, label) ->
                        OutlinedButton(
                            onClick = {
                                if (photos.size < 6 && key !in photos) {
                                    photos = (photos + key).toMutableList()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 540.dp)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Pick Photo")
                }

                BrandGradientButton(
                    text = if (isOnboarding) "Start Discovering" else "Save Photos",
                    onClick = {
                        currentUserProfile?.let { prof ->
                            onSavePhotos(prof.copy(photosCsv = photos.joinToString("|")))
                        }
                        onFinish()
                    },
                    modifier = Modifier.weight(1.3f),
                    testTag = "finish_photos_button"
                )
            }
        }
    }
}
