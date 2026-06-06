package com.example.nebulastrike.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nebulastrike.audio.SoundManager
import kotlinx.coroutines.isActive

@Composable
fun MainMenuScreen(
    soundManager: SoundManager,
    onStartGame: () -> Unit,
    onViewScores: () -> Unit,
    onViewSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Infinite transition for title pulse and glow
    val transition = rememberInfiniteTransition(label = "titlePulse")
    val titleScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val glowIntensity by transition.animateFloat(
        initialValue = 10f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Animated starfield specifically for the menu background
    val stars = remember {
        mutableStateListOf<Star>().apply {
            for (i in 0..60) {
                add(
                    Star(
                        x = (Math.random() * 1200).toFloat(),
                        y = (Math.random() * 2000).toFloat(),
                        size = (Math.random() * 4 + 1.5f).toFloat(),
                        speed = (Math.random() * 1.5f + 0.5f).toFloat(),
                        color = Color.White.copy(alpha = (Math.random() * 0.6f + 0.2f).toFloat())
                    )
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameMillis {
                stars.forEach { star ->
                    star.y += star.speed
                    if (star.y > 2200f) {
                        star.y = -10f
                        star.x = (Math.random() * 1200).toFloat()
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060A17))
    ) {
        // Draw Menu Starfield
        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEach { star ->
                drawCircle(
                    color = star.color,
                    radius = star.size,
                    center = Offset(star.x / 1200f * size.width, star.y / 2000f * size.height)
                )
            }
        }

        // Main Menu Content Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Game Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "NEBULA",
                    style = TextStyle(
                        fontSize = (52f * titleScale).sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFF00E5FF),
                        textAlign = TextAlign.Center,
                        shadow = Shadow(
                            color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                            offset = Offset(0f, 0f),
                            blurRadius = glowIntensity
                        )
                    )
                )
                Text(
                    text = "STRIKE",
                    style = TextStyle(
                        fontSize = (60f * titleScale).sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFFD500F9),
                        textAlign = TextAlign.Center,
                        shadow = Shadow(
                            color = Color(0xFFD500F9).copy(alpha = 0.8f),
                            offset = Offset(0f, 0f),
                            blurRadius = glowIntensity * 1.2f
                        )
                    )
                )
            }

            Text(
                text = "RETRO ARCADE SHIELD OPERATIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Menu Buttons (Glassmorphism layout)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MenuButton(
                    text = "LAUNCH MISSION",
                    color1 = Color(0xFF00E5FF),
                    color2 = Color(0xFF00E676),
                    onClick = {
                        soundManager.playLaser()
                        onStartGame()
                    }
                )

                MenuButton(
                    text = "LEADERBOARD",
                    color1 = Color(0xFFD500F9),
                    color2 = Color(0xFF00E5FF),
                    onClick = {
                        soundManager.playLaser()
                        onViewScores()
                    }
                )

                MenuButton(
                    text = "SETTINGS",
                    color1 = Color(0xFF334155),
                    color2 = Color(0xFF475569),
                    onClick = {
                        soundManager.playLaser()
                        onViewSettings()
                    }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    color1: Color,
    color2: Color,
    onClick: () -> Unit
) {
    val borderBrush = Brush.linearGradient(listOf(color1, color2))
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x1E1E2F).copy(alpha = 0.7f))
            .border(2.dp, borderBrush, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = color1.copy(alpha = 0.6f),
                    offset = Offset(0f, 0f),
                    blurRadius = 6f
                )
            )
        )
    }
}
