package se.ju.student.robomow.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt



class JoystickView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    interface JoystickListener {
        fun onJoystickMoved(angle: Double, speed: Float)
    }

    var joystickListener: JoystickListener? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center.x = w / 2f
        center.y = h / 2f
    }

    // Constants for controlling the size and position of the joystick
    private val outerRadius = 200f
    private val innerRadius = 100f

    private val center = PointF(width / 2f, height / 2f)

    // Variables for tracking the position of the joystick
    private var joystickPosition = center
    private var isMoving = false

    // Paint objects for drawing the joystick
    private val outerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
    }
    private val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Draw the outer circle of the joystick
        canvas?.drawCircle(center.x, center.y, outerRadius, outerCirclePaint)

        // Draw the inner circle of the joystick at its current position
        canvas?.drawCircle(joystickPosition.x, joystickPosition.y, innerRadius, innerCirclePaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if the touch event is within the outer circle of the joystick
                val distanceFromCenter = getDistance(event.x, event.y, center.x, center.y)
                if (distanceFromCenter <= outerRadius) {
                    // Set the joystick position to the initial touch point
                    joystickPosition = PointF(event.x, event.y)
                    isMoving = true
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                var angleToRobot = 0.0
                var speedToRobot = 0.0f
                // If the user is already touching the joystick, update its position
                if (isMoving) {
                    joystickPosition = PointF(event.x, event.y)
                    // Make sure the joystick stays within the outer circle
                    val distanceFromCenter = getDistance(joystickPosition.x, joystickPosition.y, center.x, center.y)
                    if (distanceFromCenter > outerRadius) {
                        val angle = getPositioningAngle(center.x, center.y, joystickPosition.x, joystickPosition.y)
                        joystickPosition.x = center.x + (outerRadius * kotlin.math.cos(angle)).toFloat()
                        joystickPosition.y = center.y + (outerRadius * sin(angle)).toFloat()
                        speedToRobot = 1.0f
                    } else {
                        speedToRobot = distanceFromCenter/outerRadius
                    }
                    angleToRobot = getAngle(center.x, center.y, joystickPosition.x, joystickPosition.y)

                    joystickListener?.onJoystickMoved(angleToRobot, speedToRobot)


                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // If the user releases their touch, reset the joystick position
                joystickPosition = PointF(center.x, center.y)
                isMoving = false

                joystickListener?.onJoystickMoved(0.0, 0.0f)
                invalidate()
                return true
            }
        }
        return false
    }

    private fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y1 - y2
        return sqrt((dx * dx) + (dy * dy))
    }

    private fun getAngle(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        val dx = x1 - x2
        val dy = y1 - y2 // Flip vertically
        var angle = atan2(dy.toDouble(), dx.toDouble())

        // Rotate the angle by PI/2 counter-clockwise
        angle -= Math.PI / 2

        // Reverse the angle
        angle = Math.PI - angle

        // Normalize the angle to the range of -PI to PI
        if (angle > Math.PI) {
            angle -= 2 * Math.PI
        } else if (angle < -Math.PI) {
            angle += 2 * Math.PI
        }

        return angle
    }

    private fun getPositioningAngle(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        val dx = x2 - x1
        val dy = y2 - y1
        return atan2(dy.toDouble(), dx.toDouble())
    }
}