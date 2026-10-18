package com.coobyk.mc.clock

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Loads the original 16x16 dial frames and draws them with nearest neighbour scaling, snapped to
 * whole multiples of a source pixel so every block of the texture stays exactly the same size.
 */
object DialRenderer {

    private val cache = HashMap<Int, Bitmap?>()

    fun source(context: Context, frame: Int): Bitmap? {
        val index = ClockArt.wrap(frame)
        if (cache.containsKey(index)) return cache[index]

        val bitmap = try {
            context.assets.open(ClockArt.assetName(index)).use { stream ->
                BitmapFactory.decodeStream(
                    stream,
                    null,
                    BitmapFactory.Options().apply {
                        inScaled = false
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    },
                )
            }
        } catch (t: Throwable) {
            null
        }

        cache[index] = bitmap
        return bitmap
    }

    /** Draws [source] centred inside [area] using only whole-pixel blocks. */
    fun drawSnapped(canvas: Canvas, source: Bitmap, area: RectF, paint: Paint) {
        val cell = max(1f, floor(min(area.width(), area.height()) / source.width))
        val size = cell * source.width
        val left = area.left + (area.width() - size) / 2f
        val top = area.top + (area.height() - size) / 2f

        paint.isAntiAlias = false
        paint.isFilterBitmap = false
        paint.isDither = false
        canvas.drawBitmap(source, null, RectF(left, top, left + size, top + size), paint)
    }
}
