package com.neoncity.rpg.game

import android.view.MotionEvent
import com.neoncity.rpg.engine.GameView

/**
 * Handles all touch input and maps it to game actions.
 */
class InputHandler(private val gameState: GameState) {

    private var scaleX: Float = 1f
    private var scaleY: Float = 1f

    // Touch tracking
    private var touchStartX: Float = 0f
    private var touchStartY: Float = 0f
    private var isDragging: Boolean = false
    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f

    // Virtual joystick
    private val joystickCenterX = 50f
    private val joystickCenterY = GameView.GAME_HEIGHT - 50f
    private val joystickRadius = 35f
    private var joystickActive = false

    // UI button regions (in game coordinates)
    private val buttonInventory = ButtonRegion(GameView.GAME_WIDTH - 60f, GameView.GAME_HEIGHT - 30f, 25f, 25f)
    private val buttonQuest = ButtonRegion(GameView.GAME_WIDTH - 30f, GameView.GAME_HEIGHT - 30f, 25f, 25f)
    private val buttonInteract = ButtonRegion(GameView.GAME_WIDTH - 45f, GameView.GAME_HEIGHT - 60f, 30f, 25f)

    fun setScale(scaleX: Float, scaleY: Float) {
        this.scaleX = scaleX
        this.scaleY = scaleY
    }

    fun handleTouch(event: MotionEvent): Boolean {
        val gameX = event.x * scaleX
        val gameY = event.y * scaleY

        return when (gameState.currentScreen) {
            GameState.Screen.TITLE -> handleTitleInput(event, gameX, gameY)
            GameState.Screen.PLAYING -> handlePlayingInput(event, gameX, gameY)
            GameState.Screen.DIALOGUE -> handleDialogueInput(event, gameX, gameY)
            GameState.Screen.INVENTORY -> handleInventoryInput(event, gameX, gameY)
            GameState.Screen.QUEST_LOG -> handleQuestLogInput(event, gameX, gameY)
            GameState.Screen.PAUSE -> handlePauseInput(event, gameX, gameY)
        }
    }

