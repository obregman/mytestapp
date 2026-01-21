package com.neoncity.rpg.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.neoncity.rpg.game.GameState

/**
 * Renders all UI elements including HUD, menus, and dialogue boxes.
 */
class UIRenderer(private val screenWidth: Int, private val screenHeight: Int) {

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
        textSize = 8f
        isAntiAlias = false
    }

    private val titlePaint = Paint().apply {
        color = Color.rgb(0, 255, 255)
        textSize = 16f
        isAntiAlias = false
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

    fun renderTitleScreen(canvas: Canvas) {
        // Title
        titlePaint.textSize = 24f
        titlePaint.color = Color.rgb(0, 255, 255)
        canvas.drawText("NEON CITY", screenWidth / 2f, 50f, titlePaint)

        // Subtitle
        titlePaint.textSize = 8f
        titlePaint.color = Color.rgb(255, 0, 255)
        canvas.drawText("A Cyberpunk RPG", screenWidth / 2f, 65f, titlePaint)

        // Menu options
        val centerX = screenWidth / 2f
        val menuY = screenHeight / 2f

        // New Game button
        drawButton(canvas, centerX - 40, menuY - 10, 80f, 20f, "NEW GAME", true)

        // Continue button
        drawButton(canvas, centerX - 40, menuY + 15, 80f, 20f, "CONTINUE", false)

        // Version
        textPaint.color = Color.rgb(80, 80, 100)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("v1.0", screenWidth / 2f, screenHeight - 10f, textPaint)
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
        canvas.drawText(text, x + width / 2, y + height / 2 + 3, textPaint)
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
        canvas.drawRect(5f, 5f, 80f, 35f, backgroundPaint)
        canvas.drawRect(5f, 5f, 80f, 35f, borderPaint)

        // Player name
        textPaint.color = Color.rgb(0, 255, 255)
        canvas.drawText(player.name, 8f, 14f, textPaint)

        // Health bar
        val healthPercent = player.health / player.maxHealth.toFloat()
        canvas.drawRect(8f, 17f, 75f, 21f, buttonPaint)
        healthPaint.color = Color.rgb(255, 50, 50)
        canvas.drawRect(8f, 17f, 8f + 67f * healthPercent, 21f, healthPaint)

        // Energy bar
        val energyPercent = player.energy / player.maxEnergy.toFloat()
        canvas.drawRect(8f, 24f, 75f, 28f, buttonPaint)
        canvas.drawRect(8f, 24f, 8f + 67f * energyPercent, 28f, energyPaint)

        // Level
        textPaint.color = Color.WHITE
        canvas.drawText("LV ${player.level}", 8f, 34f, textPaint)
    }

    private fun renderTimeAndLocation(canvas: Canvas, gameState: GameState) {
        // Background panel
        backgroundPaint.color = Color.argb(180, 10, 10, 25)
        canvas.drawRect(screenWidth - 85f, 5f, screenWidth - 5f, 30f, backgroundPaint)
        canvas.drawRect(screenWidth - 85f, 5f, screenWidth - 5f, 30f, borderPaint)

        // Location
        textPaint.color = Color.rgb(255, 0, 255)
        canvas.drawText(gameState.currentDistrict.name, screenWidth - 82f, 14f, textPaint)

        // Time
        val hours = gameState.gameTime.toInt()
        val minutes = ((gameState.gameTime - hours) * 60).toInt()
        val timeStr = String.format("%02d:%02d", hours, minutes)
        val period = if (hours < 12) "AM" else "PM"

        textPaint.color = if (gameState.currentDistrict.isNight) Color.rgb(100, 100, 200) else Color.rgb(255, 200, 100)
        canvas.drawText("$timeStr $period", screenWidth - 82f, 24f, textPaint)
    }

    private fun renderJoystickIndicator(canvas: Canvas) {
        // Joystick base circle
        val centerX = 50f
        val centerY = screenHeight - 50f

        accentPaint.color = Color.argb(60, 0, 200, 200)
        canvas.drawCircle(centerX, centerY, 35f, accentPaint)

        borderPaint.color = Color.argb(100, 0, 255, 255)
        canvas.drawCircle(centerX, centerY, 35f, borderPaint)

        // Inner joystick
        accentPaint.color = Color.argb(100, 0, 255, 255)
        canvas.drawCircle(centerX, centerY, 12f, accentPaint)
    }

    private fun renderActionButtons(canvas: Canvas, gameState: GameState) {
        // Inventory button
        drawActionButton(canvas, screenWidth - 60f, screenHeight - 30f, 25f, "INV")

        // Quest button
        drawActionButton(canvas, screenWidth - 30f, screenHeight - 30f, 25f, "QST")

        // Interact button (larger)
        val nearNPC = gameState.npcs.any { it.isPlayerNear }
        drawActionButton(canvas, screenWidth - 45f, screenHeight - 60f, 30f, "ACT", nearNPC)
    }

    private fun drawActionButton(canvas: Canvas, x: Float, y: Float, size: Float, label: String, highlight: Boolean = false) {
        val rect = RectF(x, y, x + size, y + size)

        buttonPaint.color = if (highlight) Color.rgb(60, 80, 80) else Color.rgb(40, 40, 60)
        canvas.drawRect(rect, buttonPaint)

        borderPaint.color = if (highlight) Color.rgb(0, 255, 200) else Color.rgb(0, 150, 150)
        canvas.drawRect(rect, borderPaint)

        textPaint.color = if (highlight) Color.WHITE else Color.rgb(150, 150, 150)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 6f
        canvas.drawText(label, x + size / 2, y + size / 2 + 2, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 8f
    }

    fun renderDialogue(canvas: Canvas, gameState: GameState) {
        val dialogue = gameState.activeDialogue ?: return

        // Dialogue box background
        val boxTop = screenHeight - 60f
        backgroundPaint.color = Color.argb(230, 10, 10, 25)
        canvas.drawRect(10f, boxTop, screenWidth - 10f, screenHeight - 10f, backgroundPaint)

        borderPaint.color = Color.rgb(0, 200, 200)
        canvas.drawRect(10f, boxTop, screenWidth - 10f, screenHeight - 10f, borderPaint)

        // NPC name
        accentPaint.color = Color.rgb(0, 200, 200)
        canvas.drawRect(15f, boxTop - 10f, 15f + dialogue.npc.name.length * 6f + 10f, boxTop, accentPaint)
        textPaint.color = Color.BLACK
        canvas.drawText(dialogue.npc.name, 20f, boxTop - 2f, textPaint)

        // Dialogue text
        textPaint.color = Color.WHITE
        val text = dialogue.getCurrentText()
        val words = text.split(" ")
        var currentLine = ""
        var lineY = boxTop + 12f
        val maxWidth = screenWidth - 40

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (textPaint.measureText(testLine) < maxWidth) {
                currentLine = testLine
            } else {
                canvas.drawText(currentLine, 15f, lineY, textPaint)
                lineY += 10f
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, 15f, lineY, textPaint)
        }

        // Response options
        val responses = dialogue.getResponses()
        val responseStartY = boxTop + 30f

        textPaint.color = Color.rgb(0, 255, 255)
        for ((index, response) in responses.withIndex()) {
            val responseY = responseStartY + index * 12f
            canvas.drawText("> $response", 20f, responseY, textPaint)
        }
    }

    fun renderInventory(canvas: Canvas, gameState: GameState) {
        // Inventory panel
        val panelLeft = 30f
        val panelTop = 20f
        val panelWidth = screenWidth - 60f
        val panelHeight = screenHeight - 40f

        // Background
        backgroundPaint.color = Color.argb(240, 10, 10, 25)
        canvas.drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, backgroundPaint)
        canvas.drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, borderPaint)

        // Title
        titlePaint.textSize = 10f
        titlePaint.color = Color.rgb(0, 255, 255)
        canvas.drawText("INVENTORY", screenWidth / 2f, panelTop + 15f, titlePaint)

        // Inventory grid
        val gridStartX = panelLeft + 10f
        val gridStartY = panelTop + 25f
        val cellSize = 20f
        val cols = 8
        val rows = 4

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val cellX = gridStartX + col * (cellSize + 2)
                val cellY = gridStartY + row * (cellSize + 2)

                buttonPaint.color = Color.rgb(30, 30, 45)
                canvas.drawRect(cellX, cellY, cellX + cellSize, cellY + cellSize, buttonPaint)
                canvas.drawRect(cellX, cellY, cellX + cellSize, cellY + cellSize, borderPaint)

                // Draw item if exists
                val itemIndex = row * cols + col
                if (itemIndex < gameState.inventory.items.size) {
                    val item = gameState.inventory.items[itemIndex]
                    textPaint.textAlign = Paint.Align.CENTER
                    textPaint.color = item.color
                    canvas.drawText(item.symbol, cellX + cellSize / 2, cellY + cellSize / 2 + 3, textPaint)
                    textPaint.textAlign = Paint.Align.LEFT
                }
            }
        }

        // Credits display
        textPaint.color = Color.rgb(255, 200, 0)
        canvas.drawText("Credits: ${gameState.inventory.credits}", panelLeft + 10f, panelTop + panelHeight - 10f, textPaint)

        // Close hint
        textPaint.color = Color.rgb(100, 100, 120)
        canvas.drawText("Tap outside to close", screenWidth / 2f - 35f, panelTop + panelHeight - 10f, textPaint)
    }

    fun renderQuestLog(canvas: Canvas, gameState: GameState) {
        // Quest log panel
        val panelLeft = 30f
        val panelTop = 20f
        val panelWidth = screenWidth - 60f
        val panelHeight = screenHeight - 40f

        // Background
        backgroundPaint.color = Color.argb(240, 10, 10, 25)
        canvas.drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, backgroundPaint)
        canvas.drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, borderPaint)

        // Title
        titlePaint.textSize = 10f
        titlePaint.color = Color.rgb(255, 0, 255)
        canvas.drawText("QUEST LOG", screenWidth / 2f, panelTop + 15f, titlePaint)

        // Quest list
        var questY = panelTop + 30f
        val activeQuests = gameState.questManager.getActiveQuests()

        if (activeQuests.isEmpty()) {
            textPaint.color = Color.rgb(100, 100, 120)
            canvas.drawText("No active quests", panelLeft + 15f, questY, textPaint)
        } else {
            for (quest in activeQuests) {
                // Quest title
                textPaint.color = Color.rgb(255, 200, 0)
                canvas.drawText(quest.title, panelLeft + 15f, questY, textPaint)
                questY += 10f

                // Quest objectives
                for (objective in quest.objectives) {
                    textPaint.color = if (objective.completed) Color.rgb(0, 200, 0) else Color.rgb(150, 150, 150)
                    val marker = if (objective.completed) "[X]" else "[ ]"
                    canvas.drawText("$marker ${objective.description}", panelLeft + 20f, questY, textPaint)
                    questY += 10f
                }
                questY += 5f
            }
        }

        // Close hint
        textPaint.color = Color.rgb(100, 100, 120)
        canvas.drawText("Tap outside to close", screenWidth / 2f - 35f, panelTop + panelHeight - 10f, textPaint)
    }

    fun renderPauseMenu(canvas: Canvas) {
        // Dim overlay
        backgroundPaint.color = Color.argb(180, 0, 0, 0)
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), backgroundPaint)

        // Pause text
        titlePaint.textSize = 16f
        titlePaint.color = Color.rgb(0, 255, 255)
        canvas.drawText("PAUSED", screenWidth / 2f, screenHeight / 2f, titlePaint)

        textPaint.color = Color.rgb(150, 150, 150)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Tap to resume", screenWidth / 2f, screenHeight / 2f + 20f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }
}
