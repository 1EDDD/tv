package com.example

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.core.graphics.drawable.toBitmap

@Composable
fun SearchScreen(viewModel: MainViewModel) {
    val query by viewModel.searchQuery.collectAsState()
    val allApps by viewModel.visibleApps.collectAsState()
    val context = LocalContext.current

    val filteredApps = remember(query, allApps) {
        if (query.isBlank()) allApps
        else allApps.filter { it.name.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }
    }

    BackHandler {
        viewModel.setSearchQuery("")
        viewModel.navigateTo(Screen.HOME)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CinematicBackground(
            backgroundImageUri = viewModel.settingsManager.backgroundImageUri,
            dimAmount = 0.55f,
            enableParallax = false,
            performanceMode = viewModel.settingsManager.performanceMode
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Left Column: Virtual TV Keyboard & Search Bar
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxSize()
            ) {
                // Header with Back Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    TVIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = {
                            viewModel.setSearchQuery("")
                            viewModel.navigateTo(Screen.HOME)
                        }
                    )
                    Text(
                        text = "Search Apps",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Query Display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (query.isEmpty()) "Type to filter..." else query,
                            color = if (query.isEmpty()) Color.White.copy(alpha = 0.4f) else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (query.isNotEmpty()) {
                            TVIconButton(
                                icon = Icons.Default.Clear,
                                contentDescription = "Clear",
                                size = 32.dp,
                                onClick = { viewModel.setSearchQuery("") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // On-screen D-Pad Keyboard
                TVVirtualKeyboard(
                    onKeyPress = { char -> viewModel.setSearchQuery(query + char) },
                    onBackspace = {
                        if (query.isNotEmpty()) {
                            viewModel.setSearchQuery(query.dropLast(1))
                        }
                    },
                    onSpace = { viewModel.setSearchQuery("$query ") },
                    onClear = { viewModel.setSearchQuery("") }
                )
            }

            // Right Column: Search Results Grid
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                Text(
                    text = "${filteredApps.size} Applications",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching apps found for \"$query\"",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 18.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 130.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            SearchAppCard(
                                app = app,
                                onClick = {
                                    viewModel.launchApp(app)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TVVirtualKeyboard(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onClear: () -> Unit
) {
    val keyboardRows = listOf(
        listOf("A", "B", "C", "D", "E", "F"),
        listOf("G", "H", "I", "J", "K", "L"),
        listOf("M", "N", "O", "P", "Q", "R"),
        listOf("S", "T", "U", "V", "W", "X"),
        listOf("Y", "Z", "1", "2", "3", "4"),
        listOf("5", "6", "7", "8", "9", "0")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        keyboardRows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { char ->
                    TVKeyButton(
                        text = char,
                        modifier = Modifier.weight(1f),
                        onClick = { onKeyPress(char) }
                    )
                }
            }
        }

        // Special actions row (Space, Backspace, Clear)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            TVKeyButton(
                text = "SPACE",
                modifier = Modifier.weight(2f),
                onClick = onSpace
            )
            TVKeyButton(
                icon = Icons.Default.Backspace,
                modifier = Modifier.weight(1f),
                onClick = onBackspace
            )
            TVKeyButton(
                text = "CLEAR",
                modifier = Modifier.weight(1.2f),
                onClick = onClear
            )
        }
    }
}

@Composable
fun TVKeyButton(
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isFocused) Color.White.copy(alpha = 0.35f)
                else Color.White.copy(alpha = 0.08f)
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
            ),
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                color = if (isFocused) Color.White else Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) Color.White else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun TVIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    size: androidx.compose.ui.unit.Dp = 44.dp,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isFocused) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f))
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
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

@Composable
fun SearchAppCard(
    app: AppInfo,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.12f else 1.0f,
        animationSpec = tween(durationMillis = 150), label = "search_card_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(130.dp)
            .scale(scale)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 10.dp else 0.dp),
            modifier = Modifier
                .size(110.dp)
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
                val bitmap = remember(app.icon) {
                    try { app.icon.toBitmap(128, 128).asImageBitmap() }
                    catch (e: Exception) { app.icon.toBitmap().asImageBitmap() }
                }
                Image(
                    bitmap = bitmap,
                    contentDescription = app.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = app.name,
            color = if (isFocused) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
