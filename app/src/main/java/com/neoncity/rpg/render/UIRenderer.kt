package com.neoncity.rpg.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.neoncity.rpg.game.GameState

/**
 * Renders all UI elements including HUD, menus, and dialogue boxes.
 * Supports both low-res (pixel art) and high-res (crisp text) rendering modes.
 */
class UIRenderer(private val gameWidth: Int, private val gameHeight: Int) {

    // Scale factors for high-res rendering (set via setScreenSize)
    private var scaleX = 1f
    private var scaleY = 1f
    private var screenWidth = gameWidth
    private var screenHeight = gameHeight
    private var isHighRes = false

    // Base text sizes (at game resolution 320x180)
    private val baseTextSize = 8f
    private val baseTitleSize = 16f
    private val baseSmallTextSize = 6f

    private val backgroundPaint = Paint().apply {
        color = Color.argb(200, 10, 10, 20)
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint().apply {
        color = Color.rgb(0, 200, 200)
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = baseTextSize
        isAntiAlias = true
    }

    private val titlePaint = Paint().apply {
        color = Color.rgb(0, 255, 255)
        textSize = baseTitleSize
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val accentPaint = Paint().apply {
        color = Color.rgb(255, 0, 255)
        style = Paint.Style.FILL
    }

    private val buttonPaint = Paint().apply {
        color = Color.rgb(40, 40, 60)
        style = Paint.Style.FILL
    }

    private val healthPaint = Paint().apply {
        color = Color.rgb(255, 50, 50)
        style = Paint.Style.FILL
    }

    private val energyPaint = Paint().apply {
        color = Color.rgb(0, 200, 255)
        style = Paint.Style.FILL
    }

    /**
     * Set screen size for high-resolution rendering.
     * Call this with actual screen dimensions to enable crisp text.
     */
    fun setScreenSize(actualWidth: Int, actualHeight: Int) {
        screenWidth = actualWidth
        screenHeight = actualHeight
        scaleX = actualWidth.toFloat() / gameWidth
        scaleY = actualHeight.toFloat() / gameHeight
        isHighRes = true
        updatePaintSizes()
    }

    /**
     * Reset to low-resolution mode (for game bitmap rendering).
     */
    fun setLowResMode() {
        screenWidth = gameWidth
        screenHeight = gameHeight
        scaleX = 1f
        scaleY = 1f
        isHighRes = false
        updatePaintSizes()
    }

    private fun updatePaintSizes() {
        val scale = if (isHighRes) minOf(scaleX, scaleY) else 1f
        textPaint.textSize = baseTextSize * scale
        titlePaint.textSize = baseTitleSize * scale
        borderPaint.strokeWidth = if (isHighRes) 2f else 1f
    }

    // Helper functions to scale coordinates
    private fun sx(x: Float) = x * scaleX
    private fun sy(y: Float) = y * scaleY
    private fun textSize(size: Float) = size * if (isHighRes) minOf(scaleX, scaleY) else 1f

    fun renderTitleScreen(canvas: Canvas) {
        // Title
        titlePaint.textSize = textSize(24f)
        titlePaint.color = Color.rgb(0, 255, 255)
        canvas.drawText("NEON CITY", screenWidth / 2f, sy(50f), titlePaint)

        // Subtitle
        titlePaint.textSize = textSize(8f)
        titlePaint.color = Color.rgb(255, 0, 255)
        canvas.drawText("A Cyberpunk RPG", screenWidth / 2f, sy(65f), titlePaint)

        // Menu options
        val centerX = screenWidth / 2f
        val menuY = screenHeight / 2f

        // New Game button
        drawButton(canvas, centerX - sx(40f), menuY - sy(10f), sx(80f), sy(20f), "NEW GAME", true)

        // Continue button
        drawButton(canvas, centerX - sx(40f), menuY + sy(15f), sx(80f), sy(20f), "CONTINUE", false)

        // Version
        textPaint.color = Color.rgb(80, 80, 100)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("v1.0", screenWidth / 2f, screenHeight - sy(10f), textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawButton(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, text: String, selected: Boolean) {
        val rect = RectF(x, y, x + width, y + height)

        // Button background
        buttonPaint.color = if (selected) Color.rgb(60, 60, 90) else Color.rgb(40, 40, 60)
        canvas.drawRect(rect, buttonPaint)

        // Border
        borderPaint.color = if (selected) Color.rgb(0, 255, 255) else Color.rgb(80, 80, 100)
        canvas.drawRect(rect, borderPaint)

        // Text
        textPaint.color = if (selected) Color.WHITE else Color.rgb(150, 150, 150)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(text, x + width / 2, y + height / 2 + sy(3f), textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    fun renderHUD(canvas: Canvas, gameState: GameState) {
        // Top-left: Player stats
        renderPlayerStats(canvas, gameState)

        // Top-right: Time and location
        renderTimeAndLocation(canvas, gameState)

        // Bottom-left: Virtual joystick area indicator
        renderJoystickIndicator(canvas)

        // Bottom-right: Action buttons
        renderActionButtons(canvas, gameState)

        // Mini-map (optional)
        // renderMiniMap(canvas, gameState)
    }

    private fun renderPlayerStats(canvas: Canvas, gameState: GameState) {
        val player = gameState.player

        // Background panel
        backgroundPaint.color = Color.argb(180, 10, 10, 25)
        canvas.drawRect(sx(5f), sy(5f), sx(80f), sy(35f), backgroundPaint)
        canvas.drawRect(sx(5f), sy(5f), sx(80f), sy(35f), borderPaint)

        // Player name
        textPaint.color = Color.rgb(0, 255, 255)
        canvas.drawText(player.name, sx(8f), sy(14f), textPaint)

        // Health bar
        val healthPercent = player.health / player.maxHealth.toFloat()
        canvas.drawRect(sx(8f), sy(17f), sx(75f), sy(21f), buttonPaint)
        healthPaint.color = Color.rgb(255, 50, 50)
        canvas.drawRect(sx(8f), sy(17f), sx(8f + 67f * healthPercent), sy(21f), healthPaint)

        // Energy bar
        val energyPercent = player.energy / player.maxEnergy.toFloat()
        canvas.drawRect(sx(8f), sy(24f), sx(75f), sy(28f), buttonPaint)
        canvas.drawRect(sx(8f), sy(24f), sx(8f + 67f * energyPercent), sy(28f), energyPaint)

        // Level
        textPaint.color = Color.WHITE
        canvas.drawText("LV ${player.level}", sx(8f), sy(34f), textPaint)
    }

    private fun renderTimeAndLocation(canvas: Canvas, gameState: GameState) {
        // Background panel
        backgroundPaint.color = Color.argb(180, 10, 10, 25)
        canvas.drawRect(screenWidth - sx(85f), sy(5f), screenWidth - sx(5f), sy(30f), backgroundPaint)
        canvas.drawRect(screenWidth - sx(85f), sy(5f), screenWidth - sx(5f), sy(30f), borderPaint)

        // Location
        textPaint.color = Color.rgb(255, 0, 255)
        canvas.drawText(gameState.currentDistrict.name, screenWidth - sx(82f), sy(14f), textPaint)

        // Time
        val hours = gameState.gameTime.toInt()
        val minutes = ((gameState.gameTime - hours) * 60).toInt()
        val timeStr = String.format("%02d:%02d", hours, minutes)
        val period = if (hours < 12) "AM" else "PM"

        textPaint.color = if (gameState.currentDistrict.isNight) Color.rgb(100, 100, 200) else Color.rgb(255, 200, 100)
        canvas.drawText("$timeStr $period", screenWidth - sx(82f), sy(24f), textPaint)
    }

    private fun renderJoystickIndicator(canvas: Canvas) {
        // Joystick base circle
        val centerX = sx(50f)
        val centerY = screenHeight - sy(50f)
        val baseRadius = sx(35f)
        val innerRadius = sx(12f)

        accentPaint.color = Color.argb(60, 0, 200, 200)
        canvas.drawCircle(centerX, centerY, baseRadius, accentPaint)

        borderPaint.color = Color.argb(100, 0, 255, 255)
        canvas.drawCircle(centerX, centerY, baseRadius, borderPaint)

        // Inner joystick
        accentPaint.color = Color.argb(100, 0, 255, 255)
        canvas.drawCircle(centerX, centerY, innerRadius, accentPaint)
    }

    private fun renderActionButtons(canvas: Canvas, gameState: GameState) {
        // Inventory button
        drawActionButton(canvas, screenWidth - sx(60f), screenHeight - sy(30f), sx(25f), "INV")

        // Quest button
        drawActionButton(canvas, screenWidth - sx(30f), screenHeight - sy(30f), sx(25f), "QST")

        // Interact button (larger)
        val nearNPC = gameState.npcs.any { it.isPlayerNear }
        drawActionButton(canvas, screenWidth - sx(45f), screenHeight - sy(60f), sx(30f), "ACT", nearNPC)
    }

    private fun drawActionButton(canvas: Canvas, x: Float, y: Float, size: Float, label: String, highlight: Boolean = false) {
        val rect = RectF(x, y, x + size, y + size)

        buttonPaint.color = if (highlight) Color.rgb(60, 80, 80) else Color.rgb(40, 40, 60)
        canvas.drawRect(rect, buttonPaint)

        borderPaint.color = if (highlight) Color.rgb(0, 255, 200) else Color.rgb(0, 150, 150)
        canvas.drawRect(rect, borderPaint)

        textPaint.color = if (highlight) Color.WHITE else Color.rgb(150, 150, 150)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = textSize(6f)
        canvas.drawText(label, x + size / 2, y + size / 2 + sy(2f), textPaint)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = textSize(baseTextSize)
    }

    fun renderDialogue(canvas: Canvas, gameState: GameState) {
        val dialogue = gameState.activeDialogue ?: return

        // Dialogue box background
        val boxTop = screenHeight - sy(60f)
        backgroundPaint.color = Color.argb(230, 10, 10, 25)
        canvas.drawRect(sx(10f), boxTop, screenWidth - sx(10f), screenHeight - sy(10f), backgroundPaint)

        borderPaint.color = Color.rgb(0, 200, 200)
        canvas.drawRect(sx(10f), boxTop, screenWidth - sx(10f), screenHeight - sy(10f), borderPaint)

        // NPC name
        accentPaint.color = Color.rgb(0, 200, 200)
        val nameWidth = textPaint.measureText(dialogue.npc.name) + sx(10f)
        canvas.drawRect(sx(15f), boxTop - sy(10f), sx(15f) + nameWidth, boxTop, accentPaint)
        textPaint.color = Color.BLACK
        canvas.drawText(dialogue.npc.name, sx(20f), boxTop - sy(2f), textPaint)

        // Dialogue text
        textPaint.color = Color.WHITE
        val text = dialogue.getCurrentText()
        val words = text.split(" ")
        var currentLine = ""
        var lineY = boxTop + sy(12f)
        val maxWidth = screenWidth - sx(40f)
        val lineHeight = sy(10f)

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (textPaint.measureText(testLine) < maxWidth) {
                currentLine = testLine
            } else {
                canvas.drawText(currentLine, sx(15f), lineY, textPaint)
                lineY += lineHeight
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, sx(15f), lineY, textPaint)
        }

        // Response options
        val responses = dialogue.getResponses()
        val responseStartY = boxTop + sy(30f)

        textPaint.color = Color.rgb(0, 255, 255)
        for ((index, response) in responses.withIndex()) {
            val responseY = responseStartY + index * sy(12f)
            canvas.drawText("> $response", sx(20f), responseY, textPaint)
        }
    }

    fun renderInventory(canvas: Canvas, gameState: GameState) {
        // Inventory panel
        val panelLeft = sx(30f)
        val panelTop = sy(20f)
        val panelWidth = screenWidth - sx(60f)
        val panelHeight = screenHeight - sy(40f)

        // Background
        backgroundPaint.color = Color.argb(240, 10, 10, 25)
        canvas.drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, backgroundPaint)
        canvas.drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, borderPaint)

        // Title
        titlePaint.textSize = textSize(10f)
        titlePaint.color = Color.rgb(0, 255, 255)
        canvas.drawText("INVENTORY", screenWidth / 2f, panelTop + sy(15f), titlePaint)

        // Inventory grid
        val gridStartX = panelLeft + sx(10f)
        val gridStartY = panelTop + sy(25f)
        val cellSize = sx(20f)
        val cellGap = sx(2f)
        val cols = 8
        val rows = 4

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val cellX = gridStartX + col * (cellSize + cellGap)
                val cellY = gridStartY + row * (cellSize + cellGap)

                buttonPaint.color = Color.rgb(30, 30, 45)
                canvas.drawRect(cellX, cellY, cellX + cellSize, cellY + cellSize, buttonPaint)
                canvas.drawRect(cellX, cellY, cellX + cellSize, cellY + cellSize, borderPaint)

                // Draw item if exists
                val itemIndex = row * cols + col
                if (itemIndex < gameState.inventory.items.size) {
                    val item = gameState.inventory.items[itemIndex]
                    textPaint.textAlign = Paint.Align.CENTER
                    textPaint.color = item.color
                    canvas.drawText(item.symbol, cellX + cellSize / 2, cellY + cellSize / 2 + sy(3f), textPaint)
                    textPaint.textAlign = Paint.Align.LEFT
                }
            }
        }

        // Credits display
        textPaint.color = Color.rgb(255, 200, 0)
        canvas.drawText("Credits: ${gameState.inventory.credits}", panelLeft + sx(10f), panelTop + panelHeight - sy(10f), textPaint)

        // Close hint
        textPaint.color = Color.rgb(100, 100, 120)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Tap outside to close", screenWidth / 2f, panelTop + panelHeight - sy(10f), textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    fun renderQuestLog(canvas: Canvas, gameState: GameState) {
        // Quest log panel
        val panelLeft = sx(30f)
        val panelTop = sy(20f)
        val panelWidth = screenWidth - sx(60f)
        val panelHeight = screenHeight - sy(40f)

        // Background
        backgroundPaint.color = Color.argb(240, 10, 10, 25)
        canvas.drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, backgroundPaint)
        canvas.drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, borderPaint)

