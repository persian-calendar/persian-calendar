package com.byagowi.persiancalendar.utils

import kotlinx.browser.window

actual val logException: (Throwable) -> Unit = Throwable::printStackTrace

actual var isDebugBuild: Boolean = !window.location.href.startsWith("https:")

actual fun debugLog(vararg message: Any?) = console.log(message)
