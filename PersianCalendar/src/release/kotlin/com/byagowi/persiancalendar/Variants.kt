package com.byagowi.persiancalendar

import android.app.Application
import android.content.Context

@Suppress("UNUSED_PARAMETER")
object Variants {
    fun mainApplication(app: Application?) {} // Nothing here
    fun debugLog(message: String) {} // Nothing here
    inline val <T> T.debugAssertNotNull: T inline get() = this // Nothing here
    inline val enableDevelopmentFeatures get() = false
}
