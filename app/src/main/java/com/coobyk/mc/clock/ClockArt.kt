package com.coobyk.mc.clock

import androidx.annotation.DrawableRes

/**
 * The 64 dial frames shipped with the app.
 *
 * `FRAME_RES[i]` is the drawable used by the home screen widget, and `assetName(i)` is the crisp
 * 16x16 original used by [ClockView] on the app's own screen.
 */
object ClockArt {

    const val FRAME_COUNT = 64

    @DrawableRes
    val FRAME_RES: IntArray = intArrayOf(
        R.drawable.clock_00, R.drawable.clock_01, R.drawable.clock_02, R.drawable.clock_03,
        R.drawable.clock_04, R.drawable.clock_05, R.drawable.clock_06, R.drawable.clock_07,
        R.drawable.clock_08, R.drawable.clock_09, R.drawable.clock_10, R.drawable.clock_11,
        R.drawable.clock_12, R.drawable.clock_13, R.drawable.clock_14, R.drawable.clock_15,
        R.drawable.clock_16, R.drawable.clock_17, R.drawable.clock_18, R.drawable.clock_19,
        R.drawable.clock_20, R.drawable.clock_21, R.drawable.clock_22, R.drawable.clock_23,
        R.drawable.clock_24, R.drawable.clock_25, R.drawable.clock_26, R.drawable.clock_27,
        R.drawable.clock_28, R.drawable.clock_29, R.drawable.clock_30, R.drawable.clock_31,
        R.drawable.clock_32, R.drawable.clock_33, R.drawable.clock_34, R.drawable.clock_35,
        R.drawable.clock_36, R.drawable.clock_37, R.drawable.clock_38, R.drawable.clock_39,
        R.drawable.clock_40, R.drawable.clock_41, R.drawable.clock_42, R.drawable.clock_43,
        R.drawable.clock_44, R.drawable.clock_45, R.drawable.clock_46, R.drawable.clock_47,
        R.drawable.clock_48, R.drawable.clock_49, R.drawable.clock_50, R.drawable.clock_51,
        R.drawable.clock_52, R.drawable.clock_53, R.drawable.clock_54, R.drawable.clock_55,
        R.drawable.clock_56, R.drawable.clock_57, R.drawable.clock_58, R.drawable.clock_59,
        R.drawable.clock_60, R.drawable.clock_61, R.drawable.clock_62, R.drawable.clock_63,
    )

    /** Wraps any index into `0..63`. */
    fun wrap(index: Int): Int = ((index % FRAME_COUNT) + FRAME_COUNT) % FRAME_COUNT

    @DrawableRes
    fun frameRes(index: Int): Int = FRAME_RES[wrap(index)]

    /** Asset path of the original 16x16 frame, e.g. `clocks/clock_07.png`. */
    fun assetName(index: Int): String =
        String.format(java.util.Locale.US, "clocks/clock_%02d.png", wrap(index))
}
