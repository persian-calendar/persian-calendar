package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_LARGE_DAY_NUMBER_ON_NOTIFICATION
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.isLargeDayNumberOnNotification
import com.byagowi.persiancalendar.global.isNotifyDate
import com.byagowi.persiancalendar.global.isNotifyDateOnLockScreen
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.utils.preferences

@Composable
fun ColumnScope.NotificationSettings() {
    val context = LocalContext.current
    val language by language.collectAsState()
    val isNotifyDate by isNotifyDate.collectAsState()
    run {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted -> context.preferences.edit { putBoolean(PREF_NOTIFY_DATE, isGranted) } }
        SettingsSwitch(
            key = PREF_NOTIFY_DATE,
            value = isNotifyDate,
            title = stringResource(R.string.notify_date),
            summary = stringResource(R.string.enable_notify),
            onBeforeToggle = { value: Boolean ->
                if (value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    false
                } else value
            },
        )
    }
    this.AnimatedVisibility(isNotifyDate) {
        val isNotifyDateOnLockScreen by isNotifyDateOnLockScreen.collectAsState()
        SettingsSwitch(
            key = PREF_NOTIFY_DATE_LOCK_SCREEN,
            value = isNotifyDateOnLockScreen,
            title = stringResource(R.string.notify_date_lock_screen),
            summary = stringResource(R.string.notify_date_lock_screen_summary),
        )
    }
    this.AnimatedVisibility(isNotifyDate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Box(
            Modifier
                .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                .clearAndSetSemantics {},
        ) {
            val isLargeDayNumberOnNotification by isLargeDayNumberOnNotification.collectAsState()
            SettingsSwitch(
                key = PREF_LARGE_DAY_NUMBER_ON_NOTIFICATION,
                value = isLargeDayNumberOnNotification,
                title = stringResource(R.string.large_day_number_on_notification),
                summary = when {
                    language.isPersianOrDari -> "نمایش روز ماه به صورت عددی بزرگ در اعلان برنامه"
                    else -> null
                },
            )
        }
    }
}
