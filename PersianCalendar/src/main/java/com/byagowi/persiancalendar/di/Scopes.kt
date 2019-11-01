package com.byagowi.persiancalendar.di

import javax.inject.Scope

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class PerActivity

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class PerChildFragment

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class PerFragment
