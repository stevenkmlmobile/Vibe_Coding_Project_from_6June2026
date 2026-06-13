package com.example.nebulastrike.game

import androidx.compose.ui.graphics.Color

enum class EnemyType(val maxHealth: Float, val scoreValue: Int, val radius: Float) {
    SCOUT(10f, 100, 20f),
    BOMBER(30f, 250, 30f),
    HUNTER(20f, 200, 25f),
    BOSS(500f, 2000, 60f)
}

enum class PowerUpType(val color: Color) {
    SHIELD(Color(0xFF00E5FF)),      // Cyan glow
    DOUBLE_SHOT(Color(0xFFFFEA00)), // Yellow
    TRIPLE_SHOT(Color(0xFFFF3D00)), // Red-Orange
    SLOW_MO(Color(0xFFD500F9)),     // Purple
    HEALTH(Color(0xFF00E676))       // Green
}

data class PlayerShip(
    var x: Float = 0f,
    var y: Float = 0f,
    var radius: Float = 24f,
    var health: Float = 100f,
    var maxHealth: Float = 100f,
    var shield: Float = 0f, // 0 to 100
    var maxShield: Float = 100f,
    var weaponLevel: Int = 1, // 1: single, 2: double, 3: triple
    var lastShotTime: Long = 0L,
    var doubleShotExpiry: Long = 0L,
    var tripleShotExpiry: Long = 0L,
    var shieldExpiry: Long = 0L,
    var slowMoExpiry: Long = 0L
) {
    fun isShieldActive(currentTime: Long): Boolean = shield > 0f || currentTime < shieldExpiry
    fun isDoubleShotActive(currentTime: Long): Boolean = currentTime < doubleShotExpiry
    fun isTripleShotActive(currentTime: Long): Boolean = currentTime < tripleShotExpiry
    fun isSlowMoActive(currentTime: Long): Boolean = currentTime < slowMoExpiry
}

data class Enemy(
    val id: String,
    val type: EnemyType,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var health: Float,
    var lastShotTime: Long = 0L,
    var angle: Float = 0f, // For rotating/bobbing animations
    var amplitude: Float = 0f, // For sinus movement patterns
    var frequency: Float = 0f,
    var spawnTime: Long = 0L
)

data class Laser(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val isPlayerLaser: Boolean,
    val damage: Float,
    val color: Color,
    val width: Float = 4f,
    val length: Float = 15f
)

data class PowerUp(
    val type: PowerUpType,
    var x: Float,
    var y: Float,
    val vy: Float,
    val radius: Float = 18f
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var size: Float,
    var alpha: Float,
    val decay: Float, // rate of alpha decay per tick
    val scaleDecay: Float = 0.95f // rate of size decay
)
