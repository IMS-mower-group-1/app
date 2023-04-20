package se.ju.student.robomow.ui.view.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView

class ItemDividerDecoration(
    private val dividerHeight: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = dividerHeight
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (child in parent.children) {
            val top = child.bottom
            val bottom = top + dividerHeight
            val paint = Paint()
            paint.color = Color.GRAY
            c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(),bottom.toFloat(), paint)
        }
    }
}