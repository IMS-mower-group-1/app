package se.ju.student.robomow.ui.view.utils

import android.graphics.Canvas
import se.ju.student.robomow.model.Position
import se.ju.student.robomow.ui.constants.MapConstants

import kotlin.math.atan2

object MapUtils {

    fun drawStartPosition(canvas: Canvas, x: Float, y: Float) {
        val radius = MapConstants.PATH_STROKE_WIDTH * 2
        canvas.drawCircle(x, y, radius, MapConstants.startPositionPaint)
        canvas.drawText(
            "START",
            x,
            y + (MapConstants.startTextPaint.textSize / 3),
            MapConstants.startTextPaint
        )
    }

    // Calculate the angle between two positions
    fun calculateAngle(start: Position, end: Position): Float {
        val deltaX = end.x - start.x
        val deltaY = end.y - start.y
        val rotation = Math.toDegrees(atan2(deltaY.toDouble(), deltaX.toDouble())).toFloat()
        return if (rotation < 0) rotation + 360 else rotation
    }
}