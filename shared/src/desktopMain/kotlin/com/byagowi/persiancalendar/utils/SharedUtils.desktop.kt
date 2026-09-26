package com.byagowi.persiancalendar.utils

actual val logException: (Throwable) -> Unit = Throwable::printStackTrace

actual var isDebugBuild: Boolean = true // given low adoption let's always enable asserts fow now

actual fun debugLog(vararg message: Any?) {
    if (isDebugBuild) println(message.joinToString(", "))
}
