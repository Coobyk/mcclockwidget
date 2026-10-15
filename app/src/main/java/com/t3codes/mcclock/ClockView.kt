package com.t3codes.mcclock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.time.LocalDateTime

/**
 * The Minecraft clock dial, drawn live. Uses the crisp 16x16 textures so the pixels stay square
 * at any size.
 */
class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val area = RectF()

    var frame: Int = MinecraftTime.frameFor(LocalDateTime.now())
        private set

    /** Recomputes the dial frame from the current time. Returns true when it changed. */
    fun refresh(): Boolean {
        val next = MinecraftTime.frameFor(LocalDateTime.now())
        if (next == frame) return false
        frame = next
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        area.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (width - paddingRight).toFloat(),
            (height - paddingBottom).toFloat(),
        )
        if (area.width() <= 0f || area.height() <= 0f) return

        val source = DialRenderer.source(context, frame) ?: return
        DialRenderer.drawSnapped(canvas, source, area, paint)
    }
}
