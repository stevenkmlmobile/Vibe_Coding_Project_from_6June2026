package com.example.nebulastrike.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nebulastrike.audio.SoundManager
import com.example.nebulastrike.data.ScoreManager

@Composable
fun HighScoresScreen(
    scoreManager: ScoreManager,
    soundManager: SoundManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scores = remember { scoreManager.getHighScores() }

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
                text = "HIGH SCORES",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFD500F9),
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Leaderboard Card container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1F2937).copy(alpha = 0.4f))
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(Color(0xFFD500F9).copy(alpha = 0.3f), Color(0xFF00E5FF).copy(alpha = 0.3f))),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                if (scores.isEmpty()) {
                    Text(
                        text = "NO MISSIONS REPORTED YET",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(scores) { index, item ->
                            val rankColor = when (index) {
                                0 -> Color(0xFFFFEA00) // Gold
                                1 -> Color(0xFFE2E8F0) // Silver
                                2 -> Color(0xFFD97706) // Bronze
                                else -> Color(0xFF00E5FF) // Cyber Cyan
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x0F172A).copy(alpha = 0.8f))
                                    .border(1.dp, rankColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Rank Number Circle
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(rankColor.copy(alpha = 0.15f))
                                            .border(1.dp, rankColor, RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = rankColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Player Name & Date
                                    Column {
                                        Text(
                                            text = item.name.uppercase(),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.date,
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                // Score
                                Text(
                                    text = "${item.score}",
                                    color = rankColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
