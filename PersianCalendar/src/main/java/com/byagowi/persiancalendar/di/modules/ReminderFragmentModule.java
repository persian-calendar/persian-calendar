package com.byagowi.persiancalendar.di.modules;

import com.byagowi.persiancalendar.di.scopes.PerChildFragment;
import com.byagowi.persiancalendar.view.reminder.fragment.EditReminderDialog;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ReminderFragmentModule {

    @PerChildFragment
    @ContributesAndroidInjector
    abstract EditReminderDialog editReminderFragmentInjector();
}
