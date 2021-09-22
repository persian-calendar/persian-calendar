package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.ui.ComposeTheme
import com.byagowi.persiancalendar.ui.utils.showComposeDialog
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.language
import com.byagowi.persiancalendar.utils.saveCity
import com.byagowi.persiancalendar.utils.sortCityNames

fun showLocationPreferenceDialog(activity: Activity) =
    showComposeDialog(activity) { LocationPreferenceDialog(it) { showProvinceDialog(activity) } }

@Composable
private fun LocationPreferenceDialog(closeDialog: () -> Unit, onMoreButtonClick: () -> Unit) {
    val cities = remember { citiesStore.values.sortCityNames }
    AlertDialog(
        onDismissRequest = { closeDialog() },
        title = { Text(stringResource(R.string.location)) },
        text = {
            LazyColumn {
                items(cities) { city ->
                    val context = LocalContext.current
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clickable {
                                closeDialog()
                                context.appPrefs.saveCity(city)
                            }
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(
                                    LocalTextStyle.current.copy(fontSize = 18.sp)
                                        .toSpanStyle()
                                ) { append(language.getCityName(city)) }
                                append(" ")
                                withStyle(
                                    LocalTextStyle.current.copy(color = Color(0xFF888888))
                                        .toSpanStyle()
                                ) { append(language.getCountryName(city)) }
                            },
                        )
                    }
                }
            }
        },
        buttons = {
            if (language.isIranExclusive) {
                TextButton(onClick = {
                    closeDialog()
                    onMoreButtonClick()
                }) { Text(stringResource(R.string.more)) }
            }
        }
    )
}

@Preview
@Composable
private fun LocationPreferenceDialogPreview() = ComposeTheme { LocationPreferenceDialog({}, {}) }
