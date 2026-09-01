package com.example

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SettingsTab(val label: String, val icon: ImageVector) {
    GENERAL("General", Icons.Default.Tune),
    APPEARANCE("Appearance", Icons.Default.ColorLens),
    FAVORITES("Favorites", Icons.Default.Star),
    PERFORMANCE("Performance", Icons.Default.Speed),
    ABOUT("About", Icons.Default.Info)
}

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(SettingsTab.GENERAL) }
    val isDefaultHome by viewModel.isDefaultLauncher.collectAsState()

    BackHandler {
        viewModel.navigateTo(Screen.HOME)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CinematicBackground(
            backgroundImageUri = viewModel.settingsManager.backgroundImageUri,
            dimAmount = 0.70f,
            enableParallax = false,
            performanceMode = viewModel.settingsManager.performanceMode
        )

        Row(modifier = Modifier.fillMaxSize().padding(36.dp)) {
            // Left Navigation Sidebar
            Column(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .padding(end = 24.dp)
            ) {
                // Header with Back Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 28.dp)
                ) {
                    TVIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = { viewModel.navigateTo(Screen.HOME) }
                    )
                    Text(
                        text = "Settings",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Category Tab Items
                SettingsTab.values().forEach { tab ->
                    SettingsTabItem(
                        tab = tab,
                        isSelected = selectedTab == tab,
                        onSelect = { selectedTab = tab }
                    )
                }
            }

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.15f))
            )

            // Right Settings Details Pane
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 32.dp)
            ) {
                when (selectedTab) {
                    SettingsTab.GENERAL -> GeneralSettingsPane(viewModel, isDefaultHome)
                    SettingsTab.APPEARANCE -> AppearanceSettingsPane(viewModel)
                    SettingsTab.FAVORITES -> FavoritesSettingsPane(viewModel)
                    SettingsTab.PERFORMANCE -> PerformanceSettingsPane(viewModel)
                    SettingsTab.ABOUT -> AboutSettingsPane()
                }
            }
        }
    }
}

