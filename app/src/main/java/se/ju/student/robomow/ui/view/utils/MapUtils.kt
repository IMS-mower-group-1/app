package se.ju.student.robomow.ui.view.utils

import android.graphics.Canvas
import se.ju.student.robomow.model.Position
import se.ju.student.robomow.ui.constants.MapConstants

import java.lang.Float.min

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
        val delta_x = end.x - start.x
        val delta_y = end.y - start.y
        val rotation = Math.toDegrees(Math.atan2(delta_y.toDouble(), delta_x.toDouble())).toFloat()
        return if (rotation < 0) rotation + 360 else rotation
    }
}