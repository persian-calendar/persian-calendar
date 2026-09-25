package com.byagowi.persiancalendar.parcelize

/**
 * Common replacement for `kotlinx.parcelize.Parcelize` so that `commonMain`
 * classes can be made Parcelable on Android without breaking other targets.
 *
 * See https://developer.android.com/kotlin/parcelize#setup_parcelize_for_kotlin_multiplatform
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class CommonParcelize

expect interface CommonParcelable
