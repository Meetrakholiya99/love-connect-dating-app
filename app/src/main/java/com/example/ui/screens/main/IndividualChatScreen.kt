package com.example.ui.screens.main

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.data.local.ChatMessageEntity
import com.example.data.local.UserProfileEntity
import com.example.data.repository.AiIcebreakerResult
import com.example.ui.components.AiSampleNoticeBanner
import com.example.ui.components.ProfilePhoto
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.SuperLikeCyan
import com.example.ui.viewmodel.AiPanelState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualChatScreen(
    matchId: String,
    partner: UserProfileEntity?,
    messages: List<ChatMessageEntity>,
    isPartnerTyping: Boolean,
    aiIcebreakerState: AiPanelState<AiIcebreakerResult>,
    onMarkRead: () -> Unit,
    onSendMessage: (text: String, imageUri: String?, voiceSec: Int?) -> Unit,
    onDeleteMessage: (messageId: String) -> Unit,
    onLoadIcebreakers: (UserProfileEntity) -> Unit,
    onStartCall: (isVideo: Boolean) -> Unit,
    onReportBlock: (UserProfileEntity) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var messageInput by remember { mutableStateOf("") }
    var showAiTray by remember { mutableStateOf(false) }
    var showEmojiTray by remember { mutableStateOf(false) }
    var showSafetyMenu by remember { mutableStateOf(false) }
    var isRecordingVoice by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            onSendMessage("Shared a photo", uri.toString(), null)
        }
    }

    LaunchedEffect(matchId) {
        onMarkRead()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfilePhoto(
                            photoKey = partner?.photos?.firstOrNull(),
                            contentDescription = partner?.fullName ?: "Match",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = partner?.fullName ?: "Match",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (partner?.isVerified == true) VerifiedBadge(modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = when {
                                    isPartnerTyping -> "typing..."
                                    partner?.isOnline == true -> "Online now"
                                    else -> "Active recently"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isPartnerTyping || partner?.isOnline == true) EmeraldOnline else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onStartCall(false) },
                        modifier = Modifier.testTag("chat_audio_call_button")
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = "Audio Call")
                    }
                    IconButton(
                        onClick = { onStartCall(true) },
                        modifier = Modifier.testTag("chat_video_call_button")
                    ) {
                        Icon(Icons.Rounded.Videocam, contentDescription = "Video Call")
                    }
                    Box {
                        IconButton(onClick = { showSafetyMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "Safety Menu")
                        }
                        DropdownMenu(
                            expanded = showSafetyMenu,
                            onDismissRequest = { showSafetyMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Report or Block User") },
                                leadingIcon = { Icon(Icons.Rounded.Shield, contentDescription = null) },
                                onClick = {
                                    showSafetyMenu = false
                                    partner?.let { onReportBlock(it) }
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isMine = msg.senderId == "me"
                    ChatBubble(
                        message = msg,
                        isMine = isMine,
                        formattedTime = timeFormatter.format(Date(msg.timestamp)),
                        onDelete = { onDeleteMessage(msg.id) }
                    )
                }
            }

            // AI Icebreaker / Conversation Starter tray (Never auto-sends; populates input for user approval)
            AnimatedVisibility(visible = showAiTray) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiSampleNoticeBanner()
                    when (aiIcebreakerState) {
                        is AiPanelState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        is AiPanelState.Ready -> {
                            Text(
                                "Tap a suggestion to preview & edit before sending:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            aiIcebreakerState.data.suggestions.forEach { suggestion ->
                                Surface(
                                    onClick = {
                                        messageInput = suggestion
                                        showAiTray = false
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                        is AiPanelState.Error -> Text(aiIcebreakerState.message, color = MaterialTheme.colorScheme.error)
                        AiPanelState.Idle -> Unit
                    }
                }
            }

            // Emoji quick bar
            AnimatedVisibility(visible = showEmojiTray) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listOf("😊", "❤️", "✨", "☕", "😂", "🔥", "🥂", "🎶", "🌹", "👋")) { emoji ->
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.clickable { messageInput += emoji }
                        )
                    }
                }
            }

            // Voice note quick indicator
            AnimatedVisibility(visible = isRecordingVoice) {
                Surface(
                    color = RosePrimary.copy(alpha = 0.16f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = RosePrimary)
                            Text("Voice clip ready (6s)", style = MaterialTheme.typography.labelMedium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { isRecordingVoice = false }) { Text("Cancel") }
                            Button(onClick = {
                                isRecordingVoice = false
                                onSendMessage("Voice note", null, 6)
                            }) { Text("Send Clip") }
                        }
                    }
                }
            }

            // Composer Bar
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            showAiTray = !showAiTray
                            if (showAiTray && partner != null) {
                                onLoadIcebreakers(partner)
                            }
                        },
                        modifier = Modifier.testTag("chat_ai_icebreaker_button")
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = "AI Icebreakers",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    IconButton(onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Icon(Icons.Rounded.Image, contentDescription = "Send Photo")
                    }

                    IconButton(onClick = { showEmojiTray = !showEmojiTray }) {
                        Icon(Icons.Rounded.Mood, contentDescription = "Emoji")
                    }

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Message...") },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input")
                    )

                    if (messageInput.isBlank()) {
                        IconButton(onClick = { isRecordingVoice = !isRecordingVoice }) {
                            Icon(Icons.Rounded.Mic, contentDescription = "Voice Note")
                        }
                    } else {
                        IconButton(
                            onClick = {
                                onSendMessage(messageInput, null, null)
                                messageInput = ""
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(RosePrimary)
                                .testTag("chat_send_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessageEntity,
    isMine: Boolean,
    formattedTime: String,
    onDelete: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }
    var playingVoice by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isMine) 20.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 20.dp
            ),
            color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clickable(enabled = isMine && !message.isDeleted) { showActions = !showActions }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (message.imageUri != null && !message.isDeleted) {
                    ProfilePhoto(
                        photoKey = message.imageUri,
                        contentDescription = "Shared photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
                if (message.voiceDurationSec != null && !message.isDeleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable { playingVoice = !playingVoice }
                    ) {
                        Icon(
                            imageVector = if (playingVoice) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                            contentDescription = "Play voice note",
                            tint = if (isMine) Color.White else MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = if (isMine) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${message.voiceDurationSec}s voice",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = if (message.isDeleted) FontStyle.Italic else FontStyle.Normal,
                        color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMine) Color.White.copy(alpha = 0.78f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isMine && !message.isDeleted) {
                        Icon(
                            imageVector = Icons.Rounded.DoneAll,
                            contentDescription = message.status,
                            tint = if (message.status == "READ") SuperLikeCyan else Color.White.copy(alpha = 0.78f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        if (showActions && isMine && !message.isDeleted) {
            TextButton(onClick = {
                showActions = false
                onDelete()
            }) {
                Text("Delete Message", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
