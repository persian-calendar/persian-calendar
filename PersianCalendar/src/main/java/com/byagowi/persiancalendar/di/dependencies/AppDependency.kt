package com.byagowi.persiancalendar.di.dependencies

import android.content.SharedPreferences
import android.preference.PreferenceManager

import com.byagowi.persiancalendar.MainApplication

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDependency @Inject
constructor(app: MainApplication) {
    val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
}
