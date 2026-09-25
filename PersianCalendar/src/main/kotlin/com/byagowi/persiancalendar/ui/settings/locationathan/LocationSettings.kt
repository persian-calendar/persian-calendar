package com.byagowi.persiancalendar.ui.settings.locationathan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.coordinates
import com.byagowi.persiancalendar.shared.generated.resources.gps_location
import com.byagowi.persiancalendar.shared.generated.resources.gps_location_help
import com.byagowi.persiancalendar.shared.generated.resources.location
import com.byagowi.persiancalendar.shared.generated.resources.location_help
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.locationathan.location.CoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.GPSLocationDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.LocationDialog
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.saveCity
import org.jetbrains.compose.resources.stringResource

@Composable
fun LocationSettings(
    modifier: Modifier = Modifier,
    navigateToMap: (() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        SettingsClickable(
            title = stringResource(Res.string.gps_location),
            summary = stringResource(Res.string.gps_location_help),
        ) { onDismissRequest -> GPSLocationDialog(onDismissRequest = onDismissRequest) }
        SettingsClickable(
            title = stringResource(Res.string.location),
            summary = stringResource(Res.string.location_help),
        ) { onDismissRequest -> LocationDialog(onDismissRequest = onDismissRequest) }

        SettingsClickable(
            title = stringResource(Res.string.coordinates),
            summary = cityName,
        ) { onDismissRequest ->
            CoordinatesDialog(
                navigateToMap = navigateToMap,
                onDismissRequest = onDismissRequest,
            )
        }
        AnimatedVisibility(
            visible = coordinates != null && (language.isPersianOrDari || when (language) {
                Language.EN_US, Language.EN_IR -> true
                else -> false
            }),
        ) {
            val context = LocalContext.current
            SettingsClickable(
                title = when {
                    language.isPersianOrDari -> "عدم نمایش اوقات"
                    else -> "Don't display times"
                },
            ) { citiesStore["CUSTOM"]?.let { context.preferences.saveCity(it) } }
        }
    }
}
