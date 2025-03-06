package com.byagowi.persiancalendar.ui

import android.content.ContentUris
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.applyLanguageToConfiguration
import com.byagowi.persiancalendar.utils.eventKey
import com.byagowi.persiancalendar.utils.jdnActionKey
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.readAndStoreDeviceCalendarEventsOfTheDay
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import com.byagowi.persiancalendar.utils.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Just to make sure we have an initial transparent system bars
        // System bars are tweaked later with project's with real values
        applyEdgeToEdge(isBackgroundColorLight = false, isSurfaceColorLight = true)

        setTheme(R.style.BaseTheme)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)

        intent.getLongExtra(eventKey, -1L).takeIf { it != -1L }?.let { eventId ->
            val intent = Intent(Intent.ACTION_VIEW).setData(
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            )
            runCatching { startActivity(intent) }.onFailure(logException)
            return finish()
        }

        initGlobal(this)

        startWorker(this)

        readAndStoreDeviceCalendarEventsOfTheDay(applicationContext)
        update(applicationContext, false)

        val initialJdn = run {
            // Follows https://github.com/FossifyOrg/Calendar/blob/fb56145d/app/src/main/kotlin/org/fossify/calendar/activities/MainActivity.kt#L531-L554
            // Receives content://com.android.calendar/time/1740774600000 or content://0@com.android.calendar/time/1740774600000
            intent?.data?.takeIf {
                when (CalendarContract.AUTHORITY) {
                    it.authority, it.authority?.substringAfter("@") -> true
                    else -> false
                } && when {
                    it.path?.startsWith("/time") == true -> true
                    intent?.extras?.getBoolean("DETAIL_VIEW", false) == true -> true
                    else -> false
                }
            }?.pathSegments?.last()?.toLongOrNull()?.let {
                Jdn(Date(it).toGregorianCalendar().toCivilDate())
            } ?: (intent.getLongExtra(jdnActionKey, -1L).takeIf { it != -1L }
                ?: intent.action?.takeIf {
                    it.startsWith(jdnActionKey)
                }?.replace(jdnActionKey, "")?.toLongOrNull())?.let(::Jdn)
        }
        setContent {
            AppTheme {
                val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
                val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
                LaunchedEffect(isBackgroundColorLight, isSurfaceColorLight) {
                    applyEdgeToEdge(isBackgroundColorLight, isSurfaceColorLight)
                }

                val view = LocalView.current
                LaunchedEffect(Unit) {
                    language.collect {
                        onConfigurationChanged(resources.configuration)
                        view.dispatchConfigurationChanged(resources.configuration)
                    }
                }

                App(intent?.action, initialJdn, ::finish)
            }
        }

        applyAppLanguage(this)

        // There is a window:enforceNavigationBarContrast set to false in styles.xml as the following
        // isn't as effective in dark themes.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    private fun applyEdgeToEdge(isBackgroundColorLight: Boolean, isSurfaceColorLight: Boolean) {
        val statusBarStyle =
            if (isBackgroundColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) enableEdgeToEdge(
            statusBarStyle,
            if (isSurfaceColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
        ) else enableEdgeToEdge(
            statusBarStyle,
            // Just don't tweak navigation bar in older Android versions, leave it to default
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(applyLanguageToConfiguration(newConfig))
        applyAppLanguage(this)
    }

    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
        update(applicationContext, false)
        ++resumeToken_.value
    }
}

private val resumeToken_ = MutableStateFlow(0)
val resumeToken: StateFlow<Int> = resumeToken_
