package se.ju.student.robomow.ui.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import se.ju.student.robomow.R
import se.ju.student.robomow.data.CollisionAvoidanceCircle
import se.ju.student.robomow.model.AvoidedCollisions
import se.ju.student.robomow.model.Position
import se.ju.student.robomow.ui.constants.MapConstants
import se.ju.student.robomow.ui.constants.MapConstants.collisionPaint
import se.ju.student.robomow.ui.view.utils.MapUtils
import kotlin.math.min
import kotlin.math.max

class ZoomableMapView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleGestureListener())
    private val gestureDetector = GestureDetector(context, PanGestureListener())
    private val scaleConstant = 80
    private var scaleFactor = 1.0f
    private var translationX = 0.0f
    private var translationY = 0.0f
    private val maxScale = 3.0f
    private val minScale = 0.5f

    private val path = Path()
    private var positions: List<Position> = emptyList()
    private val collisionAvoidanceCircleAndAvoidedCollisions =
        mutableListOf<Pair<CollisionAvoidanceCircle, AvoidedCollisions>>()

    var listener: CollisionAvoidanceListener? = null

    private val grassTexture: BitmapDrawable =
        context.getDrawable(R.drawable.grass_texture) as BitmapDrawable

    // New paint object with grassTexture as shader
    val paint = Paint().apply {
        shader = BitmapShader(
            grassTexture.bitmap,
            Shader.TileMode.REPEAT,
            Shader.TileMode.REPEAT
        )
    }

    // Bitmap and matrix for the mower image
    private val mowerBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.husq_mower)
    private val scaledMowerBitmap: Bitmap = Bitmap.createScaledBitmap(
        mowerBitmap,
        (mowerBitmap.width * MapConstants.IMAGE_WIDTH_SCALE).toInt(), // Scale the width down from the original width
        (mowerBitmap.height * MapConstants.IMAGE_HEIGHT_SCALE).toInt(), // Scale the height down from the original height
        true
    )
    private val mowerMatrix = Matrix()

    interface CollisionAvoidanceListener {
        fun onCollisionAvoidanceClicked(collision: AvoidedCollisions)
        fun onInformationOverviewClicked()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //Center the view on 0,0
        val centeredWidth = width / 2f
        val centeredHeight = height / 2f
        canvas.translate(width / 2f, height / 2f)
        canvas.save()

        // Draw a rectangle that fills the canvas with our paint object which is a grass texture
        canvas.drawRect(
            -width / 2f,
            -height / 2f,
            width / 2f,
            height / 2f,
            paint
        )
        canvas.drawRect(
            centeredWidth - 50,
            -centeredHeight,
            centeredWidth,
            -centeredHeight + 59,
            collisionPaint
        )
        canvas.translate(translationX, translationY)
        canvas.scale(scaleFactor, scaleFactor)
        //canvas.drawRect((w-500),h,w,h+500 ,Paint().apply { Color.WHITE })

        // Draw the mower at the last position on the path
        positions.takeLast(2).let { lastTwoPositions ->
            if (lastTwoPositions.size == 2) {
                val lastPosition = lastTwoPositions[1]
                val canvasX = (lastPosition.x * scaleConstant)
                val canvasY = (lastPosition.y * scaleConstant)
                val rotation = calculateAngle(lastTwoPositions[0], lastTwoPositions[1])
                drawMower(canvas, canvasX, canvasY, rotation)
            }
        }

        canvas.drawPath(path, MapConstants.pathPaint)
        collisionAvoidanceCircleAndAvoidedCollisions.forEach {
            canvas.drawCircle(it.first.x, it.first.y, it.first.radius, collisionPaint)
        }

        // Draw the start position
        if (positions.isNotEmpty()) {
            val startX = (positions[0].x * scaleConstant)
            val startY = (positions[0].y * scaleConstant)
            MapUtils.drawStartPosition(canvas, startX, startY)
        }

        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val canvasX = (event.x - width / 2f - translationX) / scaleFactor
                val canvasY = (event.y - height / 2f - translationY) / scaleFactor
                if (isInformationBoxClicked(event.x,event.y)){
                    listener?.onInformationOverviewClicked()
                    return true
                }

                collisionAvoidanceCircleAndAvoidedCollisions.forEach {
                    if (isCollisionAvoidanceClicked(canvasX, canvasY, it.first)) {
                        listener?.onCollisionAvoidanceClicked(it.second)
                        return true
                    }
                }
            }
        }
        return true
    }

    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            detector.let {
                scaleFactor *= it.scaleFactor
                scaleFactor = max(minScale, min(scaleFactor, maxScale))
                invalidate()
            }
            return true
        }
    }

    private inner class PanGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            translationX -= distanceX
            translationY -= distanceY
            invalidate()
            return true
        }
    }

    fun setCoordinates(newPositions: List<Position>?, avoidedCollisions: List<AvoidedCollisions>?) {
        if (width == 0 || height == 0) {
            post {
                setCoordinates(
                    newPositions,
                    avoidedCollisions
                )
            } // If the view is not yet laid out, post the action to the message queue
            return
        }

        newPositions?.let {
            positions = it.map { // Reverse the y-coordinates of each position
                Position(it.x, -it.y)
            }

            path.reset()
            positions.forEachIndexed { index, coordinate ->
                val canvasX = (coordinate.x * scaleConstant)
                val canvasY = (coordinate.y * scaleConstant)

                if (index == 0) {
                    path.moveTo(canvasX, canvasY)
                } else {
                    addCollisionAvoidanceCircleAndCollisionAvoidance(
                        coordinate.x,
                        coordinate.y,
                        avoidedCollisions,
                        canvasX,
                        canvasY
                    )
                    path.lineTo(canvasX, canvasY)
                }
            }
            //scaledGrassBitmap = Bitmap.createScaledBitmap(grassBitmap, width, height, true)
        }
        invalidate()
    }

    // Draw the mower image at the specified position and rotation
    private fun drawMower(canvas: Canvas, x: Float, y: Float, rotation: Float) {
        val halfWidth = scaledMowerBitmap.width / 2f
        val halfHeight = scaledMowerBitmap.height / 2f

        mowerMatrix.reset()
        mowerMatrix.postRotate(rotation, halfWidth, halfHeight)
        mowerMatrix.postTranslate(x - halfWidth, y - halfHeight)

        canvas.drawBitmap(
            scaledMowerBitmap,
            mowerMatrix,
            null
        ) // null can be replaced with a custom Paint object if needed
    }

    // Calculate the angle between two positions
    private fun calculateAngle(start: Position, end: Position): Float {
        val delta_x = end.x - start.x
        val delta_y = end.y - start.y
        val rotation = Math.toDegrees(Math.atan2(delta_y.toDouble(), delta_x.toDouble())).toFloat()
        return if (rotation < 0) rotation + 360 else rotation
    }

    private fun addCollisionAvoidanceCircleAndCollisionAvoidance(
        x: Float,
        y: Float,
        avoidedCollisions: List<AvoidedCollisions>?,
        canvasX: Float,
        canvasY: Float
    ) {
        avoidedCollisions?.let {
            val pos = Position(x, -y)
            val avoidedCollision = avoidedCollisions.find { it.position == pos }
            avoidedCollision?.let {
                avoidedCollision
                collisionAvoidanceCircleAndAvoidedCollisions.add(
                    Pair(
                        CollisionAvoidanceCircle(
                            canvasX,
                            canvasY
                        ), avoidedCollision
                    )
                )
            }
        }
    }

    private fun isCollisionAvoidanceClicked(
        x: Float,
        y: Float,
        collisionAvoidanceCircle: CollisionAvoidanceCircle
    ): Boolean {
        val circlePadding = 30
        return (x >= collisionAvoidanceCircle.x - circlePadding
                && x <= collisionAvoidanceCircle.x + circlePadding
                && y >= collisionAvoidanceCircle.y - circlePadding
                && y <= collisionAvoidanceCircle.y + circlePadding
                )
    }

    private fun isInformationBoxClicked(x: Float, y: Float) : Boolean{
        return (x >= (width-100)  && y <= 100)
    }
}
