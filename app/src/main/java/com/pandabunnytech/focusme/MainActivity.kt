package com.pandabunnytech.focusme

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Bundle
import android.os.SystemClock
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

private const val COLOR_COUNT = 40


class MainActivity : ComponentActivity(), SurfaceHolder.Callback {

    private lateinit var button: Button
    private lateinit var messageText: TextView
    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder

    private val messages = listOf(
        "Please take a deep breath",
        "Think what you want to do now",
        "Think what happens if I do not do this now?",
        "Let go of the troubles",
        "Focus",
        "Focus",
        "I can focus",
        "Exhale"
    )

    private val backgroundColors = IntArray(COLOR_COUNT)

    private fun initializeBackgroundColors() {
        val pink = Color.parseColor("#FF4081") // Pink
        val green = Color.parseColor("#88AF50") // Green

        val colors = IntArray(COLOR_COUNT)

        for (i in 0 until COLOR_COUNT / 2) {
            val fraction = i.toFloat() / (COLOR_COUNT - 1)
            val hue = fraction * 2 / 3
            val interpolatedColor = interpolateColor(pink, green, hue)
            colors[i] = interpolatedColor
            colors[COLOR_COUNT - i - 1] = interpolatedColor
        }

        // Assign the interpolated colors to the backgroundColors array
        System.arraycopy(colors, 0, backgroundColors, 0, COLOR_COUNT)
    }

    private fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val startHSV = FloatArray(3)
        val endHSV = FloatArray(3)
        Color.colorToHSV(startColor, startHSV)
        Color.colorToHSV(endColor, endHSV)

        for (i in 0 until 3) {
            endHSV[i] = interpolate(startHSV[i], endHSV[i], fraction)
        }
        return Color.HSVToColor(endHSV)
    }

    private fun interpolate(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }


    private var lastDrawTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus)
        initializeBackgroundColors()

        button = findViewById(R.id.focus_button)
        messageText = findViewById(R.id.message_text)
        surfaceView = findViewById(R.id.surface_view)
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)

        button.setOnClickListener {
            displayMessages()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun displayMessages() {
        button.isEnabled = false
        GlobalScope.launch(Dispatchers.Main) {
            for (i in messages.indices) {
                messageText.text = messages[i]
                delay(2000)
            }
            delay(3000)
            button.isEnabled = true
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        lastDrawTime = SystemClock.elapsedRealtime()
        drawBackground(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Not used
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Not used
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun drawBackground(holder: SurfaceHolder) {
        GlobalScope.launch(Dispatchers.IO) {
            val currentTime = SystemClock.elapsedRealtime()
            val deltaTime = max(currentTime - lastDrawTime, 1)
            val targetDelay = 1000 / 30

            shiftColors()
            val canvas = holder.lockCanvas()
            drawGradient(canvas)
            holder.unlockCanvasAndPost(canvas)

            val remainingDelay = max(targetDelay - deltaTime, 1)
            delay(remainingDelay)

            lastDrawTime = SystemClock.elapsedRealtime()
            drawBackground(holder)
        }
    }

    private fun shiftColors() {
        val firstColor = backgroundColors.first()
        val lastColor = backgroundColors.last()

        for (i in 0 until backgroundColors.size - 1) {
            backgroundColors[i] =
                interpolateColor(backgroundColors[i], backgroundColors[i + 1], 0.5f)
        }

        backgroundColors[backgroundColors.size - 1] = interpolateColor(lastColor, firstColor, 0.5f)
    }

    private fun drawGradient(canvas: Canvas) {
        val paint = Paint()
        paint.shader = LinearGradient(
            0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(),
            backgroundColors, null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
    }
}