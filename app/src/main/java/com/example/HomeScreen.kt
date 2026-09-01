package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val installedApps by viewModel.installedApps.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val context = LocalContext.current

    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }

    val dimAmount = viewModel.settingsManager.backgroundDimAmount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Cinematic Background (Static dark gradient/overlay)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimAmount))
            )
        }

        if (showSettings) {
            SettingsScreen(viewModel)
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(viewModel.settingsManager.clockVisible)

                Spacer(modifier = Modifier.weight(1f))

                // Favorites Row
                if (favorites.isNotEmpty()) {
                    Text(
                        text = "Favorites",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 48.dp, bottom = 16.dp)
                    )
                    AppRow(
                        apps = favorites,
                        context = context,
                        viewModel = viewModel,
                        isFavorites = true,
                        onAppSelected = { selectedApp = it },
                        onLongPress = { app ->
                            selectedApp = app
                            showContextMenu = true
                        }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // All Apps Row
                if (installedApps.isNotEmpty()) {
                    Text(
                        text = "Apps",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 48.dp, bottom = 16.dp)
                    )
                    AppRow(
                        apps = installedApps,
                        context = context,
                        viewModel = viewModel,
                        isFavorites = false,
                        onAppSelected = { selectedApp = it },
                        onLongPress = { app ->
                            selectedApp = app
                            showContextMenu = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    if (showContextMenu && selectedApp != null) {
        ContextMenuDialog(
            app = selectedApp!!,
            viewModel = viewModel,
            onDismiss = { showContextMenu = false },
            context = context
        )
    }
}

@Composable
fun TopBar(showClock: Boolean) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val timeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(currentTime))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showClock) {
            Text(
                text = timeString,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun AppRow(
    apps: List<AppInfo>,
    context: Context,
    viewModel: MainViewModel,
    isFavorites: Boolean,
    onAppSelected: (AppInfo) -> Unit,
    onLongPress: (AppInfo) -> Unit
) {
    val showLabels = viewModel.settingsManager.showAppLabels

    LazyRow(
        contentPadding = PaddingValues(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(apps) { _, app ->
            AppCard(
                app = app,
                showLabel = showLabels,
                onClick = {
                    try {
                        if (app.packageName == context.packageName) {
                            viewModel.openSettings()
                        } else {
                            app.launchIntent?.let {
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(it)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                onLongClick = {
                    onLongPress(app)
                },
                onFocused = {
                    onAppSelected(app)
                }
            )
        }

        if (!isFavorites) {
            item {
                SettingsCard(
                    onClick = { viewModel.openSettings() },
                    onFocused = {}
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    app: AppInfo,
    showLabel: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFocused: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1.0f,
        animationSpec = tween(durationMillis = 200), label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(140.dp)
    ) {
        Card(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .onFocusChanged { state ->
                    isFocused = state.isFocused
                    if (state.isFocused) {
                        onFocused()
                    }
                }
                .focusable(true)
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyUp) {
                        if (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter) {
                            onClick()
                            return@onKeyEvent true
                        }
                    }
                    false
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 12.dp else 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isFocused) Color.White.copy(alpha = 0.2f) else Color.DarkGray)
            ) {
                // For TV sticks, it's safer to extract Bitmap from Drawable 
                // Alternatively use Accompanist/Coil but this is lightweight for local apps
                val bitmap = remember(app.icon) { app.icon.toBitmap().asImageBitmap() }
                Image(
                    bitmap = bitmap,
                    contentDescription = app.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val textAlpha by animateFloatAsState(
            targetValue = if (isFocused || showLabel) 1f else 0f,
            animationSpec = tween(durationMillis = 200), label = "alpha"
        )
        Text(
            text = app.name,
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(textAlpha)
        )
    }
}

@Composable
fun SettingsCard(
    onClick: () -> Unit,
    onFocused: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1.0f,
        animationSpec = tween(durationMillis = 200), label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(140.dp)
    ) {
        Card(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .onFocusChanged { state ->
                    isFocused = state.isFocused
                    if (state.isFocused) {
                        onFocused()
                    }
                }
                .focusable(true)
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyUp) {
                        if (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter) {
                            onClick()
                            return@onKeyEvent true
                        }
                    }
                    false
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.DarkGray
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 12.dp else 0.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isFocused) Color.White.copy(alpha = 0.2f) else Color.DarkGray)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val textAlpha by animateFloatAsState(
            targetValue = if (isFocused) 1f else 0.5f,
            animationSpec = tween(durationMillis = 200), label = "alpha"
        )
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(textAlpha)
        )
    }
}
@Composable
fun ContextMenuDialog(
    app: AppInfo,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    context: Context
) {
    val isFavorite = viewModel.favorites.collectAsState().value.any { it.packageName == app.packageName }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                modifier = Modifier
                    .width(300.dp)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = app.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    ContextMenuItem(
                        text = "Open",
                        onClick = {
                            onDismiss()
                            app.launchIntent?.let {
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(it)
                            }
                        }
                    )
                    
                    ContextMenuItem(
                        text = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                        onClick = {
                            onDismiss()
                            viewModel.toggleFavorite(app)
                        }
                    )
                    
                    if (isFavorite) {
                        ContextMenuItem(
                            text = "Move Left",
                            onClick = {
                                viewModel.moveFavoriteLeft(app)
                            }
                        )
                        ContextMenuItem(
                            text = "Move Right",
                            onClick = {
                                viewModel.moveFavoriteRight(app)
                            }
                        )
                    }
                    
                    ContextMenuItem(
                        text = "App Info",
                        onClick = {
                            onDismiss()
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = Uri.parse("package:${app.packageName}")
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ContextMenuItem(text: String, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFocused) Color.White.copy(alpha = 0.2f) else Color.Transparent)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(true)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp && 
                    (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)) {
                    onClick()
                    return@onKeyEvent true
                }
                false
            }
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 16.sp)
    }
}