    private fun handleTitleInput(event: MotionEvent, gameX: Float, gameY: Float): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            // Check if tap is in the "New Game" button area
            val centerY = GameView.GAME_HEIGHT / 2f
            if (gameY > centerY - 20 && gameY < centerY + 20) {
                gameState.startGame()
                return true
            }
        }
        return true
    }

    private fun handlePlayingInput(event: MotionEvent, gameX: Float, gameY: Float): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = gameX
                touchStartY = gameY
                lastTouchX = gameX
                lastTouchY = gameY
                isDragging = false

                // Check if touching joystick area
                val dx = gameX - joystickCenterX
                val dy = gameY - joystickCenterY
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                joystickActive = dist < joystickRadius * 1.5f

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (joystickActive) {
                    // Calculate joystick direction
                    val dx = gameX - joystickCenterX
                    val dy = gameY - joystickCenterY
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                    if (dist > 5f) {
                        // Normalize and apply movement
                        val normalizedX = dx / dist
                        val normalizedY = dy / dist
                        val speed = minOf(dist / joystickRadius, 1f) * 0.08f

                        gameState.player.velocityX = normalizedX * speed
                        gameState.player.velocityY = normalizedY * speed

                        // Update facing direction
                        if (kotlin.math.abs(normalizedX) > kotlin.math.abs(normalizedY)) {
                            gameState.player.facing = if (normalizedX > 0) 1 else 3
                        } else {
                            gameState.player.facing = if (normalizedY > 0) 2 else 0
                        }
                    }
                } else {
                    isDragging = true
                }

                lastTouchX = gameX
                lastTouchY = gameY
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (joystickActive) {
                    // Stop movement
                    gameState.player.velocityX = 0f
                    gameState.player.velocityY = 0f
                    joystickActive = false
                } else if (!isDragging) {
                    // It's a tap - check UI buttons
                    when {
                        buttonInventory.contains(gameX, gameY) -> {
                            gameState.toggleInventory()
                        }
                        buttonQuest.contains(gameX, gameY) -> {
                            gameState.toggleQuestLog()
                        }
                        buttonInteract.contains(gameX, gameY) -> {
                            // Try to interact with nearby NPC
                            tryInteract()
                        }
                        else -> {
                            // Tap to move (click-to-move)
                            handleTapToMove(gameX, gameY)
                        }
                    }
                }
                isDragging = false
                return true
            }
        }
        return true
    }

    private fun handleTapToMove(gameX: Float, gameY: Float) {
        // Convert screen position to world position
        val worldX = screenToWorldX(gameX) + gameState.cameraX
        val worldY = screenToWorldY(gameY) + gameState.cameraY

        // Set target for player
        gameState.player.setMoveTarget(worldX, worldY)
    }

    private fun screenToWorldX(screenX: Float): Float {
        // Convert isometric screen coordinates to world coordinates
        val centerX = GameView.GAME_WIDTH / 2f
        val centerY = GameView.GAME_HEIGHT / 2f
        val dx = screenX - centerX
        val dy = screenY(screenX, centerY) - centerY
        return (dx / 16f + dy / 8f)
    }

    private fun screenToWorldY(screenY: Float): Float {
        val centerX = GameView.GAME_WIDTH / 2f
        val centerY = GameView.GAME_HEIGHT / 2f
        val dx = centerX - centerX
        val dy = screenY - centerY
        return (dy / 8f - dx / 16f)
    }

    private fun screenY(x: Float, y: Float): Float = y

    private fun tryInteract() {
        for (npc in gameState.npcs) {
            if (npc.isPlayerNear) {
                gameState.startDialogue(npc)
                break
            }
        }
    }

    private fun handleDialogueInput(event: MotionEvent, gameX: Float, gameY: Float): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val dialogue = gameState.activeDialogue ?: return true

            // Check which response was tapped
            val responses = dialogue.getResponses()
            val dialogueBoxTop = GameView.GAME_HEIGHT - 60f
            val responseHeight = 12f

            for (i in responses.indices) {
                val responseY = dialogueBoxTop + 25f + i * responseHeight
                if (gameY >= responseY - 6 && gameY <= responseY + 6) {
                    if (!dialogue.selectResponse(i)) {
                        // End dialogue if no next node
                        gameState.endDialogue()
                    }
                    return true
                }
            }

            // Tap outside responses area - advance or close
            if (responses.isEmpty() || responses.size == 1 && responses[0] == "Goodbye") {
                gameState.endDialogue()
            }
        }
        return true
    }

    private fun handleInventoryInput(event: MotionEvent, gameX: Float, gameY: Float): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            // Check for close button or tap outside
            if (gameX < 20 || gameX > GameView.GAME_WIDTH - 20 ||
                gameY < 20 || gameY > GameView.GAME_HEIGHT - 20) {
                gameState.toggleInventory()
            }
            // TODO: Handle item selection
        }
        return true
    }

    private fun handleQuestLogInput(event: MotionEvent, gameX: Float, gameY: Float): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            // Check for close button or tap outside
            if (gameX < 20 || gameX > GameView.GAME_WIDTH - 20 ||
                gameY < 20 || gameY > GameView.GAME_HEIGHT - 20) {
                gameState.toggleQuestLog()
            }
        }
        return true
    }

    private fun handlePauseInput(event: MotionEvent, gameX: Float, gameY: Float): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            gameState.currentScreen = GameState.Screen.PLAYING
        }
        return true
    }

    data class ButtonRegion(val x: Float, val y: Float, val width: Float, val height: Float) {
        fun contains(px: Float, py: Float): Boolean {
            return px >= x && px <= x + width && py >= y && py <= y + height
        }
    }
}
