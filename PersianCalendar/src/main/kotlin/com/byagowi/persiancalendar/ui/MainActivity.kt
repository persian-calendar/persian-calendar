package com.byagowi.persiancalendar.ui

import android.content.ContentUris
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.initialDayFromIntent
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.eventKey
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.readAndStoreDeviceCalendarEventsOfTheDay
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.update

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Just to make sure we have an initial transparent system bars
        // System bars are tweaked later with project's theme matching values
        applyEdgeToEdge(isBackgroundColorLight = false, isSurfaceColorLight = true)

        setTheme(R.style.BaseTheme)
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

        setContent {
            AppTheme {
                val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
                val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
                LaunchedEffect(isBackgroundColorLight, isSurfaceColorLight) {
                    applyEdgeToEdge(isBackgroundColorLight, isSurfaceColorLight)
                }

                val view = LocalView.current
                LaunchedEffect(language) {
                    onConfigurationChanged(resources.configuration)
                    view.dispatchConfigurationChanged(resources.configuration)
                }

                val initialJdn = initialDayFromIntent(intent)
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

    override fun onResume() {
        super.onResume()
        update(applicationContext, false)
        ++resumeToken_.intValue
    }
}

private val resumeToken_ = mutableIntStateOf(0)
val resumeToken by resumeToken_
