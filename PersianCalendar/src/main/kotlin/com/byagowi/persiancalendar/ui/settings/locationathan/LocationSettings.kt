package com.byagowi.persiancalendar.ui.settings.locationathan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.locationathan.location.CoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.GPSLocationDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.LocationDialog
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.saveCity

@Composable
fun ColumnScope.LocationSettings(navigateToMap: (() -> Unit)? = null) {
    SettingsClickable(
        title = stringResource(R.string.gps_location),
        summary = stringResource(R.string.gps_location_help),
    ) { onDismissRequest -> GPSLocationDialog(onDismissRequest) }
    SettingsClickable(
        title = stringResource(R.string.location),
        summary = stringResource(R.string.location_help),
    ) { onDismissRequest -> LocationDialog(onDismissRequest) }

    SettingsClickable(stringResource(R.string.coordinates), cityName) { onDismissRequest ->
        CoordinatesDialog(navigateToMap = navigateToMap, onDismissRequest = onDismissRequest)
    }
    AnimatedVisibility(coordinates != null) {
        val context = LocalContext.current
        SettingsClickable(stringResource(R.string.dont_show_times)) {
            citiesStore["CUSTOM"]?.let { context.preferences.saveCity(it) }
        }
    }
}
