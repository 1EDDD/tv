package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import coil.compose.AsyncImage

@Composable
fun CinematicBackground(
    backgroundImageUri: String?,
    dimAmount: Float,
    enableParallax: Boolean,
    performanceMode: Boolean,
    parallaxOffsetX: Float = 0f
) {
    val pureBlack = Color(0xFF000000)

    Box(modifier = Modifier.fillMaxSize().background(pureBlack)) {
        if (!backgroundImageUri.isNullOrEmpty()) {
            val parallaxShift = if (enableParallax) parallaxOffsetX * 0.12f else 0f
            AsyncImage(
                model = backgroundImageUri,
                contentDescription = "Background",
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = parallaxShift.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Extremely subtle darkness overlay layer
        if (dimAmount > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimAmount.coerceIn(0f, 1f)))
            )
        }
    }
}
