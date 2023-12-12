package com.byagowi.persiancalendar.ui.settings.locationathan.location

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.Dialog
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.saveCity
import com.byagowi.persiancalendar.utils.sortCityNames

@Composable
fun LocationDialog(onDismissRequest: () -> Unit) {
    var showProvincesDialog by rememberSaveable { mutableStateOf(false) }
    if (showProvincesDialog) return ProvincesDialog(onDismissRequest)
    val cities = remember { citiesStore.values.sortCityNames }
    Dialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.location)) },
        confirmButton = if (language.isIranExclusive) {
            {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showProvincesDialog = true },
                ) { Text(stringResource(R.string.more), Modifier.padding(8.dp)) }
            }
        } else null
    ) {
        val context = LocalContext.current
        cities.forEach { city ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .height(SettingsItemHeight.dp)
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        context.appPrefs.saveCity(city)
                    }
                    .padding(horizontal = SettingsHorizontalPaddingItem.dp),
            ) {
                Text(
                    buildAnnotatedString {
                        append(language.getCityName(city))
                        append(" ")
                        withStyle(
                            LocalTextStyle.current.toSpanStyle().copy(
                                color = LocalTextStyle.current.color.copy(.5f)
                            )
                        ) { append(language.getCountryName(city)) }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun LocationPreferenceDialogPreview() = AppTheme { LocationDialog {} }
