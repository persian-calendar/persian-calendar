package com.byagowi.persiancalendar.ui

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.applyLanguageToConfiguration
import com.byagowi.persiancalendar.utils.readAndStoreDeviceCalendarEventsOfTheDay
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Just to make sure we have an initial transparent system bars
        // System bars are tweaked later with project's with real values
        applyEdgeToEdge(isBackgroundColorLight = false, isSurfaceColorLight = true)

        setTheme(R.style.BaseTheme)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)

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

                App(intent?.action, ::finish)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) enableEdgeToEdge(
            if (isBackgroundColorLight)
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
            if (isSurfaceColorLight)
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
        ) else enableEdgeToEdge( // Just don't tweak navigation bar in older Android versions
            if (isBackgroundColorLight)
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT)
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
