package com.example.nebulastrike.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class HighScore(
    val name: String,
    val score: Int,
    val date: String
) : Comparable<HighScore> {
    override fun compareTo(other: HighScore): Int {
        return other.score.compareTo(this.score) // Descending order
    }
}

class ScoreManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nebula_strike_scores", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun getHighScores(): List<HighScore> {
        val jsonStr = prefs.getString("scores", null) ?: return getDefaultScores()
        return try {
            json.decodeFromString<List<HighScore>>(jsonStr).sorted()
        } catch (e: Exception) {
            getDefaultScores()
        }
    }

    fun saveHighScore(name: String, score: Int) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        val newScore = HighScore(name.ifBlank { "Pilot" }, score, dateStr)
        
        val currentScores = getHighScores().toMutableList()
        currentScores.add(newScore)
        val sortedScores = currentScores.sorted().take(5) // Keep top 5
        
        prefs.edit().putString("scores", json.encodeToString(sortedScores)).apply()
    }

    fun isHighScore(score: Int): Boolean {
        val scores = getHighScores()
        if (scores.size < 5) return true
        return score > scores.last().score
    }

    private fun getDefaultScores(): List<HighScore> {
        return listOf(
            HighScore("Astraea", 5000, "2026-06-01"),
            HighScore("Hyperion", 3000, "2026-06-02"),
            HighScore("Vanguard", 1500, "2026-06-03"),
            HighScore("Novak", 800, "2026-06-04"),
            HighScore("Rookie", 300, "2026-06-05")
        ).sorted()
    }
}
