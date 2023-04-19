package se.ju.student.robomow.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import se.ju.student.robomow.model.Position
import java.lang.Float.min

class MapView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    companion object {
        private const val PATH_COLOR = 0xFF000000.toInt() // Black
        private const val BORDER_COLOR = 0xFF09A104.toInt() // Green
        private const val PATH_STROKE_WIDTH = 10f
        private const val BORDER_STROKE_WIDTH = 20f
        private const val MAX_SCALE_FACTOR = 50f
        private const val MARGIN = 20f
    }

    private val pathPaint = Paint().apply {
        color = PATH_COLOR
        style = Paint.Style.STROKE
        strokeWidth = PATH_STROKE_WIDTH
    }
    private val borderPaint = Paint().apply {
        color = BORDER_COLOR
        style = Paint.Style.STROKE
        strokeWidth = BORDER_STROKE_WIDTH
    }
    private val path = Path()
    private var scaleFactor = 50f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the border
        canvas.drawRect(
            borderPaint.strokeWidth / 2,
            borderPaint.strokeWidth / 2,
            width - borderPaint.strokeWidth / 2,
            height - borderPaint.strokeWidth / 2,
            borderPaint
        )

        // Draw the path
        canvas.save()
        canvas.translate(MARGIN, MARGIN) // Apply the margin
        canvas.drawPath(path, pathPaint)
        canvas.restore()
    }

    private fun autoScale(positions: List<Position>): Pair<Float, Float> {
        val maxWidth = width - 2 * MARGIN
        val maxHeight = height - 2 * MARGIN

        val minX = positions.minOf { it.x }
        val minY = positions.minOf { it.y }
        val maxX = positions.maxOf { it.x }
        val maxY = positions.maxOf { it.y }

        val pathWidth = maxX - minX
        val pathHeight = maxY - minY

        val scaleX = maxWidth / pathWidth
        val scaleY = maxHeight / pathHeight

        scaleFactor = min(min(scaleX, scaleY), MAX_SCALE_FACTOR) // Add a maximum limit to the scaleFactor

        val pathCenterX = (minX + maxX) * scaleFactor / 2
        val pathCenterY = (minY + maxY) * scaleFactor / 2

        return Pair(pathCenterX, pathCenterY)
    }


    fun setCoordinates(positions: List<Position>?) {
        if (width == 0 || height == 0) {
            post { setCoordinates(positions) } // If the view is not yet laid out, post the action to the message queue
            return
        }

        val (pathCenterX, pathCenterY) = autoScale(positions!!) // Calculate the optimal scaleFactor and get the center of the path

        val centerX = (width / 2f) - pathCenterX
        val centerY = (height / 2f) - pathCenterY

        path.reset()
        positions?.forEachIndexed { index, coordinate ->
            val x = (coordinate.x * scaleFactor) + centerX
            val y = (coordinate.y * scaleFactor) + centerY

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        invalidate()
    }

}
