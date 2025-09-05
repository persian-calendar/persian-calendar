package com.byagowi.persiancalendar.ui.settings.locationathan.location

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.generated.districtsStore
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.AppDialogWithLazyColumn
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.saveLocation
import io.github.persiancalendar.praytimes.Coordinates

@Composable
fun ProvincesDialog(onDismissRequest: () -> Unit, navigateUp: () -> Unit) {
    var province by rememberSaveable { mutableStateOf<String?>(null) }
    if (province != null) return DistrictsDialog(
        province = province.orEmpty(),
        onSuccess = onDismissRequest,
        navigateUp = { province = null },
    )
    AppDialogWithLazyColumn(
        title = { Text("انتخاب استان") },
        onDismissRequest = navigateUp,
    ) {
        items(districtsStore.keys.toList()) { provinceName ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { province = provinceName }
                    .height(SettingsItemHeight.dp)
                    .padding(horizontal = SettingsHorizontalPaddingItem.dp)
            ) { Text(provinceName) }
        }
    }
}

@Composable
fun DistrictsDialog(onSuccess: () -> Unit, navigateUp: () -> Unit, province: String) {
    val districts = remember {
        (districtsStore[province] ?: emptyList()).flatMap { county ->
            val countyDetails = county.split(";")
            countyDetails.drop(1).map { it.split(":") to countyDetails[0] }
        }.sortedBy { (district, _) -> language.value.prepareForSort(district[0/*district name*/]) }
    }
    val context = LocalContext.current
    AppDialogWithLazyColumn(title = { Text(province) }, onDismissRequest = navigateUp) {
        itemsIndexed(districts, { _, (district, _) -> district }) { index, (district, county) ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSuccess()
                        val coordinates = Coordinates(
                            districts[index].first[1/*latitude*/].toDoubleOrNull() ?: 0.0,
                            districts[index].first[2/*longitude*/].toDoubleOrNull() ?: 0.0,
                            0.0,
                        )
                        context.preferences.saveLocation(coordinates, districts[index].first[0])
                    }
                    .height(SettingsItemHeight.dp)
                    .padding(horizontal = SettingsHorizontalPaddingItem.dp)
            ) {
                Text(
                    buildAnnotatedString {
                        append(district[0])
                        append(" ")
                        withStyle(
                            LocalTextStyle.current.toSpanStyle().copy(
                                color = LocalContentColor.current.copy(alpha = .5f)
                            )
                        ) { append(county) }
                    }
                )
            }
        }
    }
}
