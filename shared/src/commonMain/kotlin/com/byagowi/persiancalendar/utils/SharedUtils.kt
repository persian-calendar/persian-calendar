package com.byagowi.persiancalendar.utils

import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter

expect val logException: (Throwable) -> Unit

expect var isDebugBuild: Boolean

inline val <T> T.debugAssertNotNull: T
    inline get() = if (isDebugBuild) checkNotNull(this) else this

expect fun debugLog(vararg message: Any?)

val supportedYearOfIranCalendar: Int get() = IranianIslamicDateConverter.latestSupportedYearOfIran
