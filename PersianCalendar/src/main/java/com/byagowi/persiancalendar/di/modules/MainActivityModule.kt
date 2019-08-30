package com.byagowi.persiancalendar.di.modules

import com.byagowi.persiancalendar.di.scopes.PerFragment
import com.byagowi.persiancalendar.ui.about.AboutFragment
import com.byagowi.persiancalendar.ui.about.DeviceInformationFragment
import com.byagowi.persiancalendar.ui.calendar.CalendarFragment
import com.byagowi.persiancalendar.ui.compass.CompassFragment
import com.byagowi.persiancalendar.ui.converter.ConverterFragment
import com.byagowi.persiancalendar.ui.preferences.PreferencesFragment
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.FragmentInterfaceCalendar
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder.CalendarPreferenceDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.FragmentLocationAthan
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.GPSLocationDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector
import net.androgames.level.LevelFragment

//import com.byagowi.persiancalendar.ui.reminder.EditReminderDialog;
//import com.byagowi.persiancalendar.ui.reminder.ReminderFragment;

@Module
abstract class MainActivityModule {

    @PerFragment
    @ContributesAndroidInjector(modules = [CalendarFragmentModule::class])
    internal abstract fun calendarFragmentInjector(): CalendarFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun settingsFragmentInjector(): PreferencesFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun compassFragmentInjector(): CompassFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun levelFragmentInjector(): LevelFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun aboutFragmentInjector(): AboutFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun deviceInfoFragmentInjector(): DeviceInformationFragment

    //    @PerFragment
    //    @ContributesAndroidInjector
    //    abstract ReminderFragment reminderFragmentInjector();
    //
    //    @PerFragment
    //    @ContributesAndroidInjector
    //    abstract EditReminderDialog editReminderFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun converterFragmentInjector(): ConverterFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun fragmentLocationAthanInjector(): FragmentLocationAthan

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun fragmentInterfaceCalendarInjector(): FragmentInterfaceCalendar

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun calendarPreferenceDialogInjector(): CalendarPreferenceDialog

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun gpsLocationDialogInjector(): GPSLocationDialog
}
