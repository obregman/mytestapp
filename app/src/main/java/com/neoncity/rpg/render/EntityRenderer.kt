package com.neoncity.rpg.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.neoncity.rpg.entity.NPC
import com.neoncity.rpg.entity.Player

/**
 * Renders game entities (player, NPCs) in isometric view.
 * Entity sprites are rendered at low resolution for pixel art effect.
 * Entity labels (names, markers) can be rendered separately at high resolution.
 */
class EntityRenderer {

    companion object {
        const val TILE_WIDTH = 16
        const val TILE_HEIGHT = 8
    }

    // Scale factors for high-res label rendering
    private var scaleX = 1f
    private var scaleY = 1f
    private var isHighRes = false
    private val baseTextSize = 6f

    private val bodyPaint = Paint().apply {
        isAntiAlias = false
        style = Paint.Style.FILL
    }

    private val outlinePaint = Paint().apply {
        isAntiAlias = false
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = Color.rgb(20, 20, 30)
    }

    private val shadowPaint = Paint().apply {
        isAntiAlias = false
        color = Color.argb(80, 0, 0, 0)
    }

    private val highlightPaint = Paint().apply {
        isAntiAlias = false
        color = Color.rgb(0, 255, 255)
        strokeWidth = 1f
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = baseTextSize
        textAlign = Paint.Align.CENTER
    }

    /**
     * Set screen size for high-resolution label rendering.
     */
    fun setScreenSize(actualWidth: Int, actualHeight: Int, gameWidth: Int, gameHeight: Int) {
        scaleX = actualWidth.toFloat() / gameWidth
        scaleY = actualHeight.toFloat() / gameHeight
        isHighRes = true
        textPaint.textSize = baseTextSize * minOf(scaleX, scaleY)
    }

    /**
     * Reset to low-resolution mode.
     */
    fun setLowResMode() {
        scaleX = 1f
        scaleY = 1f
        isHighRes = false
        textPaint.textSize = baseTextSize
    }

    // Helper to convert low-res coordinates to high-res screen coordinates
    private fun toScreenX(x: Float) = if (isHighRes) x * scaleX else x
    private fun toScreenY(y: Float) = if (isHighRes) y * scaleY else y

    /**
     * Render entity sprites (low-res pixel art mode).
     * Labels are skipped here if they will be rendered at high-res separately.
     */
    fun render(canvas: Canvas, entity: Any, cameraX: Float, cameraY: Float, screenWidth: Int, screenHeight: Int, skipLabels: Boolean = false) {
        when (entity) {
            is Player -> renderPlayer(canvas, entity, cameraX, cameraY, screenWidth, screenHeight)
            is NPC -> renderNPC(canvas, entity, cameraX, cameraY, screenWidth, screenHeight, skipLabels)
        }
    }

    /**
     * Render entity labels at high resolution (names, quest markers).
     * Call this on the screen canvas after rendering scaled-up game bitmap.
     */
    fun renderLabels(canvas: Canvas, entity: Any, cameraX: Float, cameraY: Float, gameWidth: Int, gameHeight: Int) {
        when (entity) {
            is NPC -> renderNPCLabels(canvas, entity, cameraX, cameraY, gameWidth, gameHeight)
        }
    }

    private fun worldToScreen(worldX: Float, worldY: Float, cameraX: Float, cameraY: Float, screenWidth: Int, screenHeight: Int): Pair<Float, Float> {
        val relX = worldX - cameraX
        val relY = worldY - cameraY
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 3f

        val screenX = centerX + (relX - relY) * (TILE_WIDTH / 2f)
        val screenY = centerY + (relX + relY) * (TILE_HEIGHT / 2f)
        return Pair(screenX, screenY)
    }

    private fun renderPlayer(canvas: Canvas, player: Player, cameraX: Float, cameraY: Float, screenWidth: Int, screenHeight: Int) {
        val (screenX, screenY) = worldToScreen(player.x, player.y, cameraX, cameraY, screenWidth, screenHeight)

        // Draw shadow
        canvas.drawOval(screenX - 4, screenY - 1, screenX + 4, screenY + 2, shadowPaint)

        // Animation frame
        val animOffset = if (player.isMoving) {
            ((System.currentTimeMillis() / 150) % 4).toInt()
        } else {
            0
        }
        val bobY = when (animOffset) {
            1, 3 -> -1f
            else -> 0f
        }

        // Draw player sprite (cyberpunk style)
        // Body
        bodyPaint.color = Color.rgb(40, 40, 60)
        canvas.drawRect(screenX - 3, screenY - 12 + bobY, screenX + 3, screenY - 4 + bobY, bodyPaint)

        // Jacket/coat highlight
        bodyPaint.color = player.clothingColor
        canvas.drawRect(screenX - 3, screenY - 10 + bobY, screenX + 3, screenY - 6 + bobY, bodyPaint)

        // Head
        bodyPaint.color = Color.rgb(220, 180, 160)
        canvas.drawRect(screenX - 2, screenY - 16 + bobY, screenX + 2, screenY - 12 + bobY, bodyPaint)

        // Hair
        bodyPaint.color = player.hairColor
        canvas.drawRect(screenX - 2, screenY - 17 + bobY, screenX + 2, screenY - 15 + bobY, bodyPaint)

        // Legs based on facing/animation
        bodyPaint.color = Color.rgb(30, 30, 45)
        when (player.facing) {
            0 -> { // North
                canvas.drawRect(screenX - 2, screenY - 4 + bobY, screenX, screenY + bobY, bodyPaint)
                canvas.drawRect(screenX, screenY - 4 + bobY, screenX + 2, screenY + bobY, bodyPaint)
            }
            1 -> { // East
                val legOffset = if (animOffset == 1) 1 else if (animOffset == 3) -1 else 0
                canvas.drawRect(screenX - 1, screenY - 4 + bobY, screenX + 1, screenY + legOffset + bobY, bodyPaint)
                canvas.drawRect(screenX, screenY - 4 + bobY, screenX + 2, screenY - legOffset + bobY, bodyPaint)
            }
            2 -> { // South
                canvas.drawRect(screenX - 2, screenY - 4 + bobY, screenX, screenY + bobY, bodyPaint)
                canvas.drawRect(screenX, screenY - 4 + bobY, screenX + 2, screenY + bobY, bodyPaint)
            }
            3 -> { // West
                val legOffset = if (animOffset == 1) 1 else if (animOffset == 3) -1 else 0
                canvas.drawRect(screenX - 2, screenY - 4 + bobY, screenX, screenY + legOffset + bobY, bodyPaint)
                canvas.drawRect(screenX - 1, screenY - 4 + bobY, screenX + 1, screenY - legOffset + bobY, bodyPaint)
            }
        }

        // Cybernetic eye glow
        highlightPaint.color = Color.rgb(0, 200, 255)
        canvas.drawPoint(screenX + 1, screenY - 14 + bobY, highlightPaint)
    }

