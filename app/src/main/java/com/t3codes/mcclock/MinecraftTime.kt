package com.t3codes.mcclock

import java.time.LocalDateTime
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor

/**
 * The maths behind Minecraft's clock item.
 *
 * A Minecraft world has 24 000 ticks in a day and tick 0 is sunrise (06:00). The clock item is an
 * animated dial of 64 frames; the frame is chosen from the world time with two vanilla steps:
 *
 *  1. `DimensionType#timeOfDay` warps the linear tick count so the dial lingers around noon and
 *     midnight. This is the same curve the sun and moon follow in the overworld sky.
 *  2. The item model compares that value against 64 predicates (0, 1/64, 2/64, ...) and picks the
 *     last one that is <= the value, i.e. `floor(value * 64)`.
 *
 * Mapping the *local* clock onto Minecraft's day with sunrise at 06:00 means the dial reads like a
 * real sun dial: the sun sits at the top at noon, the moon at the top at midnight.
 */
object MinecraftTime {

    const val DAY_TICKS = 24_000.0

    /** Sunrise, in ticks since the start of the Minecraft day. */
    const val SUNRISE_TICK = 0.0

    /** Sunset, in ticks since the start of the Minecraft day (18:00). */
    const val SUNSET_TICK = 12_000.0

    /** Vanilla `DimensionType#timeOfDay` for the overworld. */
    fun timeOfDay(dayTimeTicks: Double): Double {
        val d = frac(dayTimeTicks / DAY_TICKS - 0.25)
        val e = 0.5 - cos(d * PI) / 2.0
        return d + (e - d) / 3.0
    }

    /** The value the vanilla clock model's `time` predicate is compared against, in `[0, 1)`. */
    fun clockPredicate(dayTimeTicks: Double): Double = frac(timeOfDay(dayTimeTicks))

    /** Dial frame `0..63`, the index of `clock_XX.png` the vanilla clock item would show. */
    fun clockFrame(dayTimeTicks: Double): Int {
        val frame = floor(clockPredicate(dayTimeTicks) * 64.0).toInt()
        return frame.coerceIn(0, 63)
    }

    /** Ticks since the last sunrise for a real wall-clock time (06:00 local == tick 0). */
    fun dayTimeFor(now: LocalDateTime): Double {
        val hours = now.hour + now.minute / 60.0 + now.second / 3600.0 + now.nano / 3.6e12
        val ticks = hours / 24.0 * DAY_TICKS - 6_000.0
        return modulo(ticks, DAY_TICKS)
    }

    /** Dial frame for a real wall-clock time. */
    fun frameFor(now: LocalDateTime): Int = clockFrame(dayTimeFor(now))

    /**
     * The Minecraft world time as `HH:MM`. Because the dial is mapped onto the local day with
     * sunrise at 06:00, this is simply the local time the player would see in game.
     */
    fun worldClockText(dayTimeTicks: Double): String {
        val minutes = modulo(dayTimeTicks / DAY_TICKS * 1440.0 + 360.0, 1440.0)
        val hour = (minutes / 60.0).toInt()
        val minute = (minutes % 60.0).toInt()
        return String.format(Locale.US, "%02d:%02d", hour, minute)
    }

    enum class Phase(val label: String) {
        SUNRISE("Sunrise"),
        DAY("Day"),
        SUNSET("Sunset"),
        NIGHT("Night"),
    }

    /** Roughly the sky phases of the overworld. */
    fun phase(dayTimeTicks: Double): Phase = when {
        dayTimeTicks < 1_100.0 -> Phase.SUNRISE
        dayTimeTicks < 11_700.0 -> Phase.DAY
        dayTimeTicks < 13_300.0 -> Phase.SUNSET
        dayTimeTicks < 22_800.0 -> Phase.NIGHT
        else -> Phase.SUNRISE
    }

    private fun frac(value: Double): Double = value - floor(value)

    private fun modulo(value: Double, modulus: Double): Double =
        ((value % modulus) + modulus) % modulus
}
