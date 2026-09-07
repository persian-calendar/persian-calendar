@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.byagowi.persiancalendar.shared.AppPlatform
import com.byagowi.persiancalendar.shared.BrowserCalendarApp
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.getElementById("app")!!) { BrowserCalendarApp(remember { BrowserPlatform() }) }
    document.getElementById("loading")?.remove()
}

private class BrowserPlatform : AppPlatform {
    override fun setLanguage(language: String, rightToLeft: Boolean) {
        document.documentElement?.setAttribute("lang", language)
        document.documentElement?.setAttribute("dir", if (rightToLeft) "rtl" else "ltr")
    }
    override fun readPreference(key: String) = read(key)
    override fun writePreference(key: String, value: String) = write(key, value)
    override fun nowMillis() = now()
    override fun todayJdn() = today().toLong()
    override fun timeZoneIds() = zones().split('|')
    override fun timeInZone(epochMillis: Double, zone: String) = zoneTime(epochMillis, zone)
    override fun offsetHours(jdn: Long, zone: String) = offset(jdn.toDouble(), zone)
    override fun copy(text: String) = copyText(text)
    override fun share(text: String) = shareText(text)
    override fun download(name: String, mimeType: String, content: String) = save(name, mimeType, content)
    override fun print() = printPage()
    override fun navigate(route: String) = navigateTo(route)
    override fun currentRoute() = route()
    override fun observeNavigation(onRoute: (String) -> Unit): () -> Unit {
        subscribe(onRoute)
        return { unsubscribe() }
    }
}

@JsFun("(key) => globalThis.calendarPlatform.read(key)") private external fun read(key: String): String?

@JsFun("(key, value) => globalThis.calendarPlatform.write(key, value)") private external fun write(key: String, value: String)

@JsFun("() => Date.now()") private external fun now(): Double

@JsFun("() => globalThis.calendarPlatform.today()") private external fun today(): Double

@JsFun("() => globalThis.calendarPlatform.zones()") private external fun zones(): String

@JsFun("(time, zone) => globalThis.calendarPlatform.time(time, zone)") private external fun zoneTime(time: Double, zone: String): String

@JsFun("(day, zone) => globalThis.calendarPlatform.offset(day, zone)") private external fun offset(day: Double, zone: String): Double

@JsFun("(text) => globalThis.calendarPlatform.copy(text)") private external fun copyText(text: String)

@JsFun("(text) => globalThis.calendarPlatform.share(text)") private external fun shareText(text: String)

@JsFun("(name, mime, content) => globalThis.calendarPlatform.save(name, mime, content)") private external fun save(name: String, mime: String, content: String)

@JsFun("() => window.print()") private external fun printPage()

@JsFun("(route) => { window.location.hash = route; }") private external fun navigateTo(route: String)

@JsFun("() => window.location.hash.slice(1) || 'calendar'") private external fun route(): String

@JsFun("(listener) => globalThis.calendarPlatform.subscribe(listener)") private external fun subscribe(listener: (String) -> Unit)

@JsFun("() => globalThis.calendarPlatform.unsubscribe()") private external fun unsubscribe()
