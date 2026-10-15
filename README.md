<img src="assets/icon.svg" width="64" height="64" alt="">

# MCClockWidget

An Android app whose home screen widget shows the time on **Minecraft's clock item** — the real
64-frame dial from the game, plus a digital readout in a pixel font.

Created in [T3 Code](https://t3.codes).

## What the widget shows

* The clock dial, using `clock_00.png` … `clock_63.png`.
* The current time as digits (`14:32`, or `6:12` on 12-hour devices). The widget leaves the
  meridiem off — it would halve the size of the digits, and the dial already says whether it is
  day or night. The app screen, which has room, adds it.
* A wide layout (dial beside the digits) and a square layout (dial above them), picked
  automatically on resize.

Tap the widget to open the app, which shows a large live clock plus the local time, the Minecraft
world time, the day time in ticks, the dial frame and the sky phase.

## How the dial is chosen

The dial is not decorative: it uses the same maths as the vanilla `clock` item.

1. A Minecraft day is 24 000 ticks and tick 0 is sunrise (06:00).
2. `DimensionType#timeOfDay` warps the linear tick count so the sun lingers around noon and
   midnight — the same curve the sky follows in game.
3. The item model compares that value against 64 predicates and picks the last one that is at most
   the value, i.e. `floor(value * 64)`.

This app maps the device's wall clock onto that day with sunrise at 06:00, so the dial reads like a
real sun dial:

| Local time | Dial frame | On the dial |
| --- | --- | --- |
| 06:00 | 50 | sun on the horizon |
| 12:00 | 0 | sun at the top |
| 18:00 | 13 | sun on the horizon |
| 00:00 | 32 | moon at the top |

The app is deliberately deterministic here, so the mapping is covered by unit tests — see
`app/src/test/java/com/t3codes/mcclock/MinecraftTimeTest.kt`.

## Project layout

```
app/src/main/java/com/t3codes/mcclock/
  MinecraftTime.kt          the vanilla clock maths
  ClockArt.kt               the 64 frames and their resource names
  DialRenderer.kt           nearest-neighbour drawing, snapped to whole source pixels
  ClockView.kt              the live dial on the app screen
  MainActivity.kt           the app screen
  widget/ClockWidgetProvider.kt   the widget
  widget/WidgetScheduler.kt       the once-a-minute wake-up
app/src/main/assets/clocks/        original 16x16 frames (used by the app screen)
app/src/main/res/drawable-nodpi/   the same frames at 10x, for the widget's ImageView
```

## Building

The app builds with the Gradle wrapper. Point `local.properties` at an Android SDK with
platform 35 and build-tools 35.0.0, then:

```sh
./gradlew :app:assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:test              # the clock-maths tests + the widget render tests
./gradlew :app:lintDebug         # Android Lint
```

`minSdk` is 26, `compileSdk`/`targetSdk` are 35, and the build runs on JDK 17 or newer.

`WidgetRenderTest` inflates the widget's real `RemoteViews`, draws them and writes PNGs to
`app/build/widget-renders/`, so a broken layout or a missing dial fails the build instead of only
showing up on a phone.

## Installing

```sh
adb install dist/mcclockwidget-1.0-debug.apk
```

Then long-press the home screen, choose *Widgets*, and pick **Minecraft Clock** — or open the app
and tap *Add to home screen*. The widget is resizable from 1x1 upwards; at 1x1 it shows the dial
with compact digits, and it switches to a wide layout once it is wider than it is tall.

## Credits

* Clock textures: the 64 frames from Minecraft's `clock` item.
* UI pixel font: [Silkscreen](https://github.com/googlefonts/silkscreen) by Jason Kottke,
  SIL Open Font License 1.1 — see `third_party/silkscreen/OFL.txt`.
