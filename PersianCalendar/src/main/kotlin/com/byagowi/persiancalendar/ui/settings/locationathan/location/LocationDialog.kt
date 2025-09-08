package com.byagowi.persiancalendar.ui.settings.locationathan.location

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.AppDialogWithLazyColumn
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.saveCity
import com.byagowi.persiancalendar.utils.sortCityNames

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDialog(onDismissRequest: () -> Unit) {
    var showProvincesDialog by rememberSaveable { mutableStateOf(false) }
    if (showProvincesDialog) return ProvincesDialog(
        onDismissRequest = onDismissRequest,
        navigateUp = { showProvincesDialog = false }
    )
    val cities = remember { citiesStore.values.sortCityNames }
    val language by language.collectAsState()
    val context = LocalContext.current
    AppDialogWithLazyColumn(
        onDismissRequest = onDismissRequest,
        title = {
            var query by rememberSaveable { mutableStateOf("") }
            val expanded = query.isNotEmpty()
            val padding by animateDpAsState(if (expanded) 0.dp else 8.dp, label = "padding")
            val focusRequester = remember { FocusRequester() }
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = {},
                        expanded = expanded,
                        onExpandedChange = {},
                        placeholder = {
                            Row {
                                Icon(Icons.Default.Search, null)
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.location))
                            }
                        },
                        trailingIcon = {
                            AppIconButton(
                                icon = Icons.Default.Close,
                                title = stringResource(R.string.close),
                            ) {
//                                viewModel.closeSearch()
                            }
                        },
//                        modifier = Modifier.border(
//                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
//                            MaterialTheme.shapes.extraLarge,
//                        )
                    )
                },
                expanded = expanded,
                onExpandedChange = { if (!it) query = "" },
                modifier = Modifier
                    .padding(horizontal = padding)
                    .focusRequester(focusRequester),
            ) {
                Text(stringResource(R.string.location))
            }
        },
        confirmButton = if (language.isIranExclusive) {
            {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showProvincesDialog = true },
                ) { Text(stringResource(R.string.more), Modifier.padding(8.dp)) }
            }
        } else null
    ) {
        items(cities, key = { it.key }) { city ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .height(SettingsItemHeight.dp)
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        context.preferences.saveCity(city)
                    }
                    .padding(horizontal = SettingsHorizontalPaddingItem.dp),
            ) {
                Text(
                    buildAnnotatedString {
                        append(language.getCityName(city))
                        append(" ")
                        withStyle(
                            LocalTextStyle.current.toSpanStyle().copy(
                                color = LocalContentColor.current.copy(alpha = .5f)
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
private fun LocationPreferenceDialogPreview() = LocationDialog {}
