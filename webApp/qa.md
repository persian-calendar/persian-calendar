# Web validation

Validated locally on 2026-09-08 with JDK 21 and Chrome 152 on Windows.

- Android debug APK assembled; 239 Android unit tests passed.
- Shared JVM and headless Chrome/Wasm suites: 16 tests each, all passed.
- Browser platform adapter: 4 Node tests passed.
- Production Wasm distribution and Spotless checks passed.
- Production website rendered in headless Chrome at 1440 x 1000 and
  393 x 1000. Inspected calendar, date converter, prayer times, astronomy,
  flat map, and settings. Persian glyphs, RTL calendar ordering, selected
  date, map paths, and narrow-screen layout rendered successfully.
- Hash navigation between those screens was exercised in the browser.

Browser UI connector was unavailable, so visual inspection used headless
Chrome screenshots. Safari, Firefox, physical touch devices, screen readers,
and the full set of interactive controls have not been manually validated.
Some supplemental labels and dataset category names retain English fallbacks.
The initial Wasm/Skia download is about 15 MiB before HTTP compression; static
hosting should enable compression and caching.

No public deployment or PWA setup was performed.
