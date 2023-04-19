package se.ju.student.robomow.ui.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import se.ju.student.robomow.model.Position
import java.lang.Float.min
import se.ju.student.robomow.R

class MapView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    // Constants and paint objects used for drawing the map
    companion object {
        private const val PATH_COLOR = 0xFF000000.toInt() // Light green
        private const val BORDER_COLOR = 0xFF09A104.toInt() // Green
        private const val PATH_STROKE_WIDTH = 30f
        private const val BORDER_STROKE_WIDTH = 20f
        private const val MAX_SCALE_FACTOR = 50f
        private const val MARGIN = 20f
        private const val IMAGE_OPACITY = 220 // Set the opacity between 0 and 255
        private const val IMAGE_WIDTH_SCALE = 0.1
        private const val IMAGE_HEIGHT_SCALE = 0.1
    }

    // Paint objects for path, border, and mower image
    private val pathPaint = Paint().apply {
        color = PATH_COLOR
        style = Paint.Style.STROKE
        strokeWidth = PATH_STROKE_WIDTH
        alpha = 150
    }
    private val borderPaint = Paint().apply {
        color = BORDER_COLOR
        style = Paint.Style.STROKE
        strokeWidth = BORDER_STROKE_WIDTH
    }
    private val mowerImagePaint = Paint().apply {
        alpha = IMAGE_OPACITY
    }

    // Path, scaleFactor, positions, and center coordinates
    private val path = Path()
    private var scaleFactor = 50f
    private var positions: List<Position> = emptyList()
    private var centerX: Float = 0f
    private var centerY: Float = 0f

    // Bitmap and matrix for the mower image
    private val mowerBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.husq_mower)
    private val scaledMowerBitmap: Bitmap = Bitmap.createScaledBitmap(
        mowerBitmap,
        (mowerBitmap.width * IMAGE_WIDTH_SCALE).toInt(), // Scale the width down from the original width
        (mowerBitmap.height * IMAGE_HEIGHT_SCALE).toInt(), // Scale the height down from the original height
        true
    )
    private val mowerMatrix = Matrix()

    // Bitmap for the grass texture background
    private val grassBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.grass_texture)
    private var scaledGrassBitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(scaledGrassBitmap, 0f, 0f, null)

        // Draw the border around the map
        canvas.drawRect(
            borderPaint.strokeWidth / 2,
            borderPaint.strokeWidth / 2,
            width - borderPaint.strokeWidth / 2,
            height - borderPaint.strokeWidth / 2,
            borderPaint
        )

        // Draw the path of the mower
        canvas.save()
        canvas.drawPath(path, pathPaint)
        canvas.restore()

        // Draw the mower at the last position on the path
        positions.takeLast(2).let { lastTwoPositions ->
            if (lastTwoPositions.size == 2) {
                val lastPosition = lastTwoPositions[1]
                val x = (lastPosition.x * scaleFactor) + centerX
                val y = (lastPosition.y * scaleFactor) + centerY
                val rotation = calculateAngle(lastTwoPositions[0], lastTwoPositions[1])
                drawMower(canvas, x, y, rotation)
            }
        }
    }

    // Calculate the angle between two positions in degrees
    private fun calculateAngle(p1: Position, p2: Position): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val angleInRadians = Math.atan2(dy.toDouble(), dx.toDouble())
        return Math.toDegrees(angleInRadians).toFloat()
    }

    // Draw the mower image at the specified position and rotation
    private fun drawMower(canvas: Canvas, x: Float, y: Float, rotation: Float) {
        val halfWidth = scaledMowerBitmap.width / 2f
        val halfHeight = scaledMowerBitmap.height / 2f

        mowerMatrix.reset()
        mowerMatrix.postRotate(rotation, halfWidth, halfHeight)
        mowerMatrix.postTranslate(x - halfWidth, y - halfHeight)

        canvas.drawBitmap(scaledMowerBitmap, mowerMatrix, mowerImagePaint)
    }

    // Calculate the optimal scaleFactor and center of the path
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

    // Set the coordinates for the mower's path and redraw the view
    fun setCoordinates(newPositions: List<Position>?) {
        if (width == 0 || height == 0) {
            post { setCoordinates(newPositions) } // If the view is not yet laid out, post the action to the message queue
            return
        }

        newPositions?.let {
            positions = it

            val (pathCenterX, pathCenterY) = autoScale(positions) // Calculate the optimal scaleFactor and get the center of the path

            centerX = (width / 2f) - pathCenterX
            centerY = (height / 2f) - pathCenterY

            path.reset()
            positions.forEachIndexed { index, coordinate ->
                val x = (coordinate.x * scaleFactor) + centerX
                val y = (coordinate.y * scaleFactor) + centerY

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            scaledGrassBitmap = Bitmap.createScaledBitmap(grassBitmap, width, height, true)
        }
        invalidate()
    }
}
