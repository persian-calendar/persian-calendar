package com.byagowi.persiancalendar

import android.app.Application
import android.content.Context

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
}