package com.byagowi.persiancalendar.utils

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

actual val logException: (Throwable) -> Unit = Throwable::printStackTrace

@OptIn(ExperimentalNativeApi::class)
actual var isDebugBuild: Boolean = Platform.isDebugBinary

actual fun debugLog(vararg message: Any?) {
    if (isDebugBuild) println(message.joinToString(", "))
}
