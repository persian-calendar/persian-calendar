package com.byagowi.persiancalendar.di.modules

import com.byagowi.persiancalendar.di.scopes.PerActivity
import com.byagowi.persiancalendar.ui.MainActivity

import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector

@Module(includes = [AndroidInjectionModule::class])
abstract class AppModule {
    @PerActivity
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    internal abstract fun mainActivityInjector(): MainActivity
}
