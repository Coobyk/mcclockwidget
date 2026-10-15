package com.t3codes.mcclock.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import java.io.File
import org.junit.Assert.assertNotNull
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
 * `app/build/widget-renders/` keeps the PNGs for eyeballing afterwards.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WidgetRenderTest {

    @Test
    fun `square widget renders`() = render(110, 110, 330, 330, "square")

    @Test
    fun `wide widget renders`() = render(250, 110, 750, 330, "wide")

    @Test
    fun `small widget renders`() = render(70, 70, 210, 210, "small")

    private fun render(minWidthDp: Int, minHeightDp: Int, widthPx: Int, heightPx: Int, name: String) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val remoteViews = ClockWidgetProvider.buildRemoteViews(context, minWidthDp, minHeightDp)

        val root = remoteViews.apply(context, FrameLayout(context))
        assertNotNull("remote views failed to inflate", root)

        root.measure(
            View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY),
        )
        root.layout(0, 0, widthPx, heightPx)

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        root.draw(Canvas(bitmap))

        assertTrue("nothing was drawn", hasVisiblePixels(bitmap))

        val directory = File("build/widget-renders")
        directory.mkdirs()
        directory.resolve("$name.png").outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    private fun hasVisiblePixels(bitmap: Bitmap): Boolean {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return pixels.any { it ushr 24 != 0 }
    }
}
