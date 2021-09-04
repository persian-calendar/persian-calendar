package com.byagowi.persiancalendar

import android.app.Application
import android.content.Context
import android.util.Log

@Suppress("UNUSED_PARAMETER")
object Variants {
    fun mainApplication(app: Application?) {} // Nothing here
    fun logDebug(message: String) = Log.d(LOG_TAG, message)
    inline val <T> T.debugAssertNotNull: T
        inline get() = this ?: throw NullPointerException("A debug only assert has happened")
    inline val enableDevelopmentFeatures get() = true
}
