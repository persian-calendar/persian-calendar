package com.byagowi.persiancalendar.ui.settings.locationathan

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.locationathan.location.CoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.GPSLocationDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.LocationDialog

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

    val cityName by cityName.collectAsState()
    SettingsClickable(stringResource(R.string.coordinates), cityName) { onDismissRequest ->
        CoordinatesDialog(navigateToMap = navigateToMap, onDismissRequest = onDismissRequest)
    }
}
