package com.example.nebulastrike

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nebulastrike.audio.SoundManager
import com.example.nebulastrike.data.ScoreManager
import com.example.nebulastrike.ui.GameScreen
import com.example.nebulastrike.ui.HighScoresScreen
import com.example.nebulastrike.ui.MainMenuScreen
import com.example.nebulastrike.ui.SettingsScreen

@Composable
fun MainNavigation(
    scoreManager: ScoreManager,
    soundManager: SoundManager
) {
    val backStack = rememberNavBackStack(Main)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                MainMenuScreen(
                    soundManager = soundManager,
                    onStartGame = { backStack.add(Game) },
                    onViewScores = { backStack.add(HighScores) },
                    onViewSettings = { backStack.add(Settings) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<Game> {
                GameScreen(
                    scoreManager = scoreManager,
                    soundManager = soundManager,
                    onReturnToMenu = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<HighScores> {
                HighScoresScreen(
                    scoreManager = scoreManager,
                    soundManager = soundManager,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<Settings> {
                SettingsScreen(
                    soundManager = soundManager,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
    )
}
