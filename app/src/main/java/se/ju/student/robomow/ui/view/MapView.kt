package se.ju.student.robomow.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import se.ju.student.robomow.ui.MapActivity

class MapView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint()
    private val path = Path()
    private val scaleFactor = 50f // Adjust this value to control the scaling of the distance between points

    // Define border and margin properties
    private val borderWidth = 10f
    private val borderColor = 0xFF000000.toInt()
    private val margin = 20f

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

    fun setCoordinates(coordinates: List<MapActivity.Coordinate>) {
        val scalingFactor = 150f // Adjust this value to control the scaling of the distance between points
        path.reset()
        coordinates.forEachIndexed { index, coordinate ->
            val x = coordinate.x * scalingFactor
            val y = coordinate.y * scalingFactor
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        invalidate()
    }
}
