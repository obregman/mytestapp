package com.neoncity.rpg.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.neoncity.rpg.world.District
import com.neoncity.rpg.world.Tile
import com.neoncity.rpg.world.TileType

/**
 * Renders the isometric tile map.
 * Uses a 2:1 isometric projection (16x8 pixel tiles).
 */
class IsometricTileRenderer {

    companion object {
        const val TILE_WIDTH = 16
        const val TILE_HEIGHT = 8
        const val TILE_DEPTH = 8  // For buildings/walls
    }

    private val tilePaint = Paint().apply {
        isAntiAlias = false
        style = Paint.Style.FILL
    }

    private val outlinePaint = Paint().apply {
        isAntiAlias = false
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = Color.rgb(30, 30, 50)
    }

    private val neonPaint = Paint().apply {
        isAntiAlias = false
        strokeWidth = 1f
    }

    fun render(canvas: Canvas, district: District, cameraX: Float, cameraY: Float, screenWidth: Int, screenHeight: Int) {
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 3f  // Offset for better view

        // Calculate visible tile range
        val startX = (cameraX - 10).toInt().coerceAtLeast(0)
        val startY = (cameraY - 10).toInt().coerceAtLeast(0)
        val endX = (cameraX + 20).toInt().coerceAtMost(district.width)
        val endY = (cameraY + 20).toInt().coerceAtMost(district.height)

        // Render tiles in isometric order (back to front)
        for (y in startY until endY) {
            for (x in startX until endX) {
                val tile = district.getTile(x, y)
                if (tile != null) {
                    val screenPos = worldToScreen(x.toFloat() - cameraX, y.toFloat() - cameraY, centerX, centerY)
                    renderTile(canvas, tile, screenPos.first, screenPos.second, district.isNight)
                }
            }
        }
    }

    private fun worldToScreen(worldX: Float, worldY: Float, centerX: Float, centerY: Float): Pair<Float, Float> {
        val screenX = centerX + (worldX - worldY) * (TILE_WIDTH / 2f)
        val screenY = centerY + (worldX + worldY) * (TILE_HEIGHT / 2f)
        return Pair(screenX, screenY)
    }

    private fun renderTile(canvas: Canvas, tile: Tile, screenX: Float, screenY: Float, isNight: Boolean) {
        when (tile.type) {
            TileType.GROUND -> renderGroundTile(canvas, tile, screenX, screenY)
            TileType.ROAD -> renderRoadTile(canvas, tile, screenX, screenY)
            TileType.SIDEWALK -> renderSidewalkTile(canvas, tile, screenX, screenY)
            TileType.BUILDING -> renderBuildingTile(canvas, tile, screenX, screenY, isNight)
            TileType.WALL -> renderWallTile(canvas, tile, screenX, screenY)
            TileType.WATER -> renderWaterTile(canvas, tile, screenX, screenY)
            TileType.PARK -> renderParkTile(canvas, tile, screenX, screenY)
            TileType.DOOR -> renderDoorTile(canvas, tile, screenX, screenY)
            TileType.TRANSITION -> renderTransitionTile(canvas, tile, screenX, screenY)
        }
    }

    private fun renderGroundTile(canvas: Canvas, tile: Tile, x: Float, y: Float) {
        tilePaint.color = Color.rgb(40, 40, 50)
        drawIsometricTile(canvas, x, y, tilePaint)
    }

    private fun renderRoadTile(canvas: Canvas, tile: Tile, x: Float, y: Float) {
        tilePaint.color = Color.rgb(35, 35, 45)
        drawIsometricTile(canvas, x, y, tilePaint)

        // Road markings
        if (tile.variant == 1) {
            tilePaint.color = Color.rgb(80, 80, 60)
            canvas.drawLine(x - 2, y, x + 2, y, tilePaint)
        }
    }

    private fun renderSidewalkTile(canvas: Canvas, tile: Tile, x: Float, y: Float) {
        tilePaint.color = Color.rgb(55, 55, 65)
        drawIsometricTile(canvas, x, y, tilePaint)
    }

    private fun renderBuildingTile(canvas: Canvas, tile: Tile, x: Float, y: Float, isNight: Boolean) {
        val height = tile.height * TILE_DEPTH

        // Building top
        tilePaint.color = tile.color
        drawIsometricTile(canvas, x, y - height, tilePaint)

        // Building front face (south side)
        val frontColor = darkenColor(tile.color, 0.7f)
        tilePaint.color = frontColor
        drawBuildingFrontFace(canvas, x, y, height)

        // Building right face (east side)
        val sideColor = darkenColor(tile.color, 0.5f)
        tilePaint.color = sideColor
        drawBuildingRightFace(canvas, x, y, height)

        // Neon lights on buildings at night
        if (isNight && tile.hasNeonLight) {
            drawNeonAccent(canvas, x, y - height + 4, tile.neonColor)
        }

        // Windows
        if (height > 8) {
            drawWindows(canvas, x, y, height, isNight)
        }
    }

    private fun renderWallTile(canvas: Canvas, tile: Tile, x: Float, y: Float) {
        val height = tile.height * TILE_DEPTH

        tilePaint.color = Color.rgb(50, 50, 60)
        drawIsometricTile(canvas, x, y - height, tilePaint)

        tilePaint.color = Color.rgb(35, 35, 45)
        drawBuildingFrontFace(canvas, x, y, height)

        tilePaint.color = Color.rgb(25, 25, 35)
        drawBuildingRightFace(canvas, x, y, height)
    }