    private fun renderNPC(canvas: Canvas, npc: NPC, cameraX: Float, cameraY: Float, screenWidth: Int, screenHeight: Int, skipLabels: Boolean = false) {
        val (screenX, screenY) = worldToScreen(npc.x, npc.y, cameraX, cameraY, screenWidth, screenHeight)

        // Draw shadow
        canvas.drawOval(screenX - 4, screenY - 1, screenX + 4, screenY + 2, shadowPaint)

        // NPC idle animation
        val idleOffset = ((System.currentTimeMillis() / 500) % 2).toInt()
        val bobY = if (idleOffset == 1) -0.5f else 0f

        // Body
        bodyPaint.color = npc.bodyColor
        canvas.drawRect(screenX - 3, screenY - 12 + bobY, screenX + 3, screenY - 4 + bobY, bodyPaint)

        // Clothing accent
        bodyPaint.color = npc.accentColor
        canvas.drawRect(screenX - 3, screenY - 10 + bobY, screenX + 3, screenY - 8 + bobY, bodyPaint)

        // Head
        bodyPaint.color = npc.skinColor
        canvas.drawRect(screenX - 2, screenY - 16 + bobY, screenX + 2, screenY - 12 + bobY, bodyPaint)

        // Hair
        bodyPaint.color = npc.hairColor
        canvas.drawRect(screenX - 2, screenY - 17 + bobY, screenX + 2, screenY - 15 + bobY, bodyPaint)

        // Legs
        bodyPaint.color = Color.rgb(35, 35, 50)
        canvas.drawRect(screenX - 2, screenY - 4 + bobY, screenX, screenY + bobY, bodyPaint)
        canvas.drawRect(screenX, screenY - 4 + bobY, screenX + 2, screenY + bobY, bodyPaint)

        // Interaction indicator (visual circle - keep in low res for pixel art)
        if (npc.isPlayerNear) {
            val pulseAlpha = ((System.currentTimeMillis() / 100) % 10).toInt() * 25
            highlightPaint.color = Color.argb(150 + pulseAlpha / 3, 0, 255, 255)
            canvas.drawCircle(screenX, screenY - 22, 3f, highlightPaint)
        }

        // Labels (name, quest markers) are rendered in high-res pass if skipLabels is true
        if (!skipLabels) {
            // Exclamation mark if has quest
            if (npc.hasQuest) {
                val bounce = if (!npc.isPlayerNear) ((System.currentTimeMillis() / 300) % 3).toInt() else 0
                textPaint.color = Color.rgb(255, 200, 0)
                val markerY = if (npc.isPlayerNear) screenY - 20 else screenY - 19 - bounce
                canvas.drawText("!", screenX, markerY, textPaint)
            }

            // Name tag when nearby
            if (npc.isPlayerNear) {
                textPaint.color = Color.WHITE
                canvas.drawText(npc.name, screenX, screenY - 25, textPaint)
            }
        }
    }

    /**
     * Render NPC labels at high resolution.
     */
    private fun renderNPCLabels(canvas: Canvas, npc: NPC, cameraX: Float, cameraY: Float, gameWidth: Int, gameHeight: Int) {
        val (gameX, gameY) = worldToScreen(npc.x, npc.y, cameraX, cameraY, gameWidth, gameHeight)

        // Convert to high-res screen coordinates
        val screenX = toScreenX(gameX)
        val screenY = toScreenY(gameY)

        // Quest marker
        if (npc.hasQuest) {
            val bounce = if (!npc.isPlayerNear) toScreenY(((System.currentTimeMillis() / 300) % 3).toFloat()) else 0f
            textPaint.color = Color.rgb(255, 200, 0)
            val markerY = if (npc.isPlayerNear) screenY - toScreenY(20f) else screenY - toScreenY(19f) - bounce
            canvas.drawText("!", screenX, markerY, textPaint)
        }

        // Name tag when nearby
        if (npc.isPlayerNear) {
            textPaint.color = Color.WHITE
            canvas.drawText(npc.name, screenX, screenY - toScreenY(25f), textPaint)
        }
    }
}
