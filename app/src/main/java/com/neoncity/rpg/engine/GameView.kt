package com.neoncity.rpg.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.neoncity.rpg.game.GameState
import com.neoncity.rpg.game.InputHandler
import com.neoncity.rpg.render.GameRenderer

/**
 * Main game view that handles the game loop and rendering.
 * Uses low-resolution rendering scaled up for pixel art effect.
 */
class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    companion object {
        // Target resolution for low-res pixel art look
        const val GAME_WIDTH = 320
        const val GAME_HEIGHT = 180
        const val TARGET_FPS = 60
        const val TARGET_FRAME_TIME = 1000L / TARGET_FPS
    }

    private var gameThread: Thread? = null
    private var isRunning = false

    private val gameState: GameState = GameState()
    private val renderer: GameRenderer = GameRenderer(context, GAME_WIDTH, GAME_HEIGHT)
    private val inputHandler: InputHandler = InputHandler(gameState)

    private var scaleX = 1f
    private var scaleY = 1f

    private val fpsPaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
        isAntiAlias = true
    }

    private var uiInitialized = false

    private var lastFrameTime = System.currentTimeMillis()
    private var fps = 0

    init {
        holder.addCallback(this)
        holder.setFormat(PixelFormat.OPAQUE)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Calculate scale factors for touch input mapping
        scaleX = GAME_WIDTH.toFloat() / width
        scaleY = GAME_HEIGHT.toFloat() / height
        inputHandler.setScale(scaleX, scaleY)

        // Set up renderers for high-resolution text
        renderer.uiRenderer.setScreenSize(width, height)
        renderer.entityRenderer.setScreenSize(width, height, GAME_WIDTH, GAME_HEIGHT)
        uiInitialized = true

        resume()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        scaleX = GAME_WIDTH.toFloat() / width
        scaleY = GAME_HEIGHT.toFloat() / height
        inputHandler.setScale(scaleX, scaleY)

        // Update renderers for high-resolution text
        renderer.uiRenderer.setScreenSize(width, height)
        renderer.entityRenderer.setScreenSize(width, height, GAME_WIDTH, GAME_HEIGHT)
        uiInitialized = true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pause()
    }

    fun resume() {
        isRunning = true
        gameThread = Thread(this)
        gameThread?.start()
    }

    fun pause() {
        isRunning = false
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun run() {
        while (isRunning) {
            val startTime = System.currentTimeMillis()

            // Update game state
            update()

            // Render frame
            val canvas = holder.lockCanvas()
            if (canvas != null) {
                try {
                    synchronized(holder) {
                        draw(canvas)
                    }
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            // Calculate FPS and frame timing
            val frameTime = System.currentTimeMillis() - startTime
            if (frameTime < TARGET_FRAME_TIME) {
                try {
                    Thread.sleep(TARGET_FRAME_TIME - frameTime)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

            // Update FPS counter
            val currentTime = System.currentTimeMillis()
            fps = (1000 / (currentTime - lastFrameTime + 1)).toInt()
            lastFrameTime = currentTime
        }
    }

    private fun update() {
        gameState.update()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // Render game graphics at low resolution then scale up (pixel art)
        val gameBitmap = renderer.render(gameState)

        // Draw scaled up bitmap for pixel art effect
        canvas.drawBitmap(
            gameBitmap,
            null,
            android.graphics.Rect(0, 0, width, height),
            null
        )

        // Render UI at full screen resolution for crisp text
        if (uiInitialized) {
            renderer.renderUI(canvas, gameState)
        }

        // Draw FPS counter (at screen resolution)
        if (gameState.showDebug) {
            canvas.drawText("FPS: $fps", 10f, 30f, fpsPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return inputHandler.handleTouch(event)
    }
}
