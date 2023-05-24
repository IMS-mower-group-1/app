package se.ju.student.robomow.ui.constants

import android.graphics.Color
import android.graphics.Paint

object MapConstants {
    const val PATH_COLOR = 0xFF000000.toInt()
    const val BORDER_COLOR = 0xFF09A104.toInt()
    const val START_POSITION_COLOR = Color.BLUE
    const val START_POSITION_TEXT_COLOR = Color.WHITE
    const val AVOIDED_COLLISION_TEXT_COLOR = Color.WHITE
    const val TEXT_SIZE = 18f
    const val PATH_STROKE_WIDTH = 15f
    const val BORDER_STROKE_WIDTH = 20f
    const val MAX_SCALE_FACTOR = 50f
    const val MARGIN = 20f
    const val IMAGE_OPACITY = 220
    const val PATH_OPACITY = 150
    const val IMAGE_WIDTH_SCALE = 0.1
    const val IMAGE_HEIGHT_SCALE = 0.1

    val pathPaint = Paint().apply {
        color = PATH_COLOR
        style = Paint.Style.STROKE
        strokeWidth = PATH_STROKE_WIDTH
        alpha = PATH_OPACITY
    }
    val borderPaint = Paint().apply {
        color = BORDER_COLOR
        style = Paint.Style.STROKE
        strokeWidth = BORDER_STROKE_WIDTH
    }
    val mowerImagePaint = Paint().apply {
        alpha = IMAGE_OPACITY
    }
    val startPositionPaint = Paint().apply {
        color = START_POSITION_COLOR
    }
    val startTextPaint = Paint().apply {
        color = START_POSITION_TEXT_COLOR
        textSize = TEXT_SIZE
        textAlign = Paint.Align.CENTER
    }

    val collisionTextPaint = Paint().apply {
        color = AVOIDED_COLLISION_TEXT_COLOR
        textSize = TEXT_SIZE
        textAlign = Paint.Align.CENTER
    }

    val collisionPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = BORDER_STROKE_WIDTH
    }
}