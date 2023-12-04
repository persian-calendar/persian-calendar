package com.byagowi.persiancalendar.ui.settings.locationathan.location

import android.location.Geocoder
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.Dialog
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.friendlyName
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.saveLocation
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Composable
fun CoordinatesDialog(
    navigateToMap: (() -> Unit)? = null,
    inputCoordinates: Coordinates? = null,
    onDismissRequest: () -> Unit,
) {
    val coordinates = inputCoordinates ?: coordinates.value
    val fields = listOf(
        R.string.latitude to PREF_LATITUDE,
        R.string.longitude to PREF_LONGITUDE,
        R.string.altitude to PREF_ALTITUDE,
    )
    val state = (coordinates?.run { listOf(latitude, longitude, elevation) } ?: List(3) { .0 })
        .map { rememberSaveable { mutableStateOf(it.toString()) } }
    var cityName by rememberSaveable { mutableStateOf<String?>(null) }
    var countryCode by rememberSaveable { mutableStateOf<String?>(null) }
    // Whenever text field change this signals geocoder rerun
    // and no need to save as below remember also isn't saved
    var changeCounter by remember { mutableStateOf(0) }
    Dialog(
        title = { Text(stringResource(R.string.coordination)) },
        neutralButton = {
            navigateToMap?.also {
                TextButton(onClick = {
                    onDismissRequest()
                    navigateToMap()
                }) { Text(stringResource(R.string.map)) }
            }
        },
        negativeButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        positiveButton = {
            val context = LocalContext.current
            TextButton(onClick = {
                onDismissRequest()
                val parts = state.mapIndexed { i, x ->
                    // Replace empty elevation with zero
                    if (i == 2 && x.value.isEmpty()) "0" else x.value
                }.mapIndexedNotNull { i, coordinate ->
                    // Make sure coordinates array has both parsable and in range numbers
                    coordinate.toDoubleOrNull()?.takeIf {
                        when (i) {
                            0 -> abs(it) <= 90 // Valid latitudes
                            1 -> abs(it) <= 180 // Valid longitudes
                            else -> it in -418.0..848.0 // Altitude, from Dead Sea to Mount Everest
                        }
                    }
                }
                if (parts.size == 3) {
                    context.appPrefs.saveLocation(
                        Coordinates(parts[0], parts[1], parts[2]),
                        cityName ?: "",
                        countryCode ?: ""
                    )
                } else context.appPrefs.edit { fields.map { it.second }.forEach(::remove) }
            }) { Text(stringResource(R.string.accept)) }
        },
        onDismissRequest = onDismissRequest,
    ) {
        fields.zip(state) { (stringId), fieldState ->
            val uiDirection = LocalLayoutDirection.current
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    label = {
                        CompositionLocalProvider(LocalLayoutDirection provides uiDirection) {
                            Text(
                                stringResource(stringId),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    value = fieldState.value,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    onValueChange = {
                        fieldState.value = it
                        ++changeCounter
                    },
                )
            }
        }
        Text(
            stringResource(R.string.altitude_praytime),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )
        if (!cityName.isNullOrBlank()) Text(
            cityName ?: "",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp),
        )
        val context = LocalContext.current
        LaunchedEffect(changeCounter) {
            launch(Dispatchers.IO) {
                runCatching {
                    val latitude = state[0].value.toDoubleOrNull() ?: 0.0
                    val longitude = state[1].value.toDoubleOrNull() ?: 0.0
                    val geocoder = Geocoder(context, language.asSystemLocale())
                        .getFromLocation(latitude, longitude, 20)
                    // TODO: Is it needed to change the state in the main thread in Compose?
                    withContext(Dispatchers.Main.immediate) {
                        val result = geocoder?.getOrNull(0)
                        cityName = result?.friendlyName
                        countryCode = result?.countryCode
                    }
                }.onFailure(logException)
            }
        }
    }
}
