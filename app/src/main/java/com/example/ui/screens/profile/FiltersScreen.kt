package com.example.ui.screens.profile

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.local.DiscoveryFilterEntity
import com.example.ui.components.BrandGradientButton
import com.example.ui.components.GlassCard
import com.example.ui.components.InterestPill
import com.example.ui.components.LoveConnectTopBar
import com.example.ui.theme.ChampagneGold
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltersScreen(
    currentFilter: DiscoveryFilterEntity,
    isPremium: Boolean,
    onApplyFilters: (DiscoveryFilterEntity) -> Unit,
    onOpenPremium: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var minAge by remember(currentFilter) { mutableFloatStateOf(currentFilter.minAge.toFloat()) }
    var maxAge by remember(currentFilter) { mutableFloatStateOf(currentFilter.maxAge.toFloat()) }
    var maxDistance by remember(currentFilter) { mutableFloatStateOf(currentFilter.maxDistanceKm.toFloat()) }
    var genderPref by remember(currentFilter) { mutableStateOf(currentFilter.genderPreference) }
    var goalFilter by remember(currentFilter) { mutableStateOf(currentFilter.relationshipGoalFilter) }
    var verifiedOnly by remember(currentFilter) { mutableStateOf(currentFilter.verifiedOnly) }
    var cityFilter by remember(currentFilter) { mutableStateOf(currentFilter.cityFilter) }

    var locationStatus by remember {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        mutableStateOf(if (granted) "Approximate location permission granted" else "City-based discovery active")
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationStatus = if (granted) {
            "Approximate location enabled (San Francisco Bay Area)"
        } else {
            "Location permission declined — using manual city filter"
        }
    }

    Scaffold(
        topBar = { LoveConnectTopBar(title = "Discovery Filters", onBack = onBack) }
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Location & Radius", style = MaterialTheme.typography.titleMedium)
                        Text("${maxDistance.roundToInt()} km", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    }

                    Slider(
                        value = maxDistance,
                        onValueChange = { maxDistance = it },
                        valueRange = 5f..150f
                    )

                    OutlinedTextField(
                        value = cityFilter,
                        onValueChange = { cityFilter = it },
                        label = { Text("City Filter (Optional)") },
                        placeholder = { Text("e.g. San Francisco, Oakland") },
                        leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedButton(
                        onClick = { locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Rounded.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Enable Approximate Location")
                    }

                    Text(locationStatus, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = stringResource(R.string.distance_privacy_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Age Preference (18+)", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${minAge.roundToInt()} - ${maxAge.roundToInt()}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    RangeSlider(
                        value = minAge..maxAge,
                        onValueChange = { range ->
                            minAge = range.start.coerceAtLeast(18f)
                            maxAge = range.endInclusive.coerceAtLeast(minAge)
                        },
                        valueRange = 18f..65f
                    )

                    Text("Show Me", style = MaterialTheme.typography.titleSmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Everyone", "Women", "Men").forEach { option ->
                            InterestPill(
                                label = option,
                                selected = genderPref == option,
                                onClick = { genderPref = option }
                            )
                        }
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Advanced Filters", style = MaterialTheme.typography.titleMedium)
                        if (!isPremium) {
                            TextButton(onClick = onOpenPremium) {
                                Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Premium", color = ChampagneGold)
                            }
                        }
                    }

                    Text("Relationship Goal", style = MaterialTheme.typography.titleSmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Any", "Long-term partner", "New friends", "Figuring it out").forEach { goal ->
                            InterestPill(
                                label = goal,
                                selected = goalFilter == goal,
                                onClick = {
                                    if (isPremium || goal == "Any") {
                                        goalFilter = goal
                                    } else {
                                        onOpenPremium()
                                    }
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Verified Profiles Only", style = MaterialTheme.typography.titleSmall)
                            Text("Show only identity-verified members", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = verifiedOnly,
                            onCheckedChange = {
                                if (isPremium) verifiedOnly = it else onOpenPremium()
                            }
                        )
                    }
                }
            }

            BrandGradientButton(
                text = "Apply Discovery Filters",
                onClick = {
                    onApplyFilters(
                        DiscoveryFilterEntity(
                            minAge = minAge.roundToInt().coerceAtLeast(18),
                            maxAge = maxAge.roundToInt().coerceAtLeast(minAge.roundToInt()),
                            maxDistanceKm = maxDistance.roundToInt(),
                            genderPreference = genderPref,
                            relationshipGoalFilter = goalFilter,
                            verifiedOnly = verifiedOnly,
                            cityFilter = cityFilter.trim()
                        )
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
