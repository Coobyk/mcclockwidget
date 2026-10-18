package com.coobyk.mc.clock.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.coobyk.mc.clock.R
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Inflates the remote views the widget ships and draws them for real, so a broken layout, a
 * missing dial resource or a font that will not load fails the build.
 *
 * The widget is deliberately bare — the dial, no panel behind it and nothing written on it — so
 * those are pinned here too. `app/build/widget-renders/` keeps the PNGs for eyeballing afterwards.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WidgetRenderTest {

    @Test
    fun `square widget renders`() = render(330, 330, "square")

    @Test
    fun `wide widget renders`() = render(750, 330, "wide")

    @Test
    fun `small widget renders`() = render(210, 210, "small")

    @Test
    fun `big widget renders`() = render(990, 990, "big")

    private fun render(widthPx: Int, heightPx: Int, name: String) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val remoteViews = ClockWidgetProvider.buildRemoteViews(context)

        val root = remoteViews.apply(context, FrameLayout(context))
        assertNotNull("remote views failed to inflate", root)

        assertNull("the widget must not paint a background", root.background)
        assertTrue("the widget must not write anything: the dial is the whole story", texts(root).isEmpty())

        val dial = root.findViewById<View>(R.id.clock_dial)
        assertNotNull("the dial is missing", dial)

        root.measure(
            View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY),
        )
        root.layout(0, 0, widthPx, heightPx)

        assertEquals("the dial should fill the widget horizontally", widthPx, dial.width)
        assertEquals("the dial should fill the widget vertically", heightPx, dial.height)

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        root.draw(Canvas(bitmap))

        assertTrue("nothing was drawn", hasVisiblePixels(bitmap))

        val directory = File("build/widget-renders")
        directory.mkdirs()
        directory.resolve("$name.png").outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    private fun texts(view: View): List<TextView> {
        val found = mutableListOf<TextView>()
        if (view is TextView) found += view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) found += texts(view.getChildAt(i))
        }
        return found
    }

    private fun hasVisiblePixels(bitmap: Bitmap): Boolean {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return pixels.any { it ushr 24 != 0 }
    }
}
