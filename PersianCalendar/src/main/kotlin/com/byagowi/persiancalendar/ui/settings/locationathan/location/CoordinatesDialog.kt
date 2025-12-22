package com.byagowi.persiancalendar.ui.settings.locationathan.location

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.SwitchWithLabel
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.utils.friendlyName
import com.byagowi.persiancalendar.utils.geocode
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.saveLocation
import io.github.persiancalendar.praytimes.Coordinates
import kotlin.math.abs

@Composable
fun CoordinatesDialog(
    navigateToMap: (() -> Unit)? = null,
    inputCoordinates: Coordinates? = null,
    notifyChange: (Coordinates) -> Unit = {},
    saveCoordinates: Boolean = true,
    isFromMap: Boolean = false,
    toggleSaveCoordinates: (Boolean) -> Unit = {},
    onDismissRequest: () -> Unit,
) {
    val coordinates = inputCoordinates ?: coordinates
    val fields = listOf(
        R.string.latitude to PREF_LATITUDE,
        R.string.longitude to PREF_LONGITUDE,
        R.string.altitude to PREF_ALTITUDE,
    )
    val state = (coordinates?.run { listOf(latitude, longitude, elevation) }
        ?: List(3) { .0 }).map { rememberSaveable { mutableStateOf(it.toString()) } }
    var cityName by rememberSaveable { mutableStateOf<String?>(null) }
    var countryCode by rememberSaveable { mutableStateOf<String?>(null) }
    // Whenever text field change this signals geocoder rerun
    // and no need to save as below remember also isn't saved
    var changeCounter by remember { mutableIntStateOf(0) }
    fun parseDouble(value: String): Double? = numeral.parseDouble(value.replace("°", ""))
    fun isValidPart(value: Double, i: Int): Boolean {
        return when (i) {
            0 -> abs(value) <= 90 // Valid latitudes
            1 -> abs(value) <= 180 // Valid longitudes
            else -> value in -418.0..8_849.0 // Altitude, from Dead Sea to Mount Everest
        }
    }
    AppDialog(
        title = { Text(stringResource(R.string.coordinates)) },
        neutralButton = {
            navigateToMap?.also {
                TextButton(onClick = {
                    onDismissRequest()
                    navigateToMap()
                }) { Text(stringResource(R.string.map)) }
            }
            if (isFromMap) SwitchWithLabel(
                stringResource(R.string.save),
                checked = saveCoordinates,
            ) { toggleSaveCoordinates(it) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        confirmButton = {
            val context = LocalContext.current
            TextButton(onClick = {
                onDismissRequest()
                val parts = state.mapIndexed { i, x ->
                    // Replace empty elevation with zero
                    if (i == 2 && x.value.isEmpty()) "0" else x.value
                }.mapIndexedNotNull { i, coordinate ->
                    // Make sure coordinates array has both parsable and in range numbers
                    parseDouble(coordinate)?.takeIf { isValidPart(it, i) }
                }
                if (parts.size == 3) {
                    val newCoordinates = Coordinates(parts[0], parts[1], parts[2])
                    if (saveCoordinates) context.preferences.saveLocation(
                        coordinates = newCoordinates,
                        cityName = cityName.orEmpty(),
                        countryCode = countryCode.orEmpty()
                    )
                    notifyChange(newCoordinates)
                } else context.preferences.edit { fields.map { it.second }.forEach(::remove) }
            }) { Text(stringResource(R.string.accept)) }
        },
        onDismissRequest = onDismissRequest,
    ) {
        fields.zip(state.withIndex()) { (stringId), (i, fieldState) ->
            val uiDirection = LocalLayoutDirection.current
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    label = {
                        CompositionLocalProvider(LocalLayoutDirection provides uiDirection) {
                            Text(stringResource(stringId), modifier = Modifier.fillMaxWidth())
                        }
                    },
                    isError = parseDouble(fieldState.value)?.takeIf { isValidPart(it, i) } == null,
                    value = numeral.format(fieldState.value, isInEdit = true).let {
                        if (stringId == R.string.altitude) it else it.replace("°", "") + "°"
                    },
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
                .padding(horizontal = 16.dp),
        )
        this.AnimatedVisibility(
            !cityName.isNullOrBlank(),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp),
        ) {
            AnimatedContent(
                cityName.orEmpty(),
                label = "summary",
                transitionSpec = appCrossfadeSpec,
            ) { state ->
                SelectionContainer { Text(state, style = MaterialTheme.typography.titleSmall) }
            }
        }
        val context = LocalContext.current
        LaunchedEffect(changeCounter) {
            val latitude = parseDouble(state[0].value) ?: return@LaunchedEffect
            val longitude = parseDouble(state[1].value) ?: return@LaunchedEffect
            if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return@LaunchedEffect
            val address = geocode(context, latitude, longitude)
            cityName = address?.friendlyName
            countryCode = address?.countryCode
        }
    }
}
