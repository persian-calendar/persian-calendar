package com.byagowi.persiancalendar.di.modules;

import com.byagowi.persiancalendar.di.scopes.PerFragment;
import com.byagowi.persiancalendar.ui.about.AboutFragment;
import com.byagowi.persiancalendar.ui.about.DeviceInformationFragment;
import com.byagowi.persiancalendar.ui.calendar.CalendarFragment;
import com.byagowi.persiancalendar.ui.compass.CompassFragment;
import com.byagowi.persiancalendar.ui.converter.ConverterFragment;
import com.byagowi.persiancalendar.ui.preferences.PreferencesFragment;
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.FragmentInterfaceCalendar;
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder.CalendarPreferenceDialog;
import com.byagowi.persiancalendar.ui.preferences.locationathan.FragmentLocationAthan;
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.GPSLocationDialog;
//import com.byagowi.persiancalendar.ui.reminder.EditReminderDialog;
//import com.byagowi.persiancalendar.ui.reminder.ReminderFragment;

import net.androgames.level.LevelFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainActivityModule {

    @PerFragment
    @ContributesAndroidInjector(modules = CalendarFragmentModule.class)
    abstract CalendarFragment calendarFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract PreferencesFragment settingsFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract CompassFragment compassFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract LevelFragment levelFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract AboutFragment aboutFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract DeviceInformationFragment deviceInfoFragmentInjector();

//    @PerFragment
//    @ContributesAndroidInjector
//    abstract ReminderFragment reminderFragmentInjector();
//
//    @PerFragment
//    @ContributesAndroidInjector
//    abstract EditReminderDialog editReminderFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract ConverterFragment converterFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract FragmentLocationAthan fragmentLocationAthanInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract FragmentInterfaceCalendar fragmentInterfaceCalendarInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract CalendarPreferenceDialog calendarPreferenceDialogInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract GPSLocationDialog gpsLocationDialogInjector();
}
