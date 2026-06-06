package com.example.nebulastrike

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey
@Serializable data object Game : NavKey
@Serializable data object HighScores : NavKey
@Serializable data object Settings : NavKey
