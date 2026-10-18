<img src="assets/icon.svg" width="64" height="64" alt="">

# MCClockWidget

An Android app whose home screen widget shows the time on **Minecraft's clock item** — the real
64-frame dial from the game, as large as the widget allows.

Created in [T3 Code](https://t3.codes).

## What the widget shows

* The clock dial, using `clock_00.png` … `clock_63.png` — and nothing else. No panel behind it and
  no digital readout, so the clock item fills the whole widget and grows with it as you resize.
* The time is still available to screen readers: it lives in the content description rather than on
  screen. Tap the widget to open the app.

The app screen keeps the trimmings the widget drops: a large live dial, the local time, the
Minecraft world time, the day time in ticks, the dial frame and the sky phase.

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
app/src/main/assets/clocks/        original frames (used by the app screen)
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

Download the APK from the [latest
release](https://github.com/Coobyk/mcclockwidget/releases/latest):

```sh
adb install mcclockwidget-1.0.0.apk
```

Or build one yourself with `./gradlew :app:assembleDebug` and install
`app/build/outputs/apk/debug/app-debug.apk`.

Then long-press the home screen, choose *Widgets*, and pick **Minecraft Clock** — or open the app
and tap *Add to home screen*. The widget is resizable from 1x1 upwards, and the dial scales to fill
whatever size you give it.

## Releasing

Pushing a tag that looks like `v1.2.3` runs `.github/workflows/release.yml`, which runs the tests,
builds a signed release APK and attaches it to a GitHub Release named after the tag:

```sh
git tag v1.0.0
git push origin v1.0.0
```

`versionName` comes from the tag and `versionCode` from the workflow run number, so every release
installs over the last one. Re-running the workflow from the *Actions* tab with the tag as its input
replaces the APK on an existing release rather than failing.

`.github/workflows/ci.yml` runs on every push to `master` and every pull request: unit tests, lint
and a debug APK.

### The signing key

Release builds are signed with `keys/mcclock-release.jks`, which is not in the repo. The same key is
stored in the repository's Actions secrets:

| Secret | |
| --- | --- |
| `MCCLOCK_KEYSTORE_BASE64` | the keystore, base64-encoded |
| `MCCLOCK_KEYSTORE_PASSWORD` | the store password |
| `MCCLOCK_KEY_ALIAS` | `mcclock` |
| `MCCLOCK_KEY_PASSWORD` | the key password |

**Keep a copy of `keys/` somewhere safe.** The key cannot be recreated, and losing it means no future
release can be installed over an existing one. Without those four environment variables
`./gradlew :app:assembleRelease` still builds — it just leaves the APK unsigned, as
`app-release-unsigned.apk`.

## Credits

* Clock textures: the 64 frames from Minecraft's `clock` item.
* UI pixel font: [Silkscreen](https://github.com/googlefonts/silkscreen) by Jason Kottke,
  SIL Open Font License 1.1 — see `third_party/silkscreen/OFL.txt`.
