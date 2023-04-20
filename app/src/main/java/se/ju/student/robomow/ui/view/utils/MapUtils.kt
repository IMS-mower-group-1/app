package se.ju.student.robomow.ui.view.utils

import android.graphics.Canvas
import android.graphics.Paint
import se.ju.student.robomow.model.Position
import se.ju.student.robomow.ui.constants.MapConstants

import se.ju.student.robomow.ui.view.MapView
import java.lang.Float.min

object MapUtils {

    fun drawStartPosition(canvas: Canvas, x: Float, y: Float) {
        val radius = MapConstants.PATH_STROKE_WIDTH * 2
        canvas.drawCircle(x, y, radius, MapConstants.startPositionPaint)
        canvas.drawText("START", x, y + (MapConstants.startTextPaint.textSize / 3), MapConstants.startTextPaint)
    }

    fun calculateAngle(p1: Position, p2: Position): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val angleInRadians = Math.atan2(dy.toDouble(), dx.toDouble())
        return Math.toDegrees(angleInRadians).toFloat()
    }

    // Calculate the optimal scaleFactor and center of the path
    fun autoScale(
        positions: List<Position>,
        width: Int,
        height: Int,
        margin: Float,
        maxScaleFactor: Float
    ): Triple<Float, Float, Float> {
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

        val scaleFactor = min(min(scaleX, scaleY), maxScaleFactor)

        val pathCenterX = (minX + maxX) * scaleFactor / 2
        val pathCenterY = (minY + maxY) * scaleFactor / 2

        return Triple(scaleFactor, pathCenterX, pathCenterY)
    }
}