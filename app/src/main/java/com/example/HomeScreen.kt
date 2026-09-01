package com.example

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val recentApps by viewModel.recentApps.collectAsState()
    val selectedApp by viewModel.selectedApp.collectAsState()
    val isDefaultHome by viewModel.isDefaultLauncher.collectAsState()
    val parallaxOffset by viewModel.parallaxOffset.collectAsState()
    val context = LocalContext.current

    var contextMenuApp by remember { mutableStateOf<AppInfo?>(null) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Screen Router
    when (currentScreen) {
        Screen.SETTINGS -> {
            SettingsScreen(viewModel)
            return
        }
        Screen.SEARCH -> {
            SearchScreen(viewModel)
            return
        }
        Screen.REORDER_FAVORITES -> {
            ReorderFavoritesScreen(viewModel)
            return
        }
        Screen.AMBIENT_SCREENSAVER -> {
            AmbientScreensaver(viewModel)
            return
        }
        Screen.HOME -> { /* render home below */ }
    }

    // Screensaver idle detection
    val screensaverMinutes = viewModel.settingsManager.ambientScreensaverMinutes
    if (screensaverMinutes > 0) {
        LaunchedEffect(lastInteractionTime) {
            while (true) {
                delay(15000)
                val idleTime = System.currentTimeMillis() - lastInteractionTime
                if (idleTime > screensaverMinutes * 60 * 1000L) {
                    viewModel.navigateTo(Screen.AMBIENT_SCREENSAVER)
                    break
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onKeyEvent {
                lastInteractionTime = System.currentTimeMillis()
                false
            }
    ) {
        // Dynamic Cinematic Background
        CinematicBackground(
            presetId = viewModel.settingsManager.backgroundPreset,
            dimAmount = viewModel.settingsManager.backgroundDimAmount,
            enableParallax = viewModel.settingsManager.enableParallax,
            performanceMode = viewModel.settingsManager.performanceMode,
            parallaxOffsetX = parallaxOffset
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Status & Navigation Bar
            TopStatusBar(
                viewModel = viewModel,
                isDefaultHome = isDefaultHome,
                onUserAction = { lastInteractionTime = System.currentTimeMillis() }
            )

            // 2. Apple TV Hero Focused App Preview Area
            HeroFocusedAppSection(selectedApp = selectedApp)

            Spacer(modifier = Modifier.weight(1f))

            // 3. App Shelves (Favorites, Recents, All Apps)
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Shelf 1: Favorites
                if (favorites.isNotEmpty()) {
                    item {
                        ShelfSection(
                            title = "Favorites",
                            apps = favorites,
                            viewModel = viewModel,
                            isFavorites = true,
                            onAppFocused = { app, offset ->
                                viewModel.setSelectedApp(app, offset)
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            onAppLongPress = { app ->
                                contextMenuApp = app
                            }
                        )
                    }
                }

                // Shelf 2: Recently Used (Optional)
                if (viewModel.settingsManager.showRecentApps && recentApps.isNotEmpty()) {
                    item {
                        ShelfSection(
                            title = "Recently Used",
                            apps = recentApps,
                            viewModel = viewModel,
                            isFavorites = false,
                            isCompact = true,
                            onAppFocused = { app, offset ->
                                viewModel.setSelectedApp(app, offset)
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            onAppLongPress = { app ->
                                contextMenuApp = app
                            }
                        )
                    }
                }

                // Shelf 3: All Applications
                if (installedApps.isNotEmpty()) {
                    item {
                        ShelfSection(
                            title = "All Applications",
                            apps = installedApps,
                            viewModel = viewModel,
                            isFavorites = false,
                            includeSystemTiles = true,
                            onAppFocused = { app, offset ->
                                viewModel.setSelectedApp(app, offset)
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            onAppLongPress = { app ->
                                contextMenuApp = app
                            }
                        )
                    }
                }
            }
        }

        // Long Press Context Menu Dialog
        if (contextMenuApp != null) {
            AppleTVContextMenuDialog(
                app = contextMenuApp!!,
                viewModel = viewModel,
                onDismiss = { contextMenuApp = null },
                context = context
            )
        }
    }
}

@Composable
fun TopStatusBar(
    viewModel: MainViewModel,
    isDefaultHome: Boolean,
    onUserAction: () -> Unit
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val timeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(currentTime))
    val dateString = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(currentTime))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Quick Action Icons (Search, Reorder, Settings)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TVTopBarButton(
                icon = Icons.Default.Search,
                label = "Search",
                onClick = {
                    onUserAction()
                    viewModel.navigateTo(Screen.SEARCH)
                }
            )

            TVTopBarButton(
                icon = Icons.Default.SwapHoriz,
                label = "Reorder",
                onClick = {
                    onUserAction()
                    viewModel.navigateTo(Screen.REORDER_FAVORITES)
                }
            )

            TVTopBarButton(
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = {
                    onUserAction()
                    viewModel.navigateTo(Screen.SETTINGS)
                }
            )

            if (!isDefaultHome) {
                SetDefaultLauncherPill(onClick = {
                    onUserAction()
                    viewModel.openHomeSettings()
                })
            }
        }

        // Right: Clock & Network Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Wi-Fi Connected",
                tint = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.size(20.dp)
            )

            if (viewModel.settingsManager.dateVisible) {
                Text(
                    text = dateString,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light
                )
            }

            if (viewModel.settingsManager.clockVisible) {
                Text(
                    text = timeString,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TVTopBarButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f))
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
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isFocused) Color.White else Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            color = if (isFocused) Color.White else Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun SetDefaultLauncherPill(onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) Color(0xFFF59E0B).copy(alpha = 0.45f) else Color(0xFFF59E0B).copy(alpha = 0.2f))
            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp))
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
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = "Set as Default Home",
            color = Color(0xFFFDE68A),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HeroFocusedAppSection(selectedApp: AppInfo?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 52.dp, vertical = 12.dp)
            .height(56.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (selectedApp != null) {
            Column {
                Text(
                    text = selectedApp.name,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = selectedApp.category.uppercase(),
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun ShelfSection(
    title: String,
    apps: List<AppInfo>,
    viewModel: MainViewModel,
    isFavorites: Boolean,
    isCompact: Boolean = false,
    includeSystemTiles: Boolean = false,
    onAppFocused: (AppInfo, Float) -> Unit,
    onAppLongPress: (AppInfo) -> Unit
) {
    val showLabelsSetting = viewModel.settingsManager.showAppLabels
    val cardScaleTarget = viewModel.settingsManager.cardScale
    val animDuration = when (viewModel.settingsManager.animationSpeed) {
        "fast" -> 100
        "off" -> 0
        else -> 180
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = if (isFavorites) 19.sp else 16.sp,
            fontWeight = if (isFavorites) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(start = 52.dp, bottom = 12.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(apps, key = { _, app -> "${title}_${app.packageName}" }) { index, app ->
                AppCardItem(
                    app = app,
                    isCompact = isCompact,
                    showLabelsSetting = showLabelsSetting,
                    cardScaleTarget = cardScaleTarget,
                    animDuration = animDuration,
                    onClick = {
                        viewModel.launchApp(app)
                    },
                    onLongPress = {
                        onAppLongPress(app)
                    },
                    onFocused = {
                        onAppFocused(app, index.toFloat())
                    }
                )
            }

            if (includeSystemTiles) {
                item {
                    SystemTileItem(
                        icon = Icons.Default.Search,
                        label = "Search",
                        isCompact = isCompact,
                        onClick = { viewModel.navigateTo(Screen.SEARCH) }
                    )
                }
                item {
                    SystemTileItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        isCompact = isCompact,
                        onClick = { viewModel.navigateTo(Screen.SETTINGS) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppCardItem(
    app: AppInfo,
    isCompact: Boolean,
    showLabelsSetting: String,
    cardScaleTarget: Float,
    animDuration: Int,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onFocused: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) cardScaleTarget else 1.0f,
        animationSpec = tween(durationMillis = animDuration),
        label = "app_card_scale"
    )

    val cardWidth = if (isCompact) 120.dp else 145.dp
    val cardHeight = if (isCompact) 120.dp else 145.dp
    val iconSize = if (isCompact) 68.dp else 84.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(cardWidth)
            .scale(scale)
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 16.dp else 2.dp),
            modifier = Modifier
                .size(cardWidth, cardHeight)
                .border(
                    width = if (isFocused) 2.5.dp else 1.dp,
                    color = if (isFocused) Color.White.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(22.dp)
                )
                .onFocusChanged { state ->
                    isFocused = state.isFocused
                    if (state.isFocused) {
                        onFocused()
                    }
                }
                .focusable(true)
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyUp) {
                        when (keyEvent.key) {
                            Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                                onClick()
                                return@onKeyEvent true
                            }
                            Key.Menu -> {
                                onLongPress()
                                return@onKeyEvent true
                            }
                        }
                    }
                    if (keyEvent.type == KeyEventType.KeyDown &&
                        (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)
                    ) {
                        if (keyEvent.nativeKeyEvent.isLongPress) {
                            onLongPress()
                            return@onKeyEvent true
                        }
                    }
                    false
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isFocused) listOf(
                                Color.White.copy(alpha = 0.30f),
                                Color.White.copy(alpha = 0.15f)
                            ) else listOf(
                                Color.White.copy(alpha = 0.10f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = remember(app.icon) {
                    try { app.icon.toBitmap(160, 160).asImageBitmap() }
                    catch (e: Exception) { app.icon.toBitmap().asImageBitmap() }
                }
                Image(
                    bitmap = bitmap,
                    contentDescription = app.name,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }

        // App Label
        val shouldShowLabel = when (showLabelsSetting) {
            "always" -> true
            "never" -> false
            else -> isFocused // "focused_only"
        }

        Spacer(modifier = Modifier.height(8.dp))

        val labelAlpha by animateFloatAsState(
            targetValue = if (shouldShowLabel) 1f else 0f,
            animationSpec = tween(durationMillis = animDuration),
            label = "label_alpha"
        )

        Text(
            text = app.name,
            color = if (isFocused) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(labelAlpha)
        )
    }
}

@Composable
fun SystemTileItem(
    icon: ImageVector,
    label: String,
    isCompact: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val cardWidth = if (isCompact) 120.dp else 145.dp
    val cardHeight = if (isCompact) 120.dp else 145.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(cardWidth)
            .scale(if (isFocused) 1.15f else 1.0f)
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
                .size(cardWidth, cardHeight)
                .border(
                    width = if (isFocused) 2.5.dp else 1.dp,
                    color = if (isFocused) Color.White else Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(22.dp)
                )
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
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isFocused) Color.White.copy(alpha = 0.25f)
                        else Color.White.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = if (isFocused) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AppleTVContextMenuDialog(
    app: AppInfo,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    context: Context
) {
    val favorites by viewModel.favorites.collectAsState()
    val isFavorite = favorites.any { it.packageName == app.packageName }
    val favIndex = favorites.indexOfFirst { it.packageName == app.packageName }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
                modifier = Modifier
                    .width(360.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // App Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        val bitmap = remember(app.icon) {
                            try { app.icon.toBitmap(96, 96).asImageBitmap() }
                            catch (e: Exception) { app.icon.toBitmap().asImageBitmap() }
                        }
                        Image(
                            bitmap = bitmap,
                            contentDescription = app.name,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Column {
                            Text(
                                text = app.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = app.category,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Open App
                    ContextMenuItemRow(
                        icon = Icons.Default.PlayArrow,
                        title = "Open App",
                        onClick = {
                            onDismiss()
                            viewModel.launchApp(app)
                        }
                    )

                    // Toggle Favorite
                    ContextMenuItemRow(
                        icon = if (isFavorite) Icons.Default.StarBorder else Icons.Default.Star,
                        title = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                        onClick = {
                            onDismiss()
                            viewModel.toggleFavorite(app)
                        }
                    )

                    // Reorder Shelf (if in favorites)
                    if (isFavorite) {
                        ContextMenuItemRow(
                            icon = Icons.Default.SwapHoriz,
                            title = "Reorder in Favorites Shelf",
                            onClick = {
                                onDismiss()
                                viewModel.navigateTo(Screen.REORDER_FAVORITES)
                            }
                        )
                    }

                    // App Info
                    ContextMenuItemRow(
                        icon = Icons.Default.Info,
                        title = "App Info & Details",
                        onClick = {
                            onDismiss()
                            viewModel.appManager.openAppInfo(app.packageName)
                        }
                    )

                    // Uninstall App
                    ContextMenuItemRow(
                        icon = Icons.Default.Delete,
                        title = "Uninstall Application",
                        titleColor = Color(0xFFF87171),
                        onClick = {
                            onDismiss()
                            viewModel.appManager.uninstallApp(app.packageName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ContextMenuItemRow(
    icon: ImageVector,
    title: String,
    titleColor: Color = Color.White,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) Color.White.copy(alpha = 0.25f) else Color.Transparent)
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
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isFocused) Color.White else titleColor.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            color = if (isFocused) Color.White else titleColor,
            fontSize = 15.sp,
            fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
