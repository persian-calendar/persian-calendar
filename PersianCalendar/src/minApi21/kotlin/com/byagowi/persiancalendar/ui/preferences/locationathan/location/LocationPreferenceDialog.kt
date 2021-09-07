package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.ui.ComposeTheme
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.language

fun showLocationPreferenceDialog(activity: Activity) {
    (activity.window.decorView as? ViewGroup)?.addView(ComposeView(activity).also { composeView ->
        composeView.setContent { ComposeTheme { LocationPreferenceDialog() } }
    })
}

@Composable
fun LocationPreferenceDialog() {
    val isDialogOpen = remember { mutableStateOf(true) }
    val cities = remember { citiesStore.values.sortedWith(language.createCitiesComparator()) }
    if (!isDialogOpen.value) return
    Surface(color = Color.Transparent) {
        AlertDialog(
            onDismissRequest = { isDialogOpen.value = false },
            title = { Text(stringResource(R.string.location)) },
            text = {
                LazyColumn {
                    items(cities) { city ->
                        val context = LocalContext.current
                        ClickableText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            text = AnnotatedString.Builder("").also {
                                it.pushStyle(SpanStyle(fontSize = 18.sp))
                                it.append(language.getCityName(city))
                                it.pop()
                                it.append(" ")
                                it.append(language.getCountryName(city))
                            }.toAnnotatedString(),
                            onClick = {
                                isDialogOpen.value = false
                                context.appPrefs.edit {
                                    remove(PREF_GEOCODED_CITYNAME)
                                    remove(PREF_LATITUDE)
                                    remove(PREF_LONGITUDE)
                                    remove(PREF_ALTITUDE)
                                    putString(PREF_SELECTED_LOCATION, city.key)
                                }
                            }
                        )
                    }
                }
            },
            buttons = {}
        )
    }
}

@Preview
@Composable
fun LocationPreferenceDialogPreview() {
    ComposeTheme { LocationPreferenceDialog() }
}
