package com.example.nebulastrike.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import com.example.nebulastrike.game.Enemy
import com.example.nebulastrike.game.EnemyType
import com.example.nebulastrike.game.GameEngine
import com.example.nebulastrike.game.Laser
import com.example.nebulastrike.game.Particle
import com.example.nebulastrike.game.PlayerShip
import com.example.nebulastrike.game.PowerUp
import com.example.nebulastrike.game.PowerUpType
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

data class Star(
    var x: Float,
    var y: Float,
    val size: Float,
    val speed: Float,
    val color: Color
)

@Composable
fun GameCanvas(
    gameEngine: GameEngine,
    modifier: Modifier = Modifier,
    onGameOver: (Int) -> Unit
) {
    var frameTime by remember { mutableStateOf(0L) }
    
    // Parallax Starfield Background
    val stars = remember {
        mutableStateListOf<Star>().apply {
            for (i in 0..120) {
                add(
                    Star(
                        x = (Math.random() * 1200).toFloat(),
                        y = (Math.random() * 2000).toFloat(),
                        size = (Math.random() * 3 + 1).toFloat(),
                        speed = (Math.random() * 4 + 1).toFloat(),
                        color = Color.White.copy(alpha = (Math.random() * 0.7 + 0.3).toFloat())
                    )
                )
            }
        }
    }

    // 60FPS Game Loop Ticker
    LaunchedEffect(key1 = gameEngine) {
        while (isActive) {
            withFrameMillis { time ->
                frameTime = time
                
                // Update Starfield
                val isSlowMo = gameEngine.player.isSlowMoActive(time)
                val starSpeedMult = if (isSlowMo) 0.4f else 1.0f
                stars.forEach { star ->
                    star.y += star.speed * starSpeedMult
                    if (star.y > gameEngine.screenHeight) {
                        star.y = -10f
                        star.x = (Math.random() * gameEngine.screenWidth).toFloat()
                    }
                }
                
                // Update Game
                gameEngine.update(time)
                
                // Game Over Check
                if (gameEngine.isGameOver) {
                    onGameOver(gameEngine.score)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B19)) // Deep outer space color
            .pointerInput(gameEngine) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (!gameEngine.isGameOver) {
                        // Drag movement: adjust player position
                        val newX = (gameEngine.player.x + dragAmount.x).coerceIn(
                            gameEngine.player.radius,
                            gameEngine.screenWidth - gameEngine.player.radius
                        )
                        val newY = (gameEngine.player.y + dragAmount.y).coerceIn(
                            gameEngine.screenHeight * 0.3f, // Restrict player from going too far up
                            gameEngine.screenHeight - gameEngine.player.radius
                        )
                        gameEngine.player.x = newX
                        gameEngine.player.y = newY
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Set dynamic canvas bounds
            gameEngine.screenWidth = size.width
            gameEngine.screenHeight = size.height

            // 1. Draw Starfield
            stars.forEach { star ->
                drawCircle(
                    color = star.color,
                    radius = star.size,
                    center = Offset(star.x, star.y)
                )
            }

            // 2. Spawn Thruster Particles for Player
            if (!gameEngine.isGameOver && Math.random() < 0.8) {
                val isSlowMo = gameEngine.player.isSlowMoActive(frameTime)
                val particleColor = if (isSlowMo) Color(0xFFD500F9) else Color(0xFFFF6D00)
                gameEngine.particles.add(
                    Particle(
                        x = gameEngine.player.x + (Math.random() * 16 - 8).toFloat(),
                        y = gameEngine.player.y + gameEngine.player.radius,
                        vx = (Math.random() * 2 - 1).toFloat(),
                        vy = (Math.random() * 5 + 3).toFloat(),
                        color = particleColor,
                        size = (Math.random() * 8 + 4).toFloat(),
                        alpha = 1.0f,
                        decay = 0.05f
                    )
                )
            }

            // 3. Draw PowerUps
            gameEngine.powerUps.forEach { powerUp ->
                drawPowerUp(powerUp, frameTime)
            }

            // 4. Draw Lasers (glowing neon effect)
            gameEngine.lasers.forEach { laser ->
                drawLaser(laser)
            }

            // 5. Draw Enemies
            gameEngine.enemies.forEach { enemy ->
                drawEnemy(enemy, frameTime)
            }

            // 6. Draw Particles (Explosions/Thrusters)
            gameEngine.particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size,
                    center = Offset(p.x, p.y)
                )
            }

            // 7. Draw Player Ship
            if (!gameEngine.isGameOver) {
                drawPlayer(gameEngine.player, frameTime)
            }

            // 8. Draw HUD Overlay
            drawHUD(gameEngine, frameTime)
        }
    }
}

