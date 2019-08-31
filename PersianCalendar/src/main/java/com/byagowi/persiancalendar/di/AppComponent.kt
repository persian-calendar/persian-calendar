package com.byagowi.persiancalendar.di

import com.byagowi.persiancalendar.MainApplication
import com.byagowi.persiancalendar.di.modules.AppModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AppModule::class])
interface AppComponent : AndroidInjector<MainApplication> {
    @Component.Factory
    abstract class Builder : AndroidInjector.Factory<MainApplication>
}
