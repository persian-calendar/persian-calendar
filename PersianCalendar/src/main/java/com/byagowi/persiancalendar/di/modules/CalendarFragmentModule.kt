package com.byagowi.persiancalendar.di.modules

import com.byagowi.persiancalendar.di.scopes.PerChildFragment
import com.byagowi.persiancalendar.ui.calendar.dialogs.MonthOverviewDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.SelectDayDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.ShiftWorkDialog
import com.byagowi.persiancalendar.ui.calendar.month.MonthFragment

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CalendarFragmentModule {
    @PerChildFragment
    @ContributesAndroidInjector(modules = [MainChildFragmentModule::class])
    internal abstract fun monthFragmentInjector(): MonthFragment

    @PerChildFragment
    @ContributesAndroidInjector
    internal abstract fun selectDayDialogInjector(): SelectDayDialog

    @PerChildFragment
    @ContributesAndroidInjector
    internal abstract fun shiftWorkDialogInjector(): ShiftWorkDialog

    @PerChildFragment
    @ContributesAndroidInjector
    internal abstract fun monthOverviewDialogInjector(): MonthOverviewDialog
}
