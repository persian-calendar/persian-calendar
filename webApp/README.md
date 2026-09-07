# Persian Calendar website

Compose Multiplatform / Kotlin-Wasm website, with no backend, service worker,
installation manifest, accounts, or personal events. Android keeps its native
integrations; the web navigation omits location detection, audio, shift work,
globe, compass, and level.

## Build and run

Use the project's JDK 21 and Android SDK setup. Initialize the bundled dataset:

```shell
git submodule update --init PersianCalendar/data/events
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

On Windows use `gradlew.bat`. For a production distribution:

```shell
./gradlew :webApp:wasmJsBrowserDistribution
node scripts/serve-web.mjs
```

Open `http://127.0.0.1:8080`. The static output is
`webApp/build/dist/wasmJs/productionExecutable`. Upload that directory's entire
contents to an HTTPS static host. Serve `.wasm` files with `application/wasm` and
JavaScript modules with a JavaScript MIME type. Hash navigation requires no server
rewrite rules. No deployment is performed by these commands.

Kotlin downloads its pinned Node and Binaryen tools on the first build. Where
Node 24 is already installed, add `-PuseSystemNode=true` to reuse it. If the
Windows environment points Gradle at `C:\.gradle`, set `GRADLE_USER_HOME` to the
existing user Gradle cache before building.

## Shared code and data

`shared` compiles the pinned calendar, prayer-time, calculator, QR, and astronomy
implementations for Android and Wasm. Android consumes this module instead of
the corresponding JVM artifacts. Numeral formatting, default color schemes, and
QR drawing are extracted from the Android source. The browser application and
its other composables live in common code, behind the `AppPlatform` interface;
the browser adapter is in `webApp`.

The `generateSharedResources` task reads the existing translations, cities,
events submodule, map paths, and license files. Edit those source datasets,
not generated output. Upstream source revisions and porting notes are recorded
in `shared/UPSTREAM.md`.

Web settings are stored only in this browser. Prayer times use manually selected
coordinates and an explicitly selected time zone; changing a city does not infer
its time zone. Magnetic overlays use Android 16's WMM-2020 model. Sensor and
background-execution permissions are never requested.

## Validation

```shell
./gradlew :shared:jvmTest :shared:wasmJsBrowserTest
./gradlew :PersianCalendar:assembleDebug :PersianCalendar:testDebugUnitTest
./gradlew :webApp:wasmJsBrowserDistribution
```

Shared tests cover calendar boundaries, numeral grouping, calculator grammar,
prayer times, astronomy, event recurrence, QR output, and magnetic-field parity
with the original Android implementation. Web CI stores the static distribution
as an artifact without publishing it.

Wasm tests run in headless Chrome because shared Compose code initializes Skia,
which needs a browser resource loader. Set `CHROME_BIN` if Chrome is not found
automatically. Browser-adapter unit tests also run with
`node --test webApp/platform.test.mjs`.
