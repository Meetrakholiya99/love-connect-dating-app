package com.example.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProfileEntity
import com.example.data.repository.AiBioSuggestion
import com.example.ui.components.*
import com.example.ui.viewmodel.AiPanelState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    existingProfile: UserProfileEntity?,
    aiBioState: AiPanelState<AiBioSuggestion>,
    isEditMode: Boolean = false,
    onGenerateAiBio: (name: String, profession: String, hobbies: List<String>, goal: String) -> Unit,
    onSaveProfile: (UserProfileEntity) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var fullName by remember(existingProfile) { mutableStateOf(existingProfile?.fullName ?: "") }
    var gender by remember(existingProfile) { mutableStateOf(existingProfile?.gender ?: "Woman") }
    var interestedIn by remember(existingProfile) { mutableStateOf(existingProfile?.interestedIn ?: "Everyone") }
    var city by remember(existingProfile) { mutableStateOf(existingProfile?.city ?: "San Francisco") }
    var education by remember(existingProfile) { mutableStateOf(existingProfile?.education ?: "") }
    var profession by remember(existingProfile) { mutableStateOf(existingProfile?.profession ?: "") }
    var heightText by remember(existingProfile) { mutableStateOf((existingProfile?.heightCm ?: 172).toString()) }
    var relationshipGoal by remember(existingProfile) { mutableStateOf(existingProfile?.relationshipGoal ?: "Long-term partner") }
    var bio by remember(existingProfile) { mutableStateOf(existingProfile?.bio ?: "") }

    val availableInterests = listOf(
        "Travel", "Coffee", "Art Galleries", "Indie Music", "Cooking",
        "Photography", "Hiking", "Cinema", "Yoga", "Tech", "Design", "Foodie"
    )
    var selectedInterests by remember(existingProfile) {
        mutableStateOf(
            existingProfile?.interests?.ifEmpty { listOf("Coffee", "Travel", "Indie Music") }
                ?: listOf("Coffee", "Travel", "Indie Music")
        )
    }

    val availableLanguages = listOf("English", "Spanish", "French", "Hindi", "Japanese", "Italian")
    var selectedLanguages by remember(existingProfile) {
        mutableStateOf(
            existingProfile?.languages?.ifEmpty { listOf("English") } ?: listOf("English")
        )
    }

    LaunchedEffect(aiBioState) {
        if (aiBioState is AiPanelState.Ready && bio.isBlank()) {
            bio = aiBioState.data.bioText
        }
    }

    Scaffold(
        topBar = {
            LoveConnectTopBar(
                title = if (isEditMode) "Edit Profile" else "Set Up Profile",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Text("I identify as", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Woman", "Man", "Non-binary").forEach { option ->
                        InterestPill(
                            label = option,
                            selected = gender == option,
                            onClick = { gender = option }
                        )
                    }
                }

                Text("Interested in", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Women", "Men", "Everyone").forEach { option ->
                        InterestPill(
                            label = option,
                            selected = interestedIn == option,
                            onClick = { interestedIn = option }
                        )
                    }
                }

                Text("Relationship Goal", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Long-term partner",
                        "Long-term, open to short",
                        "New friends",
                        "Figuring it out"
                    ).forEach { goal ->
                        InterestPill(
                            label = goal,
                            selected = relationshipGoal == goal,
                            onClick = { relationshipGoal = goal }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Height (cm)") },
                        singleLine = true,
                        modifier = Modifier.width(120.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                OutlinedTextField(
                    value = profession,
                    onValueChange = { profession = it },
                    label = { Text("Profession") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = education,
                    onValueChange = { education = it },
                    label = { Text("Education") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Text("Hobbies & Interests (Pick up to 6)", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableInterests.forEach { tag ->
                        val isSelected = tag in selectedInterests
                        InterestPill(
                            label = tag,
                            selected = isSelected,
                            onClick = {
                                selectedInterests = if (isSelected) {
                                    selectedInterests - tag
                                } else if (selectedInterests.size < 6) {
                                    selectedInterests + tag
                                } else {
                                    selectedInterests
                                }
                            }
                        )
                    }
                }

                Text("Languages", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableLanguages.forEach { lang ->
                        val isSelected = lang in selectedLanguages
                        InterestPill(
                            label = lang,
                            selected = isSelected,
                            onClick = {
                                selectedLanguages = if (isSelected && selectedLanguages.size > 1) {
                                    selectedLanguages - lang
                                } else if (!isSelected) {
                                    selectedLanguages + lang
                                } else {
                                    selectedLanguages
                                }
                            }
                        )
                    }
                }

                // AI Bio Generator section
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("About Me & AI Bio", style = MaterialTheme.typography.titleMedium)
                            FilledTonalButton(
                                onClick = {
                                    onGenerateAiBio(
                                        fullName.ifBlank { "Alex" },
                                        profession,
                                        selectedInterests,
                                        relationshipGoal
                                    )
                                },
                                modifier = Modifier.testTag("ai_generate_bio_button")
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("AI Suggest")
                            }
                        }

                        AiSampleNoticeBanner()

                        if (aiBioState is AiPanelState.Loading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        } else if (aiBioState is AiPanelState.Ready) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = aiBioState.data.styleTag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = aiBioState.data.bioText,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    TextButton(onClick = { bio = aiBioState.data.bioText }) {
                                        Text("Use This Bio")
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { if (it.length <= 320) bio = it },
                            label = { Text("Your Bio") },
                            placeholder = { Text("Share what lights you up...") },
                            minLines = 3,
                            maxLines = 5,
                            supportingText = { Text("${bio.length}/320") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_bio_input"),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                BrandGradientButton(
                    text = if (isEditMode) "Save Profile Changes" else "Next: Upload Photos",
                    enabled = fullName.isNotBlank(),
                    onClick = {
                        val base = existingProfile ?: UserProfileEntity(
                            id = "me",
                            isCurrentUser = true,
                            fullName = fullName.trim(),
                            dateOfBirth = "1999-05-14",
                            age = 27,
                            gender = gender,
                            interestedIn = interestedIn,
                            bio = bio.trim(),
                            city = city.trim(),
                            distanceKm = 0,
                            education = education.trim(),
                            profession = profession.trim(),
                            hobbiesCsv = selectedInterests.joinToString("|"),
                            interestsCsv = selectedInterests.joinToString("|"),
                            relationshipGoal = relationshipGoal,
                            languagesCsv = selectedLanguages.joinToString("|"),
                            heightCm = heightText.toIntOrNull() ?: 172,
                            photosCsv = "drawable:sophia"
                        )
                        onSaveProfile(
                            base.copy(
                                fullName = fullName.trim(),
                                gender = gender,
                                interestedIn = interestedIn,
                                city = city.trim().ifBlank { "San Francisco" },
                                education = education.trim(),
                                profession = profession.trim(),
                                heightCm = (heightText.toIntOrNull() ?: 172).coerceIn(130, 230),
                                relationshipGoal = relationshipGoal,
                                hobbiesCsv = selectedInterests.joinToString("|"),
                                interestsCsv = selectedInterests.joinToString("|"),
                                languagesCsv = selectedLanguages.joinToString("|"),
                                bio = bio.trim().ifBlank {
                                    "Exploring ${city.ifBlank { "the city" }}, loving ${selectedInterests.take(2).joinToString(" & ")}."
                                }
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "save_profile_button"
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
