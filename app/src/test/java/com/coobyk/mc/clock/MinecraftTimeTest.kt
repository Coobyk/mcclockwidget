package com.coobyk.mc.clock

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the dial maths to the frames the vanilla clock item actually shows, so a refactor cannot
 * quietly rotate the sun.
 */
class MinecraftTimeTest {

    @Test
    fun `noon puts the sun at the top of the dial`() {
        // dayTime 6000 is noon in Minecraft.
        assertEquals(0, MinecraftTime.clockFrame(6000.0))
    }

    @Test
    fun `midnight puts the moon at the top of the dial`() {
        // dayTime 18000 is midnight.
        assertEquals(32, MinecraftTime.clockFrame(18000.0))
    }

    @Test
    fun `sunrise and sunset land on the expected frames`() {
        assertEquals(50, MinecraftTime.clockFrame(0.0))
        assertEquals(13, MinecraftTime.clockFrame(12000.0))
    }

    @Test
    fun `the local clock is mapped with sunrise at six`() {
        assertEquals(0, MinecraftTime.frameFor(LocalDateTime.of(2026, 10, 15, 12, 0)))
        assertEquals(32, MinecraftTime.frameFor(LocalDateTime.of(2026, 10, 15, 0, 0)))
        assertEquals(50, MinecraftTime.frameFor(LocalDateTime.of(2026, 10, 15, 6, 0)))
        assertEquals(13, MinecraftTime.frameFor(LocalDateTime.of(2026, 10, 15, 18, 0)))
    }

    @Test
    fun `world clock text matches the vanilla 06-00 sunrise`() {
        assertEquals("06:00", MinecraftTime.worldClockText(0.0))
        assertEquals("12:00", MinecraftTime.worldClockText(6000.0))
        assertEquals("18:00", MinecraftTime.worldClockText(12000.0))
        assertEquals("00:00", MinecraftTime.worldClockText(18000.0))
    }

    @Test
    fun `the frame is always a valid texture index`() {
        for (second in 0 until 86_400 step 37) {
            val now = LocalDateTime.of(2026, 1, 1, 0, 0).plusSeconds(second.toLong())
            val dayTime = MinecraftTime.dayTimeFor(now)
            assertTrue("dayTime out of range: $dayTime", dayTime >= 0.0 && dayTime < 24_000.0)

            val frame = MinecraftTime.frameFor(now)
            assertTrue("frame out of range: $frame", frame in 0 until ClockArt.FRAME_COUNT)
        }
    }

    @Test
    fun `the dial sweeps the whole texture set exactly once a day`() {
        val seen = HashSet<Int>()
        for (minute in 0 until 1440 step 5) {
            val now = LocalDateTime.of(2026, 6, 1, 0, 0).plusMinutes(minute.toLong())
            seen += MinecraftTime.frameFor(now)
        }
        assertEquals(ClockArt.FRAME_COUNT, seen.size)
    }

    @Test
    fun `every dial frame has art`() {
        assertEquals(ClockArt.FRAME_COUNT, ClockArt.FRAME_RES.size)
        assertEquals("clocks/clock_00.png", ClockArt.assetName(0))
        assertEquals("clocks/clock_63.png", ClockArt.assetName(63))
        assertEquals("clocks/clock_00.png", ClockArt.assetName(64))
        assertEquals("clocks/clock_63.png", ClockArt.assetName(-1))
    }
}
