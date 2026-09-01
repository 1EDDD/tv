package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.activity.compose.BackHandler

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    var dimAmount by remember { mutableStateOf(viewModel.settingsManager.backgroundDimAmount) }
    var showLabels by remember { mutableStateOf(viewModel.settingsManager.showAppLabels) }
    var showClock by remember { mutableStateOf(viewModel.settingsManager.clockVisible) }

    BackHandler {
        viewModel.closeSettings()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp)
        ) {
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            LazyColumn(modifier = Modifier.width(400.dp)) {
                item {
                    SettingsItem(
                        title = "Background Darkness",
                        value = "${(dimAmount * 100).toInt()}%",
                        onClick = {
                            dimAmount = if (dimAmount >= 0.8f) 0.1f else dimAmount + 0.1f
                            viewModel.settingsManager.backgroundDimAmount = dimAmount
                        }
                    )
                }

                item {
                    SettingsItem(
                        title = "Show App Labels",
                        value = if (showLabels) "ON" else "OFF",
                        onClick = {
                            showLabels = !showLabels
                            viewModel.settingsManager.showAppLabels = showLabels
                        }
                    )
                }

                item {
                    SettingsItem(
                        title = "Show Clock",
                        value = if (showClock) "ON" else "OFF",
                        onClick = {
                            showClock = !showClock
                            viewModel.settingsManager.clockVisible = showClock
                        }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    SettingsItem(
                        title = "Back to Home",
                        value = "",
                        onClick = {
                            viewModel.closeSettings()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItem(title: String, value: String, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
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
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                color = Color.LightGray,
                fontSize = 18.sp
            )
        }
    }
}