@Composable
fun SettingsTabItem(
    tab: SettingsTab,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isFocused -> Color.White.copy(alpha = 0.30f)
                    isSelected -> Color.White.copy(alpha = 0.14f)
                    else -> Color.Transparent
                }
            )
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) onSelect()
            }
            .focusable(true)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp &&
                    (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)
                ) {
                    onSelect()
                    return@onKeyEvent true
                }
                false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = null,
            tint = if (isFocused || isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = tab.label,
            color = if (isFocused || isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun GeneralSettingsPane(viewModel: MainViewModel, isDefaultHome: Boolean) {
    var showLabels by remember { mutableStateOf(viewModel.settingsManager.showAppLabels) }
    var showClock by remember { mutableStateOf(viewModel.settingsManager.clockVisible) }
    var showDate by remember { mutableStateOf(viewModel.settingsManager.dateVisible) }
    var showRecents by remember { mutableStateOf(viewModel.settingsManager.showRecentApps) }
    var rememberFocus by remember { mutableStateOf(viewModel.settingsManager.rememberLastFocus) }
    var screensaverMins by remember { mutableIntStateOf(viewModel.settingsManager.ambientScreensaverMinutes) }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "General Settings",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Default Launcher
        item {
            SettingsCardAction(
                title = "Default Home Launcher",
                subtitle = if (isDefaultHome) "TV Launcher is currently set as your default Home screen"
                else "Press to open system settings and set as default Home",
                badge = if (isDefaultHome) "DEFAULT" else "NOT SET",
                badgeColor = if (isDefaultHome) Color(0xFF10B981) else Color(0xFFF59E0B),
                onClick = { viewModel.openHomeSettings() }
            )
        }

        // App Labels
        item {
            SettingsCardOption(
                title = "App Labels",
                subtitle = "Control when app titles are displayed under cards",
                value = when (showLabels) {
                    "focused_only" -> "Focused Only"
                    "always" -> "Always Visible"
                    else -> "Hidden"
                },
                onClick = {
                    showLabels = when (showLabels) {
                        "focused_only" -> "always"
                        "always" -> "never"
                        else -> "focused_only"
                    }
                    viewModel.settingsManager.showAppLabels = showLabels
                }
            )
        }

        // Show Clock
        item {
            SettingsCardToggle(
                title = "Show Clock",
                subtitle = "Display current time on the top status bar",
                checked = showClock,
                onToggle = {
                    showClock = !showClock
                    viewModel.settingsManager.clockVisible = showClock
                }
            )
        }

        // Show Date
        item {
            SettingsCardToggle(
                title = "Show Date",
                subtitle = "Display current date next to the clock",
                checked = showDate,
                onToggle = {
                    showDate = !showDate
                    viewModel.settingsManager.dateVisible = showDate
                }
            )
        }

        // Show Recently Used Apps
        item {
            SettingsCardToggle(
                title = "Recently Used Apps Row",
                subtitle = "Display quick access row of recent applications",
                checked = showRecents,
                onToggle = {
                    showRecents = !showRecents
                    viewModel.settingsManager.showRecentApps = showRecents
                }
            )
        }

        // Remember Last Focus
        item {
            SettingsCardToggle(
                title = "Remember Last Focus Position",
                subtitle = "Highlight last used app when returning to home",
                checked = rememberFocus,
                onToggle = {
                    rememberFocus = !rememberFocus
                    viewModel.settingsManager.rememberLastFocus = rememberFocus
                }
            )
        }

        // Screensaver Timer
        item {
            SettingsCardOption(
                title = "Ambient Screensaver",
                subtitle = "Enter subtle clock screensaver when idle",
                value = if (screensaverMins == 0) "Disabled" else "$screensaverMins Minutes",
                onClick = {
                    screensaverMins = when (screensaverMins) {
                        0 -> 5
                        5 -> 10
                        10 -> 15
                        else -> 0
                    }
                    viewModel.settingsManager.ambientScreensaverMinutes = screensaverMins
                }
            )
        }
    }
}

@Composable
fun AppearanceSettingsPane(viewModel: MainViewModel) {
    var backgroundImageUri by remember { mutableStateOf(viewModel.settingsManager.backgroundImageUri) }
    var dimAmount by remember { mutableStateOf(viewModel.settingsManager.backgroundDimAmount) }
    var enableParallax by remember { mutableStateOf(viewModel.settingsManager.enableParallax) }
    var cardScale by remember { mutableStateOf(viewModel.settingsManager.cardScale) }
    var animSpeed by remember { mutableStateOf(viewModel.settingsManager.animationSpeed) }

    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val uriString = uri.toString()
            backgroundImageUri = uriString
            viewModel.settingsManager.backgroundImageUri = uriString
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Appearance & Wallpapers",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Custom Background Image
        item {
            SettingsCardOption(
                title = "Background Image",
                subtitle = if (backgroundImageUri != null) "Custom image selected" else "OLED Black",
                value = if (backgroundImageUri != null) "Change" else "Select",
                onClick = {
                    imagePickerLauncher.launch("image/*")
                }
            )
        }

        if (backgroundImageUri != null) {
            item {
                SettingsCardAction(
                    title = "Remove Background",
                    subtitle = "Reset to pure OLED black",
                    badge = "RESET",
                    badgeColor = Color(0xFFF87171),
                    onClick = {
                        backgroundImageUri = null
                        viewModel.settingsManager.backgroundImageUri = null
                    }
                )
            }
        }

        // Background Darkness
        item {
            SettingsCardOption(
                title = "Background Darkness Overlay",
                subtitle = "Controls background contrast for icon legibility",
                value = "${(dimAmount * 100).toInt()}%",
                onClick = {
                    dimAmount = if (dimAmount >= 0.70f) 0.15f else dimAmount + 0.15f
                    viewModel.settingsManager.backgroundDimAmount = dimAmount
                }
            )
        }

        // Parallax Effect
        item {
            SettingsCardToggle(
                title = "Subtle Parallax Motion",
                subtitle = "Background shifts subtly when navigating apps",
                checked = enableParallax,
                onToggle = {
                    enableParallax = !enableParallax
                    viewModel.settingsManager.enableParallax = enableParallax
                }
            )
        }

        // Card Focus Scale
        item {
            SettingsCardOption(
                title = "Focused Card Scale",
                subtitle = "Magnification amount when an app card is highlighted",
                value = when {
                    cardScale < 1.12f -> "Subtle (1.10x)"
                    cardScale < 1.18f -> "Standard (1.15x)"
                    else -> "Large (1.20x)"
                },
                onClick = {
                    cardScale = when {
                        cardScale < 1.12f -> 1.15f
                        cardScale < 1.18f -> 1.20f
                        else -> 1.10f
                    }
                    viewModel.settingsManager.cardScale = cardScale
                }
            )
        }

        // Animation Speed
        item {
            SettingsCardOption(
                title = "Animation Speed",
                subtitle = "Transition fluidity for card selection",
                value = when (animSpeed) {
                    "fast" -> "Fast (100ms)"
                    "off" -> "Instant (No Animation)"
                    else -> "Cinematic Smooth (180ms)"
                },
                onClick = {
                    animSpeed = when (animSpeed) {
                        "smooth" -> "fast"
                        "fast" -> "off"
                        else -> "smooth"
                    }
                    viewModel.settingsManager.animationSpeed = animSpeed
                }
            )
        }
    }
}

