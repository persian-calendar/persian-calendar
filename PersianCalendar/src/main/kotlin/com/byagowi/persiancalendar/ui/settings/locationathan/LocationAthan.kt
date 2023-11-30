package com.byagowi.persiancalendar.ui.settings.locationathan

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.byagowi.persiancalendar.DEFAULT_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.DEFAULT_HIGH_LATITUDES_METHOD
import com.byagowi.persiancalendar.DEFAULT_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.DEFAULT_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.EN_DASH
import com.byagowi.persiancalendar.POST_NOTIFICATION_PERMISSION_REQUEST_CODE_ENABLE_ATHAN_NOTIFICATION
import com.byagowi.persiancalendar.PREF_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.PREF_ASR_HANAFI_JURISTIC
import com.byagowi.persiancalendar.PREF_ATHAN_NAME
import com.byagowi.persiancalendar.PREF_HIGH_LATITUDES_METHOD
import com.byagowi.persiancalendar.PREF_MIDNIGHT_METHOD
import com.byagowi.persiancalendar.PREF_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.SettingsSection
import com.byagowi.persiancalendar.ui.settings.SettingsSingleSelect
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showAthanGapDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showAthanSelectDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showAthanVolumeDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showPrayerSelectDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showPrayerSelectPreviewDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showCoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showGPSLocationDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showLocationPreferenceDialog
import com.byagowi.persiancalendar.ui.utils.askForPostNotificationPermission
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.enableHighLatitudesConfiguration
import com.byagowi.persiancalendar.utils.titleStringId
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.HighLatitudesMethod
import io.github.persiancalendar.praytimes.MidnightMethod
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun LocationAthanSettings(activity: ComponentActivity, pickRingtone: () -> Unit) {
    val viewLifecycleOwner = LocalLifecycleOwner.current
    SettingsSection(stringResource(R.string.location))
    SettingsClickable(
        title = stringResource(R.string.gps_location),
        summary = stringResource(R.string.gps_location_help),
    ) { showGPSLocationDialog(activity, viewLifecycleOwner) }
    SettingsClickable(
        title = stringResource(R.string.location),
        summary = stringResource(R.string.location_help),
    ) { showLocationPreferenceDialog(activity) }
    SettingsClickable(stringResource(R.string.coordination)) {
        showCoordinatesDialog(activity, viewLifecycleOwner)
    }

    var isLocationSet by remember { mutableStateOf(coordinates.value != null) }
    var showHighLatitudesMethod by remember {
        mutableStateOf(enableHighLatitudesConfiguration)
    }
    var showAsrCalculationMethod by remember {
        mutableStateOf(!calculationMethod.isJafari)
    }
    val context = LocalContext.current
    val appPrefs = remember { context.appPrefs }
    var athanSoundName by remember {
        mutableStateOf(
            appPrefs.getString(
                PREF_ATHAN_NAME, context.getString(R.string.default_athan)
            )
        )
    }
    var showAscendingAthanVolume by remember {
        mutableStateOf(
            !appPrefs.getBoolean(PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN)
        )
    }
    var showAthanVolume by remember {
        mutableStateOf(
            !appPrefs.getBoolean(
                PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN
            ) && !appPrefs.getBoolean(
                PREF_ASCENDING_ATHAN_VOLUME, DEFAULT_ASCENDING_ATHAN_VOLUME
            )
        )
    }
    val scope = rememberCoroutineScope()
    scope.launch { coordinates.collectLatest { isLocationSet = coordinates.value != null } }
    DisposableEffect(null) {
        val listener = { _: SharedPreferences, _: String? ->
            updateStoredPreference(context)
            showHighLatitudesMethod = enableHighLatitudesConfiguration
            showAsrCalculationMethod = !calculationMethod.isJafari
            athanSoundName = appPrefs.getString(
                PREF_ATHAN_NAME, context.getString(R.string.default_athan)
            )
            showAscendingAthanVolume = !appPrefs.getBoolean(
                PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN
            )
            showAthanVolume = !appPrefs.getBoolean(
                PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN
            ) && !appPrefs.getBoolean(
                PREF_ASCENDING_ATHAN_VOLUME, DEFAULT_ASCENDING_ATHAN_VOLUME
            )
        }
        appPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { appPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    Divider()
    SettingsSection(
        stringResource(R.string.athan),
        if (isLocationSet) null else stringResource(R.string.athan_disabled_summary)
    )
    AnimatedVisibility(isLocationSet) {
        SettingsSingleSelect(
            PREF_PRAY_TIME_METHOD,
            CalculationMethod.entries.map { stringResource(it.titleStringId) },
            CalculationMethod.entries.map { it.name },
            DEFAULT_PRAY_TIME_METHOD,
            dialogTitleResId = R.string.pray_methods_calculation,
            title = stringResource(R.string.pray_methods)
        )
    }
    AnimatedVisibility(isLocationSet && showHighLatitudesMethod) {
        SettingsSingleSelect(
            PREF_HIGH_LATITUDES_METHOD,
            HighLatitudesMethod.entries.map { stringResource(it.titleStringId) },
            HighLatitudesMethod.entries.map { it.name },
            DEFAULT_HIGH_LATITUDES_METHOD,
            dialogTitleResId = R.string.high_latitudes_method,
            title = stringResource(R.string.high_latitudes_method)
        )
    }
    AnimatedVisibility(isLocationSet && showAsrCalculationMethod) {
        SettingsSwitch(
            PREF_ASR_HANAFI_JURISTIC,
            language.isHanafiMajority,
            stringResource(R.string.asr_hanafi_juristic)
        )
    }
    AnimatedVisibility(isLocationSet) {
        SettingsClickable(
            stringResource(R.string.athan_gap),
            stringResource(R.string.athan_gap_summary),
        ) { showAthanGapDialog(activity) }
    }
    AnimatedVisibility(isLocationSet) {
        SettingsClickable(
            stringResource(R.string.athan_alarm),
            stringResource(R.string.athan_alarm_summary),
        ) { showPrayerSelectDialog(activity) }
    }
    AnimatedVisibility(isLocationSet) {
        SettingsClickable(stringResource(R.string.custom_athan), athanSoundName) {
            showAthanSelectDialog(activity, pickRingtone)
        }
    }
    AnimatedVisibility(isLocationSet) {
        SettingsClickable(stringResource(R.string.preview)) { showPrayerSelectPreviewDialog(activity) }
    }
    AnimatedVisibility(isLocationSet) {
        SettingsSwitch(
            PREF_NOTIFICATION_ATHAN,
            DEFAULT_NOTIFICATION_ATHAN,
            stringResource(R.string.notification_athan),
            stringResource(R.string.enable_notification_athan),
            onBeforeToggle = { value ->
                if (value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                        activity, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    activity.askForPostNotificationPermission(
                        POST_NOTIFICATION_PERMISSION_REQUEST_CODE_ENABLE_ATHAN_NOTIFICATION
                    )
                    false
                } else value
            },
            followChanges = true,
        )
    }
    AnimatedVisibility(isLocationSet && showAscendingAthanVolume) {
        SettingsSwitch(
            PREF_ASCENDING_ATHAN_VOLUME,
            DEFAULT_ASCENDING_ATHAN_VOLUME,
            stringResource(R.string.ascending_athan_volume),
            stringResource(R.string.enable_ascending_athan_volume),
        )
    }
    AnimatedVisibility(isLocationSet && showAthanVolume) {
        SettingsClickable(
            stringResource(R.string.athan_volume), stringResource(R.string.athan_volume_summary)
        ) { showAthanVolumeDialog(activity) }
    }
    AnimatedVisibility(isLocationSet) {
        var midnightSummary by remember {
            mutableStateOf(getMidnightMethodPreferenceSummary(context))
        }
        SettingsClickable(stringResource(R.string.midnight), midnightSummary) {
            val methodsToShow =
                MidnightMethod.entries.filter { !it.isJafariOnly || calculationMethod.isJafari }
            val entryValues = listOf("DEFAULT") + methodsToShow.map { it.name }
            val entries = listOf(midnightDefaultTitle(context)) + methodsToShow.map {
                midnightMethodToString(context, it)
            }
            androidx.appcompat.app.AlertDialog.Builder(context).setTitle(R.string.midnight)
                .setNegativeButton(R.string.cancel, null).setSingleChoiceItems(
                    entries.toTypedArray(), entryValues.indexOf(
                        context.appPrefs.getString(PREF_MIDNIGHT_METHOD, null) ?: "DEFAULT"
                    )
                ) { dialog, which ->
                    context.appPrefs.edit {
                        if (which == 0) remove(PREF_MIDNIGHT_METHOD)
                        else putString(PREF_MIDNIGHT_METHOD, entryValues[which])
                    }
                    midnightSummary = entries[which]
                    dialog.dismiss()
                }.show()
        }
    }
}

private fun midnightDefaultTitle(context: Context): String {
    return context.getString(calculationMethod.titleStringId) + spacedComma + midnightMethodToString(
        context, calculationMethod.defaultMidnight
    )
}

private fun getMidnightMethodPreferenceSummary(context: Context): String {
    return context.appPrefs.getString(PREF_MIDNIGHT_METHOD, null)
        ?.let { midnightMethodToString(context, MidnightMethod.valueOf(it)) }
        ?: midnightDefaultTitle(context)
}

private fun midnightMethodToString(context: Context, method: MidnightMethod): String {
    return when (method) {
        MidnightMethod.MidSunsetToSunrise -> listOf(R.string.sunset, R.string.sunrise)

        MidnightMethod.MidSunsetToFajr -> listOf(R.string.sunset, R.string.fajr)

        MidnightMethod.MidMaghribToSunrise -> listOf(R.string.maghrib, R.string.sunrise)

        MidnightMethod.MidMaghribToFajr -> listOf(R.string.maghrib, R.string.fajr)
    }.joinToString(EN_DASH) { context.getString(it) }
}
