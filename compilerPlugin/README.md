# Platform type audit compiler plugin

This is a K2 FIR checker that reports direct dereferences of Java platform types.
It exists because Android lint's UAST does not expose JSpecify type-use annotations,
so the previous lint check could not distinguish an annotated Java declaration from
an unannotated platform type.

It runs as part of Android CI with `-PplatformTypeCheck`. For a local run:

```bash
./gradlew :PersianCalendar:compileDebugKotlin -PplatformTypeCheck
```

The check only reports the direct receiver of a function call or property access;
safe calls and Elvis expressions are left alone. Java APIs that are guaranteed to
return non-null despite missing platform metadata are listed in `ALWAYS_NON_NULL`
in `PlatformTypeCheckers.kt`. Add an entry only when the API contract guarantees it;
otherwise handle the nullable result or add an explicit type to the receiver.