    private fun renderWaterTile(canvas: Canvas, tile: Tile, x: Float, y: Float) {
        // Animated water effect using frame
        val waterColor = if ((System.currentTimeMillis() / 500) % 2 == 0L) {
            Color.rgb(20, 40, 80)
        } else {
            Color.rgb(25, 50, 90)
        }
        tilePaint.color = waterColor
        drawIsometricTile(canvas, x, y, tilePaint)

        // Water shimmer
        neonPaint.color = Color.rgb(60, 100, 150)
        canvas.drawPoint(x + ((System.currentTimeMillis() / 200) % 8 - 4), y, neonPaint)
    }

    private fun renderParkTile(canvas: Canvas, tile: Tile, x: Float, y: Float) {
        tilePaint.color = Color.rgb(30, 60, 35)
        drawIsometricTile(canvas, x, y, tilePaint)

        // Draw grass details
        if (tile.variant == 1) {
            tilePaint.color = Color.rgb(40, 80, 45)
            canvas.drawPoint(x - 2, y - 1, tilePaint)
            canvas.drawPoint(x + 2, y + 1, tilePaint)
        }
    }

    private fun renderDoorTile(canvas: Canvas, tile: Tile, x: Float, y: Float) {
        // Ground
        tilePaint.color = Color.rgb(50, 45, 40)
        drawIsometricTile(canvas, x, y, tilePaint)

        // Door frame
        tilePaint.color = tile.color
        canvas.drawRect(x - 2, y - 10, x + 2, y - 2, tilePaint)

        // Door glow
        if (tile.hasNeonLight) {
            neonPaint.color = tile.neonColor
            canvas.drawLine(x - 3, y - 10, x - 3, y - 2, neonPaint)
            canvas.drawLine(x + 3, y - 10, x + 3, y - 2, neonPaint)
        }
    }

    private fun renderTransitionTile(canvas: Canvas, tile: Tile, x: Float, y: Float) {
        // Arrow or indicator for zone transitions
        tilePaint.color = Color.rgb(60, 60, 80)
        drawIsometricTile(canvas, x, y, tilePaint)

        // Pulsing indicator
        val pulse = ((System.currentTimeMillis() / 300) % 3).toInt()
        neonPaint.color = Color.rgb(0, 200 + pulse * 20, 200 + pulse * 20)
        canvas.drawCircle(x, y - 2, 2f, neonPaint)
    }

    private fun drawIsometricTile(canvas: Canvas, x: Float, y: Float, paint: Paint) {
        val path = Path()
        path.moveTo(x, y - TILE_HEIGHT / 2f)  // Top
        path.lineTo(x + TILE_WIDTH / 2f, y)    // Right
        path.lineTo(x, y + TILE_HEIGHT / 2f)   // Bottom
        path.lineTo(x - TILE_WIDTH / 2f, y)    // Left
        path.close()

        canvas.drawPath(path, paint)
        canvas.drawPath(path, outlinePaint)
    }

    private fun drawBuildingFrontFace(canvas: Canvas, x: Float, y: Float, height: Int) {
        val path = Path()
        path.moveTo(x - TILE_WIDTH / 2f, y)
        path.lineTo(x, y + TILE_HEIGHT / 2f)
        path.lineTo(x, y + TILE_HEIGHT / 2f - height)
        path.lineTo(x - TILE_WIDTH / 2f, y - height)
        path.close()

        canvas.drawPath(path, tilePaint)
    }

    private fun drawBuildingRightFace(canvas: Canvas, x: Float, y: Float, height: Int) {
        val path = Path()
        path.moveTo(x, y + TILE_HEIGHT / 2f)
        path.lineTo(x + TILE_WIDTH / 2f, y)
        path.lineTo(x + TILE_WIDTH / 2f, y - height)
        path.lineTo(x, y + TILE_HEIGHT / 2f - height)
        path.close()

        canvas.drawPath(path, tilePaint)
    }

    private fun drawNeonAccent(canvas: Canvas, x: Float, y: Float, color: Int) {
        neonPaint.color = color
        canvas.drawLine(x - 5, y, x + 5, y, neonPaint)
    }

    private fun drawWindows(canvas: Canvas, x: Float, y: Float, height: Int, isNight: Boolean) {
        val windowColor = if (isNight) {
            if (Math.random() > 0.3) Color.rgb(200, 180, 100) else Color.rgb(30, 30, 40)
        } else {
            Color.rgb(100, 150, 200)
        }

        tilePaint.color = windowColor
        val windowCount = height / 10

        for (i in 0 until windowCount) {
            val wy = y - 6 - i * 10
            canvas.drawRect(x - 4, wy - 2, x - 2, wy, tilePaint)
        }
    }

    private fun darkenColor(color: Int, factor: Float): Int {
        val r = ((Color.red(color) * factor).toInt()).coerceIn(0, 255)
        val g = ((Color.green(color) * factor).toInt()).coerceIn(0, 255)
        val b = ((Color.blue(color) * factor).toInt()).coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }
}
