package com.byagowi.persiancalendar.di;

import com.byagowi.persiancalendar.view.fragment.MonthFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainFragmentModule {
    @PerChildFragment
    @ContributesAndroidInjector(modules = MainChildFragmentModule.class)
    abstract MonthFragment monthFragmentInjector();
}
