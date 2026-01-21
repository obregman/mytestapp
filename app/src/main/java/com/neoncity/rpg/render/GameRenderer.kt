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
 * Main game renderer - renders game graphics to a low-res bitmap
 * that gets scaled up for the pixel art effect.
 * UI is rendered separately at full resolution for crisp text.
 */
class GameRenderer(
    context: Context,
    private val width: Int,
    private val height: Int
) {
    private val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    private val canvas: Canvas = Canvas(bitmap)

    private val tileRenderer: IsometricTileRenderer = IsometricTileRenderer()
    val entityRenderer: EntityRenderer = EntityRenderer()
    val uiRenderer: UIRenderer = UIRenderer(width, height)

    private val backgroundPaint = Paint().apply {
        color = Color.rgb(13, 13, 26)  // Dark blue-black
    }

    /**
     * Render game graphics at low resolution (pixel art).
     * UI is NOT rendered here - it should be rendered separately at high resolution.
     */
    fun render(gameState: GameState): Bitmap {
        // Clear canvas
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        when (gameState.currentScreen) {
            GameState.Screen.TITLE -> renderTitleBackground()
            GameState.Screen.PLAYING,
            GameState.Screen.DIALOGUE,
            GameState.Screen.INVENTORY,
            GameState.Screen.QUEST_LOG,
            GameState.Screen.PAUSE -> renderGameplay(gameState)
        }

        return bitmap
    }

    /**
     * Render UI elements to the given canvas at high resolution.
     * Call this after rendering the scaled-up game bitmap.
     */
    fun renderUI(canvas: Canvas, gameState: GameState) {
        // Render entity labels (NPC names, quest markers) at high resolution
        if (gameState.currentScreen != GameState.Screen.TITLE) {
            renderEntityLabels(canvas, gameState)
        }

        when (gameState.currentScreen) {
            GameState.Screen.TITLE -> uiRenderer.renderTitleScreen(canvas)
            GameState.Screen.PLAYING -> uiRenderer.renderHUD(canvas, gameState)
            GameState.Screen.DIALOGUE -> {
                uiRenderer.renderHUD(canvas, gameState)
                uiRenderer.renderDialogue(canvas, gameState)
            }
            GameState.Screen.INVENTORY -> {
                uiRenderer.renderHUD(canvas, gameState)
                uiRenderer.renderInventory(canvas, gameState)
            }
            GameState.Screen.QUEST_LOG -> {
                uiRenderer.renderHUD(canvas, gameState)
                uiRenderer.renderQuestLog(canvas, gameState)
            }
            GameState.Screen.PAUSE -> {
                uiRenderer.renderHUD(canvas, gameState)
                uiRenderer.renderPauseMenu(canvas)
            }
        }
    }

    /**
     * Render entity labels at high resolution.
     */
    private fun renderEntityLabels(canvas: Canvas, gameState: GameState) {
        val cameraX = gameState.cameraX
        val cameraY = gameState.cameraY

        // Render NPC labels sorted by isometric depth (x + y) for proper ordering
        gameState.npcs.sortedBy { it.x + it.y }.forEach { npc ->
            entityRenderer.renderLabels(canvas, npc, cameraX, cameraY, width, height)
        }
    }

    private fun renderTitleBackground() {
        // Draw title background with city silhouette (pixel art)
        drawCitySilhouette()
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

        // Collect and sort entities by isometric depth (x + y)
        // Using floor() to determine which tile row they belong to
        val entities = mutableListOf<Any>()
        entities.add(gameState.player)
        entities.addAll(gameState.npcs)

        // Group entities by their tile Y row for interleaved rendering
        val entitiesByRow = entities.groupBy { entity ->
            when (entity) {
                is com.neoncity.rpg.entity.Player -> entity.y.toInt()
                is com.neoncity.rpg.entity.NPC -> entity.y.toInt()
                else -> 0
            }
        }

        // Track which rows have been rendered
        val renderedRows = mutableSetOf<Int>()

        // Render tiles with interleaved entity rendering for proper depth
        tileRenderer.render(canvas, gameState.currentDistrict, cameraX, cameraY, width, height) { row ->
            // Render all entities at this Y row (and any rows between last rendered)
            // This handles entities that might be at fractional positions
            val startRow = (renderedRows.maxOrNull() ?: (row - 1)) + 1
            for (r in startRow..row) {
                entitiesByRow[r]?.let { rowEntities ->
                    // Sort entities within the same row by x + y for proper isometric depth
                    rowEntities.sortedBy { entity ->
                        when (entity) {
                            is com.neoncity.rpg.entity.Player -> entity.x + entity.y
                            is com.neoncity.rpg.entity.NPC -> entity.x + entity.y
                            else -> 0f
                        }
                    }.forEach { entity ->
                        entityRenderer.render(canvas, entity, cameraX, cameraY, width, height, skipLabels = true)
                    }
                }
                renderedRows.add(r)
            }
        }

        // Render any remaining entities that weren't covered by tile rows
        entitiesByRow.keys.filter { it !in renderedRows }.sorted().forEach { row ->
            entitiesByRow[row]?.sortedBy { entity ->
                when (entity) {
                    is com.neoncity.rpg.entity.Player -> entity.x + entity.y
                    is com.neoncity.rpg.entity.NPC -> entity.x + entity.y
                    else -> 0f
                }
            }?.forEach { entity ->
                entityRenderer.render(canvas, entity, cameraX, cameraY, width, height, skipLabels = true)
            }
        }
    }
}
