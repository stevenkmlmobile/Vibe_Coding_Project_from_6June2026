package com.example.nebulastrike.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nebulastrike.audio.SoundManager
import com.example.nebulastrike.data.ScoreManager
import com.example.nebulastrike.game.GameEngine
import kotlinx.coroutines.isActive

@Composable
fun GameScreen(
    scoreManager: ScoreManager,
    soundManager: SoundManager,
    onReturnToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Remember game engine instance
    val gameEngine = remember { GameEngine(soundManager = soundManager) }
    
    // UI state mirroring engine variables
    var score by remember { mutableStateOf(0) }
    var level by remember { mutableStateOf(1) }
    var isGameOver by remember { mutableStateOf(false) }
    
    // Game Over Name Input State
    var pilotName by remember { mutableStateOf("") }
    var scoreSaved by remember { mutableStateOf(false) }
    var isHighScore by remember { mutableStateOf(false) }

    // Synchronize UI State from Game Loop ticker
    LaunchedEffect(key1 = gameEngine) {
        while (isActive) {
            withFrameMillis {
                score = gameEngine.score
                level = gameEngine.level
                
                if (gameEngine.isGameOver && !isGameOver) {
                    isGameOver = true
                    isHighScore = scoreManager.isHighScore(score)
                    scoreSaved = false
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Core Drawing Canvas
        GameCanvas(
            gameEngine = gameEngine,
            onGameOver = { finalScore ->
                // Handled in ticker loop
            }
        )

        // HUD Text Overlay (Top of Screen)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level Display
            Column {
                Text(
                    text = "SECTOR",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$level",
                    color = Color(0xFFD500F9),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    style = TextStyle(
                        shadow = Shadow(Color(0xFFD500F9).copy(alpha = 0.5f), Offset(0f, 0f), 8f)
                    )
                )
            }

            // Score Display
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "SCORE",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$score",
                    color = Color(0xFF00E5FF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    style = TextStyle(
                        shadow = Shadow(Color(0xFF00E5FF).copy(alpha = 0.5f), Offset(0f, 0f), 8f)
                    )
                )
            }
        }

        // Pause/Reset Button (Top Center-Right)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x1F2937).copy(alpha = 0.6f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .clickable {
                    soundManager.playLaser()
                    gameEngine.reset()
                    isGameOver = false
                    scoreSaved = false
                    pilotName = ""
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "RESTART",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Game Over Glassmorphism Modal Dialogue Overlay
        AnimatedVisibility(
            visible = isGameOver,
            enter = fadeIn(animationSpec = tween(600)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "redPulse")
                val textGlow by infiniteTransition.animateFloat(
                    initialValue = 10f,
                    targetValue = 25f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(Color(0xFFFF1744), Color(0xFFD500F9))),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MISSION FAILED",
                        color = Color(0xFFFF1744),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xFFFF1744).copy(alpha = 0.8f),
                                offset = Offset(0f, 0f),
                                blurRadius = textGlow
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "FINAL SCORE",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "$score",
                        color = Color(0xFF00E5FF),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(
                            shadow = Shadow(Color(0xFF00E5FF).copy(alpha = 0.6f), Offset(0f, 0f), 12f)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Leaderboard Entry
                    if (isHighScore && !scoreSaved) {
                        Text(
                            text = "NEW HIGH SCORE DETECTED!",
                            color = Color(0xFFFFEA00),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = pilotName,
                            onValueChange = { if (it.length <= 10) pilotName = it },
                            label = { Text("PILOT CALLSIGN", color = Color.White.copy(alpha = 0.6f)) },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = Color(0xFF00E5FF)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (pilotName.isNotBlank()) {
                                    scoreManager.saveHighScore(pilotName.trim(), score)
                                    scoreSaved = true
                                    soundManager.playPowerUp()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEA00)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("TRANSMIT CODES", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else if (scoreSaved) {
                        Text(
                            text = "CODES TRANSMITTED SECURELY",
                            color = Color(0xFF00E676),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                soundManager.playLaser()
                                gameEngine.reset()
                                isGameOver = false
                                scoreSaved = false
                                pilotName = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("RETRY", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                soundManager.playLaser()
                                onReturnToMenu()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD500F9)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ABORT", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
