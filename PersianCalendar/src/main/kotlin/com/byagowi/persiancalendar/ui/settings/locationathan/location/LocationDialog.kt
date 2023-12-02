package com.byagowi.persiancalendar.ui.settings.locationathan.location

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.saveCity
import com.byagowi.persiancalendar.utils.sortCityNames
import com.google.accompanist.themeadapter.material3.Mdc3Theme

@Composable
fun LocationDialog(onMoreButtonClick: () -> Unit, onDismissRequest: () -> Unit) {
    val cities = remember { citiesStore.values.sortCityNames }
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column {
                run {
                    Text(
                        stringResource(R.string.location),
                        modifier = Modifier.padding(top = 24.dp, start = 24.dp),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                Box(
                    Modifier
                        .weight(weight = 1f, fill = false)
                        .align(Alignment.Start)
                ) {
                    LazyColumn {
                        items(cities) { city ->
                            val context = LocalContext.current
                            Text(
                                buildAnnotatedString {
                                    withStyle(MaterialTheme.typography.bodyLarge.toSpanStyle()) {
                                        append(language.getCityName(city))
                                    }
                                    append(" ")
                                    withStyle(
                                        MaterialTheme.typography.bodyMedium.copy(
                                            color = LocalTextStyle.current.color.copy(.5f)
                                        ).toSpanStyle()
                                    ) { append(language.getCountryName(city)) }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismissRequest()
                                        context.appPrefs.saveCity(city)
                                    }
                                    .padding(vertical = 16.dp, horizontal = 24.dp)
                            )
                        }
                    }
                }
                Box(modifier = Modifier.align(Alignment.End)) {
                    if (language.isIranExclusive) {
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onDismissRequest()
                                onMoreButtonClick()
                            },
                        ) { Text(stringResource(R.string.more), Modifier.padding(8.dp)) }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LocationPreferenceDialogPreview() = Mdc3Theme { LocationDialog({}, {}) }
