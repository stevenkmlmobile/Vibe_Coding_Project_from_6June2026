package com.example.nebulastrike.game

import androidx.compose.ui.graphics.Color
import com.example.nebulastrike.audio.SoundManager
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GameEngine(
    var screenWidth: Float = 1080f,
    var screenHeight: Float = 1920f,
    private val soundManager: SoundManager? = null
) {
    // Game States
    var player = PlayerShip()
    val enemies = mutableListOf<Enemy>()
    val lasers = mutableListOf<Laser>()
    val powerUps = mutableListOf<PowerUp>()
    val particles = mutableListOf<Particle>()

    var score = 0
    var isGameOver = false
    var level = 1
    var kills = 0
    
    private var lastEnemySpawnTime = 0L
    private var lastLevelUpTime = 0L
    private var bossSpawned = false

    init {
        reset()
    }

    fun reset() {
        player = PlayerShip(
            x = screenWidth / 2f,
            y = screenHeight * 0.8f,
            radius = 24f
        )
        enemies.clear()
        lasers.clear()
        powerUps.clear()
        particles.clear()
        score = 0
        isGameOver = false
        level = 1
        kills = 0
        bossSpawned = false
        lastEnemySpawnTime = 0L
        lastLevelUpTime = 0L
    }

    fun update(currentTime: Long) {
        if (isGameOver) {
            updateParticles()
            return
        }

        val isSlowMo = player.isSlowMoActive(currentTime)
        val speedMultiplier = if (isSlowMo) 0.5f else 1.0f

        // 1. Move Player Lasers & auto shoot
        if (currentTime - player.lastShotTime > 180) {
            shootPlayerLaser(currentTime)
        }

        // 2. Spawn Enemies
        spawnEnemies(currentTime)

        // 3. Move Entities
        updateEntities(speedMultiplier, currentTime)

        // 4. Check Collisions
        checkCollisions(currentTime)

        // 5. Clean up out-of-bounds
        cleanUp()
    }

    private fun shootPlayerLaser(currentTime: Long) {
        val laserSpeed = -30f
        val laserDamage = 10f
        val laserColor = Color(0xFF00E5FF) // Neon Cyan
        
        val activeTriple = player.isTripleShotActive(currentTime)
        val activeDouble = player.isDoubleShotActive(currentTime)

        if (activeTriple) {
            lasers.add(Laser(player.x, player.y - 30f, 0f, laserSpeed, true, laserDamage, laserColor))
            lasers.add(Laser(player.x - 25f, player.y - 10f, -6f, laserSpeed, true, laserDamage, laserColor))
            lasers.add(Laser(player.x + 25f, player.y - 10f, 6f, laserSpeed, true, laserDamage, laserColor))
        } else if (activeDouble) {
            lasers.add(Laser(player.x - 15f, player.y - 20f, 0f, laserSpeed, true, laserDamage, laserColor))
            lasers.add(Laser(player.x + 15f, player.y - 20f, 0f, laserSpeed, true, laserDamage, laserColor))
        } else {
            lasers.add(Laser(player.x, player.y - 30f, 0f, laserSpeed, true, laserDamage, laserColor))
        }
        player.lastShotTime = currentTime
        soundManager?.playLaser()
    }

    private fun spawnEnemies(currentTime: Long) {
        if (bossSpawned && enemies.any { it.type == EnemyType.BOSS }) return

        // Level scaling
        val spawnInterval = (2000 - (level * 150)).coerceAtLeast(600)
        
        // Spawn boss every 15 kills or 10000 points
        if (kills > 0 && kills % 20 == 0 && !bossSpawned) {
            bossSpawned = true
            val boss = Enemy(
                id = UUID.randomUUID().toString(),
                type = EnemyType.BOSS,
                x = screenWidth / 2f,
                y = -100f,
                vx = 3f,
                vy = 2f,
                health = EnemyType.BOSS.maxHealth,
                spawnTime = currentTime
            )
            enemies.add(boss)
            return
        }

        if (currentTime - lastEnemySpawnTime > spawnInterval) {
            val type = when {
                Math.random() < 0.15 -> EnemyType.BOMBER
                Math.random() < 0.35 -> EnemyType.HUNTER
                else -> EnemyType.SCOUT
            }

            val x = (Math.random() * (screenWidth - 100f) + 50f).toFloat()
            val y = -50f
            
            val vx = if (type == EnemyType.HUNTER) 0f else (Math.random() * 4 - 2).toFloat()
            val vy = when (type) {
                EnemyType.SCOUT -> (3f + level * 0.4f).coerceAtMost(8f)
                EnemyType.BOMBER -> 2f
                EnemyType.HUNTER -> 4f
                else -> 3f
            }

            enemies.add(
                Enemy(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    health = type.maxHealth,
                    spawnTime = currentTime,
                    amplitude = (40f + Math.random() * 40f).toFloat(),
                    frequency = (0.01f + Math.random() * 0.02f).toFloat()
                )
            )
            lastEnemySpawnTime = currentTime
        }

        // Auto Level Up after 30 seconds
        if (currentTime - lastLevelUpTime > 30000) {
            level++
            bossSpawned = false // Allow boss to spawn again in next level
            lastLevelUpTime = currentTime
        }
    }

    private fun updateEntities(speedMultiplier: Float, currentTime: Long) {
        // Move Enemies and let them shoot
        enemies.forEach { enemy ->
            val anglePhase = (currentTime - enemy.spawnTime)
            
            when (enemy.type) {
                EnemyType.SCOUT -> {
                    // Sinusoidal horizontal movement
                    enemy.x += enemy.vx * speedMultiplier
                    enemy.y += enemy.vy * speedMultiplier
                    // Bounce off walls
                    if (enemy.x < enemy.type.radius || enemy.x > screenWidth - enemy.type.radius) {
                        enemy.vx = -enemy.vx
                    }
                }
                EnemyType.BOMBER -> {
                    // Moves slowly down, occasionally stopping to shoot
                    enemy.y += enemy.vy * speedMultiplier
                    enemy.x += enemy.vx * speedMultiplier * 0.2f
                    
                    if (currentTime - enemy.lastShotTime > 1500) {
                        // Bomber shoots heavy laser
                        lasers.add(Laser(enemy.x, enemy.y + 40f, 0f, 12f, false, 20f, Color(0xFFFF1744), width = 6f, length = 25f))
                        enemy.lastShotTime = currentTime
                    }
                }
                EnemyType.HUNTER -> {
                    // Slowly tracks player's horizontal position
                    val dx = player.x - enemy.x
                    val step = if (dx > 0) 3f else -3f
                    enemy.x += step * speedMultiplier
                    enemy.y += enemy.vy * speedMultiplier
                }
                EnemyType.BOSS -> {
                    // Moves side-to-side at top of screen
                    if (enemy.y < 250f) {
                        enemy.y += enemy.vy * speedMultiplier
                    } else {
                        enemy.x += enemy.vx * speedMultiplier
                        if (enemy.x < 150f || enemy.x > screenWidth - 150f) {
                            enemy.vx = -enemy.vx
                        }
                        
                        // Boss shooting patterns
                        if (currentTime - enemy.lastShotTime > 1000) {
                            // Triple shot
                            lasers.add(Laser(enemy.x, enemy.y + 70f, 0f, 15f, false, 15f, Color(0xFFFF5252), width = 5f, length = 20f))
                            lasers.add(Laser(enemy.x - 40f, enemy.y + 50f, -3f, 14f, false, 15f, Color(0xFFFF5252), width = 5f, length = 20f))
                            lasers.add(Laser(enemy.x + 40f, enemy.y + 50f, 3f, 14f, false, 15f, Color(0xFFFF5252), width = 5f, length = 20f))
                            
                            // Chance for ring fire
                            if (Math.random() < 0.4) {
                                for (angle in -2..2) {
                                    lasers.add(Laser(enemy.x, enemy.y + 70f, angle * 4f, 12f, false, 10f, Color(0xFFFFEA00), width = 4f, length = 18f))
                                }
                            }
                            enemy.lastShotTime = currentTime
                        }
                    }
                }
            }
        }

        // Move Lasers
        lasers.forEach { laser ->
            laser.x += laser.vx * speedMultiplier
            laser.y += laser.vy * speedMultiplier
        }

        // Move Powerups
        powerUps.forEach { powerUp ->
            powerUp.y += powerUp.vy * speedMultiplier
        }

        // Update Particles
        updateParticles()
    }

    private fun updateParticles() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx
            p.y += p.vy
            p.alpha -= p.decay
            p.size *= p.scaleDecay
            if (p.alpha <= 0f || p.size <= 0.5f) {
                iterator.remove()
            }
        }
    }

    private fun checkCollisions(currentTime: Long) {
        // 1. Player lasers vs Enemies
        val laserIter = lasers.iterator()
        while (laserIter.hasNext()) {
            val laser = laserIter.next()
            if (!laser.isPlayerLaser) continue

            val enemyIter = enemies.iterator()
            var laserHit = false
            while (enemyIter.hasNext()) {
                val enemy = enemyIter.next()
                
                // Circle-box or circle-circle approximation
                val dist = distance(laser.x, laser.y, enemy.x, enemy.y)
                if (dist < enemy.type.radius + laser.width) {
                    enemy.health -= laser.damage
                    laserHit = true
                    
                    // Hit spark particles
                    triggerHitSparks(laser.x, laser.y, laser.color)

                    if (enemy.health <= 0f) {
                        // Enemy destroyed
                        kills++
                        score += enemy.type.scoreValue
                        triggerExplosion(enemy.x, enemy.y, getEnemyColor(enemy.type))
                        soundManager?.playExplosion()

                        // Roll power-up drop (15% chance for regular, 100% for Boss)
                        if (enemy.type == EnemyType.BOSS) {
                            spawnPowerUp(enemy.x, enemy.y)
                            bossSpawned = false
                        } else if (Math.random() < 0.18) {
                            spawnPowerUp(enemy.x, enemy.y)
                        }

                        enemyIter.remove()
                    }
                    break
                }
            }
            if (laserHit) {
                laserIter.remove()
            }
        }

        // 2. Enemy lasers vs Player
        val laserIter2 = lasers.iterator()
        while (laserIter2.hasNext()) {
            val laser = laserIter2.next()
            if (laser.isPlayerLaser) continue

            val dist = distance(laser.x, laser.y, player.x, player.y)
            if (dist < player.radius + laser.width) {
                damagePlayer(laser.damage, currentTime)
                triggerHitSparks(laser.x, laser.y, laser.color)
                laserIter2.remove()
            }
        }

        // 3. Enemies vs Player (Collision crash)
        val enemyIter = enemies.iterator()
        while (enemyIter.hasNext()) {
            val enemy = enemyIter.next()
            val dist = distance(enemy.x, enemy.y, player.x, player.y)
            if (dist < enemy.type.radius + player.radius) {
                // Instantly deal massive damage to player and destroy enemy (unless it's a boss)
                val crashDamage = if (enemy.type == EnemyType.BOSS) 50f else 30f
                damagePlayer(crashDamage, currentTime)
                
                triggerExplosion(enemy.x, enemy.y, getEnemyColor(enemy.type))
                soundManager?.playExplosion()
                
                if (enemy.type != EnemyType.BOSS) {
                    enemyIter.remove()
                }
            }
        }

        // 4. Powerups vs Player
        val powerIter = powerUps.iterator()
        while (powerIter.hasNext()) {
            val powerUp = powerIter.next()
            val dist = distance(powerUp.x, powerUp.y, player.x, player.y)
            if (dist < powerUp.radius + player.radius) {
                activatePowerUp(powerUp.type, currentTime)
                soundManager?.playPowerUp()
                
                // Flash particles on player
                triggerHitSparks(player.x, player.y, powerUp.type.color, count = 25)
                powerIter.remove()
            }
        }
    }

    private fun damagePlayer(damage: Float, currentTime: Long) {
        if (player.isShieldActive(currentTime)) {
            // Shield absorbs damage
            player.shield = (player.shield - damage).coerceAtLeast(0f)
            if (player.shield <= 0f) {
                player.shieldExpiry = 0L // Shield depleted
            }
            soundManager?.playPlayerHit()
        } else {
            player.health -= damage
            soundManager?.playPlayerHit()
            
            // Camera shake / hit flash trigger
            if (player.health <= 0f) {
                player.health = 0f
                isGameOver = true
                triggerExplosion(player.x, player.y, Color(0xFFFFEA00), count = 60)
                soundManager?.playGameOver()
            }
        }
    }

    private fun activatePowerUp(type: PowerUpType, currentTime: Long) {
        val duration = 8000L // 8 seconds
        when (type) {
            PowerUpType.SHIELD -> {
                player.shield = player.maxShield
                player.shieldExpiry = currentTime + duration
            }
            PowerUpType.DOUBLE_SHOT -> {
                player.doubleShotExpiry = currentTime + duration
                player.tripleShotExpiry = 0L // Override triple shot
            }
            PowerUpType.TRIPLE_SHOT -> {
                player.tripleShotExpiry = currentTime + duration
                player.doubleShotExpiry = 0L
            }
            PowerUpType.SLOW_MO -> {
                player.slowMoExpiry = currentTime + duration
            }
            PowerUpType.HEALTH -> {
                player.health = (player.health + 40f).coerceAtMost(player.maxHealth)
            }
        }
        score += 150 // Reward for collecting power-up
    }

    private fun spawnPowerUp(x: Float, y: Float) {
        val rand = Math.random()
        val type = when {
            rand < 0.20 -> PowerUpType.HEALTH
            rand < 0.45 -> PowerUpType.SHIELD
            rand < 0.70 -> PowerUpType.DOUBLE_SHOT
            rand < 0.85 -> PowerUpType.TRIPLE_SHOT
            else -> PowerUpType.SLOW_MO
        }
        powerUps.add(PowerUp(type, x, y, 4f))
    }

    private fun cleanUp() {
        // Remove off-screen lasers
        lasers.removeAll { it.y < -50f || it.y > screenHeight + 50f || it.x < -50f || it.x > screenWidth + 50f }
        // Remove off-screen enemies
        enemies.removeAll { it.y > screenHeight + 100f }
        // Remove off-screen powerups
        powerUps.removeAll { it.y > screenHeight + 50f }
    }

    fun triggerExplosion(x: Float, y: Float, color: Color, count: Int = 30) {
        for (i in 0 until count) {
            val angle = (Math.random() * 2 * Math.PI)
            val speed = (Math.random() * 8 + 2).toFloat()
            val vx = (cos(angle) * speed).toFloat()
            val vy = (sin(angle) * speed).toFloat()
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = color,
                    size = (Math.random() * 12 + 6).toFloat(),
                    alpha = 1.0f,
                    decay = (Math.random() * 0.03 + 0.015).toFloat()
                )
            )
        }
    }

    private fun triggerHitSparks(x: Float, y: Float, color: Color, count: Int = 10) {
        for (i in 0 until count) {
            val vx = (Math.random() * 6 - 3).toFloat()
            val vy = (Math.random() * 6 - 3).toFloat()
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = color,
                    size = (Math.random() * 6 + 3).toFloat(),
                    alpha = 1.0f,
                    decay = (Math.random() * 0.05 + 0.04).toFloat()
                )
            )
        }
    }

    private fun getEnemyColor(type: EnemyType): Color {
        return when (type) {
            EnemyType.SCOUT -> Color(0xFFFF7043)
            EnemyType.BOMBER -> Color(0xFFFF1744)
            EnemyType.HUNTER -> Color(0xFFD500F9)
            EnemyType.BOSS -> Color(0xFFFFEA00)
        }
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2))
    }
}
