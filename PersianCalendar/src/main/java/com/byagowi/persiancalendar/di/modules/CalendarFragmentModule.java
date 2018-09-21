package com.byagowi.persiancalendar.di.modules;

import com.byagowi.persiancalendar.di.scopes.PerChildFragment;
import com.byagowi.persiancalendar.view.dialog.SelectDayDialog;
import com.byagowi.persiancalendar.view.fragment.MonthFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class CalendarFragmentModule {
    @PerChildFragment
    @ContributesAndroidInjector(modules = MainChildFragmentModule.class)
    abstract MonthFragment monthFragmentInjector();

    @PerChildFragment
    @ContributesAndroidInjector(modules = MainChildFragmentModule.class)
    abstract SelectDayDialog selectDayDialogInjector();
}
