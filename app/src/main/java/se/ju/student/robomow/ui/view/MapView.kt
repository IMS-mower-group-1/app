package se.ju.student.robomow.ui.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import se.ju.student.robomow.model.Position
import se.ju.student.robomow.R
import se.ju.student.robomow.model.AvoidedCollisions
import se.ju.student.robomow.ui.constants.MapConstants
import se.ju.student.robomow.ui.view.utils.MapUtils


class MapView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val pathPaint = MapConstants.pathPaint
    private val borderPaint = MapConstants.borderPaint
    private val mowerImagePaint = MapConstants.mowerImagePaint
    private val startPositionPaint = MapConstants.startPositionPaint
    private val startTextPaint = MapConstants.startTextPaint
    private val collisionPaint = MapConstants.collisionPaint
    private val avoidedCollisions = mutableListOf<Pair<RectF, AvoidedCollisions>>()
    var listener: CollisionAvoidanceListener? = null

    private val path = Path()
    private var scaleFactor = 50f
    private var positions: List<Position> = emptyList()
    private var centerX: Float = 0f
    private var centerY: Float = 0f

    // Bitmap and matrix for the mower image
    private val mowerBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.husq_mower)
    private val scaledMowerBitmap: Bitmap = Bitmap.createScaledBitmap(
        mowerBitmap,
        (mowerBitmap.width * MapConstants.IMAGE_WIDTH_SCALE).toInt(), // Scale the width down from the original width
        (mowerBitmap.height * MapConstants.IMAGE_HEIGHT_SCALE).toInt(), // Scale the height down from the original height
        true
    )
    private val mowerMatrix = Matrix()

    // Bitmap for the grass texture background
    private val grassBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.grass_texture)
    private var scaledGrassBitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    interface CollisionAvoidanceListener {
        fun collisionAvoidancePressed(collision: AvoidedCollisions)
    }

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
        avoidedCollisions.forEach {
            canvas.drawRect(it.first, collisionPaint)
        }
        // Draw the mower at the last position on the path
        positions.takeLast(2).let { lastTwoPositions ->
            if (lastTwoPositions.size == 2) {
                val lastPosition = lastTwoPositions[1]
                val x = (lastPosition.x * scaleFactor) + centerX
                val y = (lastPosition.y * scaleFactor) + centerY
                val rotation = MapUtils.calculateAngle(lastTwoPositions[0], lastTwoPositions[1])
                drawMower(canvas, x, y, rotation)
            }
        }

        if (positions.isNotEmpty()) {
            val startX = (positions[0].x * scaleFactor) + centerX
            val startY = (positions[0].y * scaleFactor) + centerY
            MapUtils.drawStartPosition(canvas, startX, startY)
        }
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

    // Set the coordinates for the mower's path and redraw the view
    fun setCoordinates(newPositions: List<Position>?, avoidedCol: List<AvoidedCollisions>?) {
        if (width == 0 || height == 0) {
            post {
                setCoordinates(
                    newPositions,
                    avoidedCol
                )
            } // If the view is not yet laid out, post the action to the message queue
            return
        }

        newPositions?.let {
            positions = it.map { // Reverse the y-coordinates of each position
                Position(it.x, -it.y)
            }

            val (scaleFactor, pathCenterX, pathCenterY) = MapUtils.autoScale(
                positions,
                width,
                height,
                MapConstants.MARGIN,
                MapConstants.MAX_SCALE_FACTOR
            ) // Calculate the optimal scaleFactor and get the center of the path

            centerX = (width / 2f) - pathCenterX
            centerY = (height / 2f) - pathCenterY

            path.reset()
            positions.forEachIndexed { index, coordinate ->
                val x = (coordinate.x * scaleFactor) + centerX
                val y = (coordinate.y * scaleFactor) + centerY

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    //if the current coordinate matches a collisionAvoidance, add it to the list
                    //This way a rect is bound to a avoidedCollision and the image can be fetched.
                    if (coordinate.x == 4) {
                        avoidedCollisions.add(Pair(RectF(x, y, x + 18, y + 18), avoidedCol!![0]))
                    }
                    path.lineTo(x, y)
                }
            }
            scaledGrassBitmap = Bitmap.createScaledBitmap(grassBitmap, width, height, true)
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                println("test")
                avoidedCollisions.forEach {
                    if (isCollisionAvoidanceEvent(x, y, it.first)) {
                        listener?.collisionAvoidancePressed(it.second)
                        return true
                    }
                }
            }
        }
        return true
    }

    private fun isCollisionAvoidanceEvent(
        x: Float,
        y: Float,
        avoidedCollisionRect: RectF
    ): Boolean {
        val rectPadding = 20
        if (x >= avoidedCollisionRect.left - rectPadding && x <= avoidedCollisionRect.right + rectPadding && y >= avoidedCollisionRect.top - rectPadding && y <= avoidedCollisionRect.bottom + rectPadding) {
            return true
        }
        return false
    }
}
