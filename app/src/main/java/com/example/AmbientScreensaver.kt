package com.example

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AmbientScreensaver(viewModel: MainViewModel) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    BackHandler {
        viewModel.navigateTo(Screen.HOME)
    }

    val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault()).format(Date(currentTime))
    val amPmFormat = SimpleDateFormat("a", Locale.getDefault()).format(Date(currentTime))
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date(currentTime))

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable(true)
            .onKeyEvent {
                viewModel.navigateTo(Screen.HOME)
                true
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { viewModel.navigateTo(Screen.HOME) }
            )
    ) {
        CinematicBackground(
            backgroundImageUri = viewModel.settingsManager.backgroundImageUri,
            dimAmount = 0.25f,
            enableParallax = true,
            performanceMode = viewModel.settingsManager.performanceMode,
            parallaxOffsetX = pulse * 100f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeFormat,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 110.sp,
                fontWeight = FontWeight.ExtraLight,
                letterSpacing = 2.sp
            )

            Text(
                text = dateFormat,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 28.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Press any button on your remote to resume",
                color = Color.White.copy(alpha = 0.45f * pulse),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
