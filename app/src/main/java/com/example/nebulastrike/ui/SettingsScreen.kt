package com.example.nebulastrike.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nebulastrike.audio.SoundManager

@Composable
fun SettingsScreen(
    soundManager: SoundManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var soundEnabled by remember { mutableStateOf(soundManager.isSoundEnabled) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060A17))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Screen Title
            Text(
                text = "SETTINGS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF00E5FF),
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Settings Container card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1F2937).copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.3f), Color(0xFFD500F9).copy(alpha = 0.3f))),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Sound Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SOUND EFFECTS",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Procedural audio feedback",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { checked ->
                            soundEnabled = checked
                            soundManager.isSoundEnabled = checked
                            if (checked) {
                                soundManager.playLaser()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E5FF),
                            checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.3f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // System specs card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1F2937).copy(alpha = 0.3f))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NEBULA STRIKE v1.0",
                    color = Color(0xFFD500F9),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Built natively in Kotlin using Jetpack Compose Canvas. Features real-time audio wave synthesis and particle physics simulation.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Back button
            MenuButton(
                text = "BACK TO BASE",
                color1 = Color(0xFF334155),
                color2 = Color(0xFF475569),
                onClick = {
                    soundManager.playLaser()
                    onBack()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
