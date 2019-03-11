package com.byagowi.persiancalendar.di.modules;

import com.byagowi.persiancalendar.di.scopes.PerChildFragment;
import com.byagowi.persiancalendar.ui.calendar.dialogs.SelectDayDialog;
import com.byagowi.persiancalendar.ui.calendar.dialogs.ShiftWorkDialog;
import com.byagowi.persiancalendar.ui.calendar.month.MonthFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class CalendarFragmentModule {
    @PerChildFragment
    @ContributesAndroidInjector(modules = MainChildFragmentModule.class)
    abstract MonthFragment monthFragmentInjector();

    @PerChildFragment
    @ContributesAndroidInjector
    abstract SelectDayDialog selectDayDialogInjector();

    @PerChildFragment
    @ContributesAndroidInjector
    abstract ShiftWorkDialog shiftWorkDialogInjector();
}
