package com.byagowi.persiancalendar

import android.app.Application

import com.byagowi.persiancalendar.util.Utils

class MainApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    Utils.initUtils(applicationContext)
  }
}