        // Title
        titlePaint.textSize = textSize(10f)
        titlePaint.color = Color.rgb(255, 0, 255)
        canvas.drawText("QUEST LOG", screenWidth / 2f, panelTop + sy(15f), titlePaint)

        // Quest list
        var questY = panelTop + sy(30f)
        val activeQuests = gameState.questManager.getActiveQuests()
        val lineHeight = sy(10f)

        if (activeQuests.isEmpty()) {
            textPaint.color = Color.rgb(100, 100, 120)
            canvas.drawText("No active quests", panelLeft + sx(15f), questY, textPaint)
        } else {
            for (quest in activeQuests) {
                // Quest title
                textPaint.color = Color.rgb(255, 200, 0)
                canvas.drawText(quest.title, panelLeft + sx(15f), questY, textPaint)
                questY += lineHeight

                // Quest objectives
                for (objective in quest.objectives) {
                    textPaint.color = if (objective.completed) Color.rgb(0, 200, 0) else Color.rgb(150, 150, 150)
                    val marker = if (objective.completed) "[X]" else "[ ]"
                    canvas.drawText("$marker ${objective.description}", panelLeft + sx(20f), questY, textPaint)
                    questY += lineHeight
                }
                questY += sy(5f)
            }
        }

        // Close hint
        textPaint.color = Color.rgb(100, 100, 120)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Tap outside to close", screenWidth / 2f, panelTop + panelHeight - sy(10f), textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    fun renderPauseMenu(canvas: Canvas) {
        // Dim overlay
        backgroundPaint.color = Color.argb(180, 0, 0, 0)
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), backgroundPaint)

        // Pause text
        titlePaint.textSize = textSize(16f)
        titlePaint.color = Color.rgb(0, 255, 255)
        canvas.drawText("PAUSED", screenWidth / 2f, screenHeight / 2f, titlePaint)

        textPaint.color = Color.rgb(150, 150, 150)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Tap to resume", screenWidth / 2f, screenHeight / 2f + sy(20f), textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }
}
