package com.neoncity.rpg.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.neoncity.rpg.engine.GameView
import com.neoncity.rpg.game.GameState

/**
 * Main game renderer - renders everything to a low-res bitmap
 * that gets scaled up for the pixel art effect.
 */
class GameRenderer(
    context: Context,
    private val width: Int,
    private val height: Int
) {
    private val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    private val canvas: Canvas = Canvas(bitmap)

    private val tileRenderer: IsometricTileRenderer = IsometricTileRenderer()
    private val entityRenderer: EntityRenderer = EntityRenderer()
    private val uiRenderer: UIRenderer = UIRenderer(width, height)

    private val backgroundPaint = Paint().apply {
        color = Color.rgb(13, 13, 26)  // Dark blue-black
    }

    fun render(gameState: GameState): Bitmap {
        // Clear canvas
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        when (gameState.currentScreen) {
            GameState.Screen.TITLE -> renderTitleScreen(gameState)
            GameState.Screen.PLAYING -> renderGameplay(gameState)
            GameState.Screen.DIALOGUE -> {
                renderGameplay(gameState)
                uiRenderer.renderDialogue(canvas, gameState)
            }
            GameState.Screen.INVENTORY -> {
                renderGameplay(gameState)
                uiRenderer.renderInventory(canvas, gameState)
            }
            GameState.Screen.QUEST_LOG -> {
                renderGameplay(gameState)
                uiRenderer.renderQuestLog(canvas, gameState)
            }
            GameState.Screen.PAUSE -> {
                renderGameplay(gameState)
                uiRenderer.renderPauseMenu(canvas)
            }
        }

        return bitmap
    }

    private fun renderTitleScreen(gameState: GameState) {
        // Draw title background with city silhouette
        drawCitySilhouette()

        // Draw title
        uiRenderer.renderTitleScreen(canvas)
    }

    private fun drawCitySilhouette() {
        val silhouettePaint = Paint().apply {
            color = Color.rgb(20, 20, 40)
        }

        // Draw building silhouettes
        val buildings = listOf(
            Rect(20, 100, 50, height),
            Rect(45, 80, 80, height),
            Rect(75, 110, 100, height),
            Rect(95, 70, 130, height),
            Rect(125, 90, 155, height),
            Rect(150, 60, 190, height),
            Rect(185, 85, 210, height),
            Rect(205, 95, 235, height),
            Rect(230, 75, 270, height),
            Rect(265, 105, 300, height)
        )

        buildings.forEach { canvas.drawRect(it, silhouettePaint) }

        // Draw neon accents
        val neonPaint = Paint().apply {
            color = Color.rgb(0, 255, 255)
            strokeWidth = 1f
        }

        canvas.drawLine(95f, 75f, 130f, 75f, neonPaint)
        neonPaint.color = Color.rgb(255, 0, 255)
        canvas.drawLine(150f, 65f, 190f, 65f, neonPaint)
        neonPaint.color = Color.rgb(255, 255, 0)
        canvas.drawLine(230f, 80f, 270f, 80f, neonPaint)
    }

    private fun renderGameplay(gameState: GameState) {
        val cameraX = gameState.cameraX
        val cameraY = gameState.cameraY

        // Render tiles
        tileRenderer.render(canvas, gameState.currentDistrict, cameraX, cameraY, width, height)

        // Render entities (sorted by Y for depth)
        val entities = mutableListOf<Any>()
        entities.add(gameState.player)
        entities.addAll(gameState.npcs)

        // Sort by Y position for proper depth ordering in isometric view
        entities.sortedBy {
            when (it) {
                is com.neoncity.rpg.entity.Player -> it.y
                is com.neoncity.rpg.entity.NPC -> it.y
                else -> 0f
            }
        }.forEach { entity ->
            entityRenderer.render(canvas, entity, cameraX, cameraY, width, height)
        }

        // Render HUD
        uiRenderer.renderHUD(canvas, gameState)
    }
}
