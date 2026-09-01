package com.example

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush

data class WallpaperPreset(
    val id: String,
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val baseDark: Color
)

object WallpaperPresets {
    val presets = listOf(
        WallpaperPreset(
            id = "nebula",
            name = "Deep Nebula",
            primaryColor = Color(0xFF1E1035),
            secondaryColor = Color(0xFF0F172A),
            accentColor = Color(0xFF38BDF8),
            baseDark = Color(0xFF07040D)
        ),
        WallpaperPreset(
            id = "aurora",
            name = "Emerald Aurora",
            primaryColor = Color(0xFF064E3B),
            secondaryColor = Color(0xFF0F172A),
            accentColor = Color(0xFF10B981),
            baseDark = Color(0xFF021B14)
        ),
        WallpaperPreset(
            id = "twilight",
            name = "Twilight Horizon",
            primaryColor = Color(0xFF4C0519),
            secondaryColor = Color(0xFF1E1B4B),
            accentColor = Color(0xFFF43F5E),
            baseDark = Color(0xFF0B0716)
        ),
        WallpaperPreset(
            id = "cosmic",
            name = "Cosmic Blue",
            primaryColor = Color(0xFF1E3A8A),
            secondaryColor = Color(0xFF172554),
            accentColor = Color(0xFF60A5FA),
            baseDark = Color(0xFF030712)
        ),
        WallpaperPreset(
            id = "sunset",
            name = "Crimson Dusk",
            primaryColor = Color(0xFF7C2D12),
            secondaryColor = Color(0xFF1C1917),
            accentColor = Color(0xFFFB923C),
            baseDark = Color(0xFF090605)
        ),
        WallpaperPreset(
            id = "obsidian",
            name = "Obsidian Minimal",
            primaryColor = Color(0xFF18181B),
            secondaryColor = Color(0xFF09090B),
            accentColor = Color(0xFF71717A),
            baseDark = Color(0xFF050507)
        )
    )

    fun getPreset(id: String): WallpaperPreset {
        return presets.find { it.id == id } ?: presets[0]
    }
}

@Composable
fun CinematicBackground(
    presetId: String,
    dimAmount: Float,
    enableParallax: Boolean,
    performanceMode: Boolean,
    parallaxOffsetX: Float = 0f
) {
    val preset = WallpaperPresets.getPreset(presetId)

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val pulse by if (!performanceMode) {
        infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    Box(modifier = Modifier.fillMaxSize().background(preset.baseDark)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val parallaxShift = if (enableParallax) parallaxOffsetX * 0.12f else 0f

            // 1. Base dark background gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        preset.baseDark,
                        preset.secondaryColor.copy(alpha = 0.6f),
                        preset.baseDark
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // 2. Large top-right ambient glow orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        preset.primaryColor.copy(alpha = 0.55f * pulse),
                        preset.primaryColor.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.75f + parallaxShift, height * 0.25f),
                    radius = width * 0.55f * pulse
                ),
                center = Offset(width * 0.75f + parallaxShift, height * 0.25f),
                radius = width * 0.55f * pulse
            )

            // 3. Lower-left subtle accent glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        preset.accentColor.copy(alpha = 0.25f),
                        preset.secondaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.20f - (parallaxShift * 0.5f), height * 0.80f),
                    radius = width * 0.45f
                ),
                center = Offset(width * 0.20f - (parallaxShift * 0.5f), height * 0.80f),
                radius = width * 0.45f
            )

            // 4. Subtle center-top highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.04f * pulse),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.1f),
                    radius = width * 0.35f
                ),
                center = Offset(width * 0.5f, height * 0.1f),
                radius = width * 0.35f
            )
        }

        // Configurable darkness overlay layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dimAmount.coerceIn(0.05f, 0.85f)))
        )
    }
}
