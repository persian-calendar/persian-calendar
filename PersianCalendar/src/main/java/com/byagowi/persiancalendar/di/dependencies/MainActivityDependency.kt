package com.byagowi.persiancalendar.di.dependencies

import com.byagowi.persiancalendar.di.scopes.PerActivity
import com.byagowi.persiancalendar.ui.MainActivity

import javax.inject.Inject

@PerActivity
class MainActivityDependency @Inject
constructor() {
    @Inject
    lateinit var mainActivity: MainActivity internal set
}
