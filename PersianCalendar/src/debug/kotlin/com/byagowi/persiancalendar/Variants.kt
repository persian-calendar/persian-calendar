package com.byagowi.persiancalendar

import android.app.Application
import android.util.Log

object Variants {
    fun mainApplication(app: Application?) {
//        // This process is dedicated to LeakCanary for heap analysis.
//        // You should not init your app in this process.
//        if (LeakCanary.isInAnalyzerProcess(app)) return
////        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
////                .detectAll()
////                .penaltyLog()
////                .penaltyDeath()
////                .build())
//        LeakCanary.install(app)
    }

    fun debugLog(message: String) = Log.d(LOG_TAG, message)
    inline val <T> T.debugAssertNotNull: T inline get() = checkNotNull(this)
    inline val enableDevelopmentFeatures get() = true
}
