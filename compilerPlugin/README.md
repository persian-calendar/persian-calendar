# Platform type audit compiler plugin

This is an opt-in K2 FIR checker that reports direct dereferences of Java platform
types. It exists because Android lint's UAST does not expose JSpecify type-use
annotations, so the previous lint check could not distinguish an annotated Java
declaration from an unannotated platform type.

Run it as a CLI audit:

```bash
./gradlew :PersianCalendar:compileDebugKotlin -PplatformTypeCheck
```

The plugin is not applied to normal builds and is intentionally not wired into CI
yet. A full app run currently reports roughly 250 existing direct dereferences,
mostly Android and Java SDK APIs whose nullability is absent from the compiler
metadata, so a failing gate needs a baseline or a narrower package policy first.
