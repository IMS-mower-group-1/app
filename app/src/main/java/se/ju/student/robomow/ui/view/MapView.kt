package se.ju.student.robomow.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import se.ju.student.robomow.model.Position
import se.ju.student.robomow.ui.MapActivity
import java.lang.Float.min

class MapView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint()
    private val path = Path()
    private var scaleFactor = 50f

    private val borderWidth = 10f
    private val borderColor = 0xFF000000.toInt()
    private val margin = 20f
    private var centerStartPosition = true // Set this flag to true to start the path from the center

    init {
        paint.color = 0xFF000000.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the border
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth
        canvas.drawRect(
            borderWidth / 2,
            borderWidth / 2,
            width - borderWidth / 2,
            height - borderWidth / 2,
            paint
        )

        // Draw the path
        paint.color = 0xFF000000.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        canvas.save()
        canvas.translate(margin, margin) // Apply the margin
        canvas.drawPath(path, paint)
        canvas.restore()
    }

    private fun autoScale(positions: List<Position>): Pair<Float, Float> {
        val maxWidth = width - 2 * margin
        val maxHeight = height - 2 * margin

        val minX = positions.minOf { it.x }
        val minY = positions.minOf { it.y }
        val maxX = positions.maxOf { it.x }
        val maxY = positions.maxOf { it.y }

        val pathWidth = maxX - minX
        val pathHeight = maxY - minY

        val scaleX = maxWidth / pathWidth
        val scaleY = maxHeight / pathHeight

        scaleFactor = min(min(scaleX, scaleY), 50f) // Add a maximum limit to the scaleFactor

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

        val centerX = if (centerStartPosition) (width / 2f) - pathCenterX else margin
        val centerY = if (centerStartPosition) (height / 2f) - pathCenterY else margin

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
