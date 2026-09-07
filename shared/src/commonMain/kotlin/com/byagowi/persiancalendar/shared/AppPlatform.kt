package com.byagowi.persiancalendar.shared

/** OS/browser operations stay outside the shared UI and calculations. */
interface AppPlatform {
    fun setLanguage(language: String, rightToLeft: Boolean)
    fun readPreference(key: String): String?
    fun writePreference(key: String, value: String)
    fun nowMillis(): Double
    fun todayJdn(): Long
    fun timeZoneIds(): List<String>
    fun timeInZone(epochMillis: Double, zone: String): String
    fun offsetHours(jdn: Long, zone: String): Double
    fun copy(text: String)
    fun share(text: String)
    fun download(name: String, mimeType: String, content: String)
    fun print()
    fun navigate(route: String)
    fun currentRoute(): String
    fun observeNavigation(onRoute: (String) -> Unit): () -> Unit
}
