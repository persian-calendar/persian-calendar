package com.byagowi.persiancalendar.di

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.byagowi.persiancalendar.MainApplication
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.calendar.CalendarFragment
import com.byagowi.persiancalendar.ui.calendar.month.DaysPaintResources
import javax.inject.Inject
import javax.inject.Singleton

@PerFragment
class CalendarFragmentDependency @Inject
constructor() {
    @Inject
    lateinit var calendarFragment: CalendarFragment
        internal set
}

@Singleton
class AppDependency @Inject
constructor(app: MainApplication) {
    val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
}

@PerActivity
class MainActivityDependency @Inject
constructor() {
    @Inject
    lateinit var mainActivity: MainActivity
        internal set
}

//
//@PerChildFragment
//public final class MonthFragmentDependency {
//    @Inject
//    public MonthFragmentDependency() {
//    }
//}
