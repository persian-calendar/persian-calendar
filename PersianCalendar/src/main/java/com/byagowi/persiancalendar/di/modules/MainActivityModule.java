package com.byagowi.persiancalendar.di.modules;

import com.byagowi.persiancalendar.di.scopes.PerFragment;
import com.byagowi.persiancalendar.view.fragment.CalendarFragment;
import com.byagowi.persiancalendar.view.preferences.FragmentLocationAthan;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainActivityModule {
    @PerFragment
    @ContributesAndroidInjector(modules = MainFragmentModule.class)
    abstract CalendarFragment calendarFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector(modules = MainFragmentModule.class)
    abstract FragmentLocationAthan fragmentLocationAthanInjector();
}
