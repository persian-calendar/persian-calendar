package com.byagowi.persiancalendar.di;

import com.byagowi.persiancalendar.view.fragment.CalendarFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainActivityModule {
    @PerFragment
    @ContributesAndroidInjector(modules = MainFragmentModule.class)
    abstract CalendarFragment mainFragmentInjector();
}