// PresetCard removed

@Composable
fun FavoritesSettingsPane(viewModel: MainViewModel) {
    val installedApps by viewModel.installedApps.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Favorites Manager",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${favorites.size} apps pinned to main shelf",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = { viewModel.navigateTo(Screen.REORDER_FAVORITES) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Reorder Shelf", color = Color.White, fontSize = 14.sp)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(installedApps, key = { it.packageName }) { app ->
                val isFav = favorites.any { it.packageName == app.packageName }
                SettingsCardToggle(
                    title = app.name,
                    subtitle = app.category,
                    checked = isFav,
                    onToggle = {
                        viewModel.toggleFavorite(app)
                    }
                )
            }
        }
    }
}

@Composable
fun PerformanceSettingsPane(viewModel: MainViewModel) {
    var perfMode by remember { mutableStateOf(viewModel.settingsManager.performanceMode) }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Performance & Low-RAM Mode",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            SettingsCardToggle(
                title = "Performance Mode (TV Stick Optimization)",
                subtitle = "Disables heavy ambient canvas pulses and simplifies animations for smooth 60fps on older 1GB RAM sticks",
                checked = perfMode,
                onToggle = {
                    perfMode = !perfMode
                    viewModel.settingsManager.performanceMode = perfMode
                }
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Built-in Optimizations Active:",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• In-memory LRU icon caching prevents repeated PackageManager queries\n" +
                               "• Pure Kotlin Compose runtime without heavy background services\n" +
                               "• 100% Offline capability with zero cloud tracking or telemetry\n" +
                               "• Minimal garbage collection overhead via stateless lazy rows",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AboutSettingsPane() {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "About TV Launcher",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TV Launcher v1.0.0",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "A dedicated cinematic Android TV Home Launcher inspired by Apple TV. Engineered for smooth performance, landscape 16:9 displays, and standard TV remote D-Pad navigation.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "Remote Shortcuts",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "• OK / Enter: Launch app or select item", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(text = "• Long Press OK / Menu: Open App Context Menu (Favorites, Info, Move)", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(text = "• D-Pad Left / Right: Move horizontally through apps", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(text = "• D-Pad Up / Down: Move between Top Bar and App Shelves", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(text = "• Back: Return to previous screen or close menu", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun SettingsCardOption(
    title: String,
    subtitle: String,
    value: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(true)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp &&
                    (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)
                ) {
                    onClick()
                    return@onKeyEvent true
                }
                false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SettingsCardToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(true)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp &&
                    (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)
                ) {
                    onToggle()
                    return@onKeyEvent true
                }
                false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF38BDF8),
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}

@Composable
fun SettingsCardAction(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(true)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp &&
                    (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)
                ) {
                    onClick()
                    return@onKeyEvent true
                }
                false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor.copy(alpha = 0.25f))
                .border(1.dp, badgeColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = badge,
                color = badgeColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
