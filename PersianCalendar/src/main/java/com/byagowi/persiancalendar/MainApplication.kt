package com.byagowi.persiancalendar

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.utils.initUtils
import kotlin.system.exitProcess

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ReleaseDebugDifference.mainApplication(this)
        initUtils(applicationContext)

        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                getSystemService<ClipboardManager>()?.setPrimaryClip(
                    ClipData.newPlainText("", Log.getStackTraceString(throwable))
                )
            } catch (e: Exception) {
            }

            oldHandler?.uncaughtException(thread, throwable) ?: exitProcess(2)
        }
    }
}