// Draw Extensions
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlayer(player: PlayerShip, frameTime: Long) {
    val x = player.x
    val y = player.y
    val r = player.radius

    // Custom glowing futuristic path for player ship
    val shipPath = Path().apply {
        moveTo(x, y - r * 1.3f) // Nose
        lineTo(x - r * 0.8f, y + r * 0.6f) // Left Wing
        lineTo(x - r * 0.4f, y + r * 0.4f) // Left engine inner
        lineTo(x, y + r * 0.8f) // Rear indent
        lineTo(x + r * 0.4f, y + r * 0.4f) // Right engine inner
        lineTo(x + r * 0.8f, y + r * 0.6f) // Right Wing
        close()
    }

    // Shield active color indicator
    val isShield = player.isShieldActive(frameTime)
    val themeColor = if (isShield) Color(0xFF00E5FF) else Color(0xFF00E676)

    // Glowing ship shadow
    drawPath(
        path = shipPath,
        color = themeColor.copy(alpha = 0.2f),
        style = Stroke(width = 12f, cap = StrokeCap.Round)
    )
    
    // Fill ship
    drawPath(
        path = shipPath,
        color = Color(0xFF1E293B)
    )

    // Inner details (wing decorations)
    drawPath(
        path = shipPath,
        color = themeColor,
        style = Stroke(width = 3f)
    )

    // Cockpit
    drawCircle(
        color = Color.White,
        radius = r * 0.25f,
        center = Offset(x, y - r * 0.2f)
    )
    
    // Draw Shield bubble
    if (isShield) {
        val shieldPulse = 1f + 0.08f * sin((frameTime / 150f)).toFloat()
        val shieldRadius = r * 1.8f * shieldPulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.0f), Color(0xFF00E5FF).copy(alpha = 0.25f), Color(0xFF00E5FF).copy(alpha = 0.7f)),
                center = Offset(x, y),
                radius = shieldRadius
            ),
            radius = shieldRadius,
            center = Offset(x, y),
            style = Stroke(width = 4f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEnemy(enemy: Enemy, frameTime: Long) {
    val x = enemy.x
    val y = enemy.y
    val r = enemy.type.radius
    val isSlowMo = enemy.type == EnemyType.BOSS // or similar glow

    val path = Path()
    val color = when (enemy.type) {
        EnemyType.SCOUT -> Color(0xFFFF7043) // Red-Orange
        EnemyType.BOMBER -> Color(0xFFFF1744) // Bright Red
        EnemyType.HUNTER -> Color(0xFFD500F9) // Purple-Magenta
        EnemyType.BOSS -> Color(0xFFFFEA00) // Gold
    }

    when (enemy.type) {
        EnemyType.SCOUT -> {
            path.moveTo(x, y - r)
            path.lineTo(x - r * 0.7f, y)
            path.lineTo(x, y + r)
            path.lineTo(x + r * 0.7f, y)
            path.close()
        }
        EnemyType.BOMBER -> {
            path.moveTo(x - r, y - r * 0.3f)
            path.lineTo(x - r * 0.4f, y - r)
            path.lineTo(x + r * 0.4f, y - r)
            path.lineTo(x + r, y - r * 0.3f)
            path.lineTo(x + r * 0.6f, y + r * 0.6f)
            path.lineTo(x - r * 0.6f, y + r * 0.6f)
            path.close()
        }
        EnemyType.HUNTER -> {
            path.moveTo(x, y + r)
            path.lineTo(x - r * 0.9f, y - r)
            path.lineTo(x, y - r * 0.3f)
            path.lineTo(x + r * 0.9f, y - r)
            path.close()
        }
        EnemyType.BOSS -> {
            // Giant spacecraft dreadnought
            path.moveTo(x, y + r)
            path.lineTo(x - r * 0.8f, y + r * 0.4f)
            path.lineTo(x - r, y - r * 0.3f)
            path.lineTo(x - r * 0.6f, y - r)
            path.lineTo(x + r * 0.6f, y - r)
            path.lineTo(x + r, y - r * 0.3f)
            path.lineTo(x + r * 0.8f, y + r * 0.4f)
            path.close()
        }
    }

    // Outer glow
    drawPath(
        path = path,
        color = color.copy(alpha = 0.25f),
        style = Stroke(width = 8f)
    )

    // Base body
    drawPath(
        path = path,
        color = Color(0xFF1E1E2F)
    )

    // Design Outline
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3f)
    )

    // Boss health bar directly above it
    if (enemy.type == EnemyType.BOSS) {
        val barW = r * 1.5f
        val barH = 8f
        val pct = (enemy.health / enemy.type.maxHealth).coerceIn(0f, 1f)
        
        drawRect(
            color = Color.DarkGray,
            topLeft = Offset(x - barW / 2f, y - r - 20f),
            size = Size(barW, barH)
        )
        drawRect(
            color = Color.Yellow,
            topLeft = Offset(x - barW / 2f, y - r - 20f),
            size = Size(barW * pct, barH)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLaser(laser: Laser) {
    // Glowing neon laser beam
    val start = Offset(laser.x, laser.y)
    val end = Offset(laser.x + laser.vx * 0.5f, laser.y + laser.length * (if (laser.vy < 0) -1 else 1))

    // Outer thick glow
    drawLine(
        color = laser.color.copy(alpha = 0.4f),
        start = start,
        end = end,
        strokeWidth = laser.width * 2.5f,
        cap = StrokeCap.Round
    )
    
    // Core white/neon line
    drawLine(
        color = Color.White,
        start = start,
        end = end,
        strokeWidth = laser.width,
        cap = StrokeCap.Round
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPowerUp(powerUp: PowerUp, frameTime: Long) {
    val x = powerUp.x
    val y = powerUp.y
    val r = powerUp.radius
    val color = powerUp.type.color

    // Pulsing animation
    val pulse = 1f + 0.12f * sin((frameTime / 120f)).toFloat()
    val size = r * pulse

    // Draw glowing back circle
    drawCircle(
        color = color.copy(alpha = 0.15f),
        radius = size * 1.5f,
        center = Offset(x, y)
    )
    
    // Draw outer outline
    drawCircle(
        color = color,
        radius = size,
        center = Offset(x, y),
        style = Stroke(width = 3f)
    )

    // Inner filled circle
    drawCircle(
        color = Color(0xFF0F172A),
        radius = size * 0.8f,
        center = Offset(x, y)
    )

    // Draw icon letter
    val textChar = when (powerUp.type) {
        PowerUpType.SHIELD -> "S"
        PowerUpType.DOUBLE_SHOT -> "D"
        PowerUpType.TRIPLE_SHOT -> "T"
        PowerUpType.SLOW_MO -> "M"
        PowerUpType.HEALTH -> "H"
    }

    // Render letter via simple Canvas shapes to avoid font sizing issues in direct Canvas draw
    drawPowerupIconSymbol(powerUp.type, x, y, size)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPowerupIconSymbol(type: PowerUpType, x: Float, y: Float, size: Float) {
    val color = type.color
    val strokeW = 3f
    
    when (type) {
        PowerUpType.SHIELD -> {
            // Draw a shield outline (crescent shape or shield arc)
            val path = Path().apply {
                moveTo(x - size * 0.3f, y - size * 0.3f)
                lineTo(x + size * 0.3f, y - size * 0.3f)
                lineTo(x + size * 0.3f, y + size * 0.1f)
                quadraticTo(x, y + size * 0.5f, x - size * 0.3f, y + size * 0.1f)
                close()
            }
            drawPath(path, color, style = Stroke(width = strokeW))
        }
        PowerUpType.DOUBLE_SHOT -> {
            // Draw two parallel small laser symbols
            drawLine(color, Offset(x - 6f, y - 10f), Offset(x - 6f, y + 10f), strokeWidth = 4f)
            drawLine(color, Offset(x + 6f, y - 10f), Offset(x + 6f, y + 10f), strokeWidth = 4f)
        }
        PowerUpType.TRIPLE_SHOT -> {
            // Draw three small lasers diverging
            drawLine(color, Offset(x - 10f, y - 8f), Offset(x - 10f, y + 8f), strokeWidth = 3f)
            drawLine(color, Offset(x, y - 10f), Offset(x, y + 10f), strokeWidth = 3f)
            drawLine(color, Offset(x + 10f, y - 8f), Offset(x + 10f, y + 8f), strokeWidth = 3f)
        }
        PowerUpType.SLOW_MO -> {
            // Draw an hourglass or clock shape
            drawCircle(color, size * 0.35f, Offset(x, y), style = Stroke(width = 3f))
            drawLine(color, Offset(x, y), Offset(x, y - size * 0.25f), strokeWidth = 4f)
            drawLine(color, Offset(x, y), Offset(x + size * 0.15f, y + size * 0.15f), strokeWidth = 4f)
        }
        PowerUpType.HEALTH -> {
            // Draw a cross
            val w = size * 0.12f
            val h = size * 0.4f
            drawRect(color, Offset(x - w, y - h), Size(w * 2f, h * 2f))
            drawRect(color, Offset(x - h, y - w), Size(h * 2f, w * 2f))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHUD(gameEngine: GameEngine, frameTime: Long) {
    val padding = 40f
    
    // Draw Health bar (top left)
    val barW = 320f
    val barH = 24f
    val healthPct = (gameEngine.player.health / gameEngine.player.maxHealth).coerceIn(0f, 1f)
    
    // Health Bar Container
    drawRect(
        color = Color(0xFF1E293B).copy(alpha = 0.6f),
        topLeft = Offset(padding, padding + 10f),
        size = Size(barW, barH)
    )
    // Health Bar Fill
    drawRect(
        color = Color(0xFF00E676),
        topLeft = Offset(padding, padding + 10f),
        size = Size(barW * healthPct, barH)
    )
    // Health Bar Border
    drawRect(
        color = Color.White.copy(alpha = 0.5f),
        topLeft = Offset(padding, padding + 10f),
        size = Size(barW, barH),
        style = Stroke(width = 2f)
    )

    // Draw Shield bar (if shield active)
    val shieldPct = (gameEngine.player.shield / gameEngine.player.maxShield).coerceIn(0f, 1f)
    if (gameEngine.player.isShieldActive(frameTime) || shieldPct > 0f) {
        drawRect(
            color = Color(0xFF1E293B).copy(alpha = 0.6f),
            topLeft = Offset(padding, padding + 44f),
            size = Size(barW, 14f)
        )
        drawRect(
            color = Color(0xFF00E5FF),
            topLeft = Offset(padding, padding + 44f),
            size = Size(barW * shieldPct, 14f)
        )
        drawRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(padding, padding + 44f),
            size = Size(barW, 14f),
            style = Stroke(width = 2f)
        )
    }

    // Active power-up icons on bottom screen
    var powerUpIdx = 0
    val pSize = 50f
    val bottomY = size.height - 180f
    
    fun drawActivePowerUpIndicator(type: PowerUpType, expiryTime: Long) {
        val remainingMs = expiryTime - frameTime
        if (remainingMs > 0) {
            val progress = (remainingMs / 8000f).coerceIn(0f, 1f)
            val startX = padding + powerUpIdx * 110f
            
            // Draw circular timer progress background
            drawCircle(
                color = Color.DarkGray.copy(alpha = 0.5f),
                radius = pSize * 0.8f,
                center = Offset(startX + pSize, bottomY + pSize)
            )
            // Progress arc
            drawArc(
                color = type.color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(startX + pSize - pSize * 0.8f, bottomY + pSize - pSize * 0.8f),
                size = Size(pSize * 1.6f, pSize * 1.6f),
                style = Stroke(width = 4f)
            )
            
            // Draw Icon Symbol in center
            drawPowerupIconSymbol(type, startX + pSize, bottomY + pSize, pSize * 0.8f)
            powerUpIdx++
        }
    }

    drawActivePowerUpIndicator(PowerUpType.SHIELD, gameEngine.player.shieldExpiry)
    drawActivePowerUpIndicator(PowerUpType.DOUBLE_SHOT, gameEngine.player.doubleShotExpiry)
    drawActivePowerUpIndicator(PowerUpType.TRIPLE_SHOT, gameEngine.player.tripleShotExpiry)
    drawActivePowerUpIndicator(PowerUpType.SLOW_MO, gameEngine.player.slowMoExpiry)
}
