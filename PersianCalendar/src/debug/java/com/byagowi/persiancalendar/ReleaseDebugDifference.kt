package com.byagowi.persiancalendar

import android.app.Application
import android.content.Context
import android.util.Log

object ReleaseDebugDifference {
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

    fun startLynxListenerIfIsDebug(context: Context?) = Unit // LynxShakeDetector(context).init()

    fun logDebug(tag: String, msg: String) = Log.d(tag, msg)

    val <T> T.debugAssertNotNull: T
        inline get() = this ?: throw NullPointerException("A debug only assert has happened")
}