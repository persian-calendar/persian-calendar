package com.byagowi.persiancalendar.utils

import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter
import io.github.persiancalendar.praytimes.Coordinates

expect val logException: (Throwable) -> Unit

expect var isDebugBuild: Boolean

@Suppress("CheckUsage") // This is the sanctioned debug-only assertion helper.
inline val <T> T.debugAssertNotNull: T
    inline get() = if (isDebugBuild) checkNotNull(this) else this

val Coordinates.isSouthernHemisphere get() = latitude < .0

expect fun debugLog(vararg message: Any?)

val supportedYearOfIranCalendar: Int get() = IranianIslamicDateConverter.latestSupportedYearOfIran
