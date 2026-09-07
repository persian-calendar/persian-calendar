# Shared upstream sources

The JVM-only dependencies below are compiled from source for Android, JVM tests,
and Wasm. Package names and numerical algorithms are retained. Wear continues
to use its existing published dependencies.

| Source | Pinned revision | Adaptation |
| --- | --- | --- |
| [calendar](https://github.com/persian-calendar/calendar) | `6e54d1ce5044bf751ed1c0ef8faa9c4a95b55ee3` | Explicit optional JVM annotation |
| [praytimes](https://github.com/persian-calendar/praytimes) | `619fd6fa2b61fea91f266b7c6d37dba48a6138df` | None |
| [qr](https://github.com/persian-calendar/qr) | `58da9a077e1acc2ba80b43635533117fea462c7b` | None |
| [calculator](https://github.com/persian-calendar/calculator) | `64d264a2c4c9eff6e7c060c3ff0bff0b15879f0e` | Common Kotlin parser implementing Grammar.g4/GrammarVisitor; JVM scientific-notation formatting retained across platforms |
| [Astronomy Engine](https://github.com/cosinekitty/astronomy) | `61dc07020aaa6885d2c7f688a4d82beaf6edb9ef` | Portable ISO formatting; platform lock around Pluto cache |
| [AOSP GeomagneticField](https://android.googlesource.com/platform/frameworks/base/+/android-16.0.0_r1/core/java/android/hardware/GeomagneticField.java) | `android-16.0.0_r1` | Common Kotlin implementation using the same WMM-2020 coefficients |

Original licenses are retained in `licenses/` and bundled in the website's
license view. Shared QR drawing, theme colors, and numeral formatting come from
this application's existing Android sources. Resource generation uses the
existing Android translations, map paths, cities, and pinned events submodule.

When updating a source revision, update its license and this table, review the
small platform adaptations, and run shared tests on both JVM and Wasm plus the
Android tests. Do not replace the source with an unrelated calculation library.
