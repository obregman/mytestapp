package com.neoncity.rpg.entity

import android.graphics.Color

/**
 * Player character with stats, movement, and customization.
 */
class Player(
    var name: String,
    var x: Float,
    var y: Float
) {
    // Movement
    var velocityX: Float = 0f
    var velocityY: Float = 0f
    var facing: Int = 2  // 0=North, 1=East, 2=South, 3=West
    val isMoving: Boolean get() = velocityX != 0f || velocityY != 0f || hasMoveTarget

    // Click-to-move target
    private var targetX: Float? = null
    private var targetY: Float? = null
    private val hasMoveTarget: Boolean get() = targetX != null

    // Stats
    var level: Int = 1
    var experience: Int = 0
    var experienceToLevel: Int = 100

    var health: Int = 100
    var maxHealth: Int = 100
    var energy: Int = 50
    var maxEnergy: Int = 50

    // RPG attributes
    var strength: Int = 5
    var agility: Int = 5
    var intelligence: Int = 5
    var charisma: Int = 5

    // Appearance (pixel colors)
    var hairColor: Int = Color.rgb(40, 30, 20)      // Dark brown
    var clothingColor: Int = Color.rgb(80, 40, 120) // Purple jacket

    // Movement speed
    private val baseSpeed: Float = 0.06f
    val speed: Float get() = baseSpeed * (1f + agility * 0.02f)

    fun update() {
        // Handle click-to-move
        if (hasMoveTarget) {
            val tx = targetX!!
            val ty = targetY!!
            val dx = tx - x
            val dy = ty - y
            val dist = kotlin.math.sqrt(dx * dx + dy * dy)

            if (dist < 0.1f) {
                // Reached target
                targetX = null
                targetY = null
                velocityX = 0f
                velocityY = 0f
            } else {
                // Move towards target
                val moveSpeed = speed
                velocityX = (dx / dist) * moveSpeed
                velocityY = (dy / dist) * moveSpeed

                // Update facing direction
                if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                    facing = if (dx > 0) 1 else 3
                } else {
                    facing = if (dy > 0) 2 else 0
                }
            }
        }

        // Apply velocity
        x += velocityX
        y += velocityY

        // Clamp position (basic boundary)
        x = x.coerceIn(0f, 100f)
        y = y.coerceIn(0f, 100f)

        // Energy regeneration
        if (!isMoving && energy < maxEnergy) {
            energy = (energy + 0.01f).toInt().coerceAtMost(maxEnergy)
        }
    }

    fun setMoveTarget(worldX: Float, worldY: Float) {
        targetX = worldX
        targetY = worldY
    }

    fun clearMoveTarget() {
        targetX = null
        targetY = null
        velocityX = 0f
        velocityY = 0f
    }

    fun addExperience(amount: Int) {
        experience += amount
        while (experience >= experienceToLevel) {
            levelUp()
        }
    }

    private fun levelUp() {
        experience -= experienceToLevel
        level++
        experienceToLevel = (experienceToLevel * 1.5f).toInt()

        // Increase stats
        maxHealth += 10
        health = maxHealth
        maxEnergy += 5
        energy = maxEnergy
    }

    fun takeDamage(amount: Int) {
        health = (health - amount).coerceAtLeast(0)
    }

    fun heal(amount: Int) {
        health = (health + amount).coerceAtMost(maxHealth)
    }

    fun useEnergy(amount: Int): Boolean {
        return if (energy >= amount) {
            energy -= amount
            true
        } else {
            false
        }
    }
}
