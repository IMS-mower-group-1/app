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

    private val grassTexture: BitmapDrawable = context.getDrawable(R.drawable.grass_texture) as BitmapDrawable
    // New paint object with grassTexture as shader
    val paint = Paint().apply {
        shader = BitmapShader(
            grassTexture.bitmap,
            Shader.TileMode.REPEAT,
            Shader.TileMode.REPEAT
        )
    }

    interface CollisionAvoidanceListener {
        fun onCollisionAvoidanceClicked(collision: AvoidedCollisions)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //Center the view on 0,0
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

        canvas.translate(translationX, translationY)
        canvas.scale(scaleFactor, scaleFactor)
        canvas.drawPath(path, MapConstants.pathPaint)
        collisionAvoidanceCircleAndAvoidedCollisions.forEach {
            canvas.drawCircle(it.first.x, it.first.y, it.first.radius, collisionPaint)
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
                val canvasX = (coordinate.x * scaleFactor * scaleConstant)
                val canvasY = (coordinate.y * scaleFactor * scaleConstant)

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
}
